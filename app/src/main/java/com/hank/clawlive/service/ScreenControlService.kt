package com.hank.clawlive.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.hank.clawlive.data.local.DeviceManager
import com.hank.clawlive.data.remote.SocketManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * AccessibilityService that enables bot-driven phone control.
 *
 * Optimizations:
 * - OkHttp connection pool: eliminates per-request TCP+TLS handshake
 * - MAX_ELEMENTS / MAX_DEPTH: prevents slow walk on complex screens (Chrome, etc.)
 * - Screen-change cache: if nothing changed since last capture, returns immediately
 */
class ScreenControlService : AccessibilityService() {

    companion object {
        private const val MAX_ELEMENTS = 150
        private const val MAX_DEPTH = 12

        // Single OkHttp client shared across all captures — connection pool reuses keep-alive connections
        private val httpClient by lazy {
            OkHttpClient.Builder()
                .connectTimeout(4, TimeUnit.SECONDS)
                .readTimeout(4, TimeUnit.SECONDS)
                .build()
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Screen-change cache
    private var cachedTree: JSONObject? = null
    @Volatile private var screenChanged = true  // true on first capture

    override fun onServiceConnected() {
        super.onServiceConnected()
        Timber.d("[ScreenControl] AccessibilityService connected")
        observeScreenRequests()
        observeControlCommands()
    }

    /**
     * Invalidate cache whenever the screen changes.
     * Configured to receive typeWindowStateChanged + typeWindowContentChanged.
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        screenChanged = true
    }

    override fun onInterrupt() {
        Timber.w("[ScreenControl] Service interrupted")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        serviceScope.cancel()
        return super.onUnbind(intent)
    }

    // ─── Screen capture ──────────────────────────────────────────────────

    private fun observeScreenRequests() {
        serviceScope.launch {
            SocketManager.screenRequestFlow.collect {
                Timber.d("[ScreenControl] Screen capture requested")
                try {
                    val tree = captureScreenTree()
                    postScreenResult(tree)
                } catch (e: Exception) {
                    Timber.e(e, "[ScreenControl] Screen capture failed")
                }
            }
        }
    }

    /**
     * Returns the cached tree if the screen hasn't changed since the last capture,
     * otherwise walks rootInActiveWindow (depth-first, max MAX_ELEMENTS / MAX_DEPTH).
     */
    private fun captureScreenTree(): JSONObject {
        // Fast path: return cached tree if screen unchanged
        val cached = cachedTree
        if (!screenChanged && cached != null) {
            Timber.d("[ScreenControl] Returning cached tree (${cached.getJSONArray("elements").length()} elements)")
            return cached
        }

        val root = rootInActiveWindow
        val packageName = root?.packageName?.toString() ?: "unknown"
        val elements = JSONArray()
        var nodeIndex = 0

        fun walk(node: AccessibilityNodeInfo?, depth: Int) {
            if (node == null || nodeIndex >= MAX_ELEMENTS || depth > MAX_DEPTH) return
            val text = node.text?.toString()
            val desc = node.contentDescription?.toString()
            val bounds = Rect()
            node.getBoundsInScreen(bounds)

            val isInteresting = !text.isNullOrEmpty() || !desc.isNullOrEmpty()
                    || node.isClickable || node.isScrollable || node.isEditable

            if (isInteresting) {
                val elem = JSONObject()
                elem.put("id", "n$nodeIndex")
                elem.put("type", node.className?.toString()?.substringAfterLast('.') ?: "View")
                if (!text.isNullOrEmpty()) elem.put("text", text)
                if (!desc.isNullOrEmpty()) elem.put("desc", desc)
                val boundsObj = JSONObject()
                boundsObj.put("x", bounds.left)
                boundsObj.put("y", bounds.top)
                boundsObj.put("w", bounds.width())
                boundsObj.put("h", bounds.height())
                elem.put("bounds", boundsObj)
                elem.put("clickable", node.isClickable)
                elem.put("scrollable", node.isScrollable)
                elem.put("editable", node.isEditable)
                elements.put(elem)
                nodeIndex++
            }

            for (i in 0 until node.childCount) {
                if (nodeIndex >= MAX_ELEMENTS) break
                walk(node.getChild(i), depth + 1)
            }
            if (node !== root) node.recycle()
        }

        walk(root, 0)
        root?.recycle()

        val result = JSONObject()
        result.put("screen", packageName)
        result.put("timestamp", System.currentTimeMillis())
        result.put("elements", elements)
        Timber.d("[ScreenControl] Captured ${nodeIndex} elements from $packageName")

        // Update cache
        cachedTree = result
        screenChanged = false
        return result
    }

    private fun postScreenResult(tree: JSONObject) {
        val dm = DeviceManager.getInstance(applicationContext)
        val body = JSONObject().apply {
            put("deviceId", dm.deviceId)
            put("deviceSecret", dm.deviceSecret)
            put("screen", tree.getString("screen"))
            put("timestamp", tree.getLong("timestamp"))
            put("elements", tree.getJSONArray("elements"))
        }

        val request = Request.Builder()
            .url("https://eclawbot.com/api/device/screen-result")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        httpClient.newCall(request).execute().use { response ->
            Timber.d("[ScreenControl] screen-result POST: HTTP ${response.code}")
        }
    }

    // ─── Control commands ─────────────────────────────────────────────────

    private fun observeControlCommands() {
        serviceScope.launch {
            SocketManager.controlCommandFlow.collect { json ->
                Timber.d("[ScreenControl] Control command: $json")
                try {
                    executeControlCommand(json)
                } catch (e: Exception) {
                    Timber.e(e, "[ScreenControl] Control command failed")
                }
            }
        }
    }

    private fun executeControlCommand(json: JSONObject) {
        val command = json.optString("command")
        val params = json.optJSONObject("params") ?: JSONObject()

        when (command) {
            "tap" -> executeTap(params)
            "type" -> executeType(params)
            "scroll" -> executeScroll(params)
            "back" -> performGlobalAction(GLOBAL_ACTION_BACK)
            "home" -> performGlobalAction(GLOBAL_ACTION_HOME)
            else -> Timber.w("[ScreenControl] Unknown command: $command")
        }
    }

    private fun executeTap(params: JSONObject) {
        val nodeId = params.optString("nodeId", "")
        if (nodeId.isNotEmpty()) {
            val node = findNodeById(nodeId)
            if (node != null) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                node.recycle()
            } else {
                Timber.w("[ScreenControl] Node not found: $nodeId")
            }
        } else {
            val x = params.optDouble("x", -1.0).toFloat()
            val y = params.optDouble("y", -1.0).toFloat()
            if (x >= 0 && y >= 0) {
                val path = Path()
                path.moveTo(x, y)
                val gesture = GestureDescription.Builder()
                    .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
                    .build()
                dispatchGesture(gesture, null, null)
            }
        }
    }

    private fun executeType(params: JSONObject) {
        val nodeId = params.optString("nodeId", "")
        val text = params.optString("text", "")
        val node = findNodeById(nodeId)
        if (node != null) {
            val arguments = Bundle()
            arguments.putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text
            )
            node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
            node.recycle()
        }
    }

    private fun executeScroll(params: JSONObject) {
        val nodeId = params.optString("nodeId", "")
        val direction = params.optString("direction", "down")
        val node = findNodeById(nodeId)
        if (node != null) {
            val action = if (direction == "up")
                AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
            else
                AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
            node.performAction(action)
            node.recycle()
        }
    }

    /**
     * Re-walks the tree to find a node by positional ID (same logic as captureScreenTree).
     */
    private fun findNodeById(nodeId: String): AccessibilityNodeInfo? {
        val targetIndex = nodeId.removePrefix("n").toIntOrNull() ?: return null
        val root = rootInActiveWindow ?: return null
        var currentIndex = 0
        var result: AccessibilityNodeInfo? = null

        fun walk(node: AccessibilityNodeInfo?, depth: Int) {
            if (node == null || result != null || depth > MAX_DEPTH) return
            val text = node.text?.toString()
            val desc = node.contentDescription?.toString()
            val isInteresting = !text.isNullOrEmpty() || !desc.isNullOrEmpty()
                    || node.isClickable || node.isScrollable || node.isEditable
            if (isInteresting) {
                if (currentIndex == targetIndex) {
                    result = node // caller owns — do NOT recycle
                    return
                }
                currentIndex++
            }
            for (i in 0 until node.childCount) {
                if (result != null) break
                val child = node.getChild(i)
                walk(child, depth + 1)
                if (result == null) child?.recycle()
            }
        }

        walk(root, 0)
        root.recycle()
        return result
    }
}

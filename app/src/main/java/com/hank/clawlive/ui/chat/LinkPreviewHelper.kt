package com.hank.clawlive.ui.chat

import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder

data class LinkPreviewData(
    val url: String,
    val title: String,
    val description: String,
    val image: String
)

object LinkPreviewHelper {

    private const val API_BASE = "https://eclawbot.com"
    private val cache = LruCache<String, LinkPreviewData>(100)

    private val urlRegex = Regex("https?://\\S+")
    private val imageExtRegex = Regex("""\.(?:png|jpe?g|gif|webp|bmp|svg)$""", RegexOption.IGNORE_CASE)

    /** Extract the first URL from a text string, or null */
    fun extractFirstUrl(text: String): String? {
        return urlRegex.find(text)?.value
    }

    /** Returns true if the URL points directly to an image file */
    fun isImageUrl(url: String): Boolean {
        return try {
            imageExtRegex.containsMatchIn(URL(url).path)
        } catch (_: Exception) {
            false
        }
    }

    /** Fetch link preview data from backend. Returns null if unavailable. */
    suspend fun fetch(url: String): LinkPreviewData? {
        cache.get(url)?.let { return it }

        return withContext(Dispatchers.IO) {
            try {
                val encoded = URLEncoder.encode(url, "UTF-8")
                val apiUrl = URL("$API_BASE/api/link-preview?url=$encoded")
                val conn = apiUrl.openConnection().apply {
                    connectTimeout = 5000
                    readTimeout = 5000
                }
                val json = conn.getInputStream().bufferedReader().use { it.readText() }
                val obj = JSONObject(json)

                val title = obj.optString("title", "")
                val desc = obj.optString("description", "")
                if (title.isEmpty() && desc.isEmpty()) {
                    return@withContext null
                }

                val resolvedUrl = obj.optString("url", url).ifEmpty { url }
                val data = LinkPreviewData(
                    url = resolvedUrl,
                    title = title,
                    description = desc,
                    image = obj.optString("image", "")
                )
                if (url.isNotEmpty()) cache.put(url, data)
                data
            } catch (e: Exception) {
                null
            }
        }
    }
}

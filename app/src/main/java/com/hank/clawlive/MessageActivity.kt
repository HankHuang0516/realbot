package com.hank.clawlive

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.hank.clawlive.data.local.ChatPreferences
import com.hank.clawlive.data.local.DeviceManager
import com.hank.clawlive.data.local.UsageManager
import com.hank.clawlive.data.remote.NetworkModule
import com.hank.clawlive.data.remote.ClawApiService
import com.hank.clawlive.data.repository.ChatRepository
import com.hank.clawlive.widget.ChatWidgetProvider
import kotlinx.coroutines.launch
import timber.log.Timber

class MessageActivity : AppCompatActivity() {

    private val api: ClawApiService by lazy { NetworkModule.api }
    private val chatPrefs: ChatPreferences by lazy { ChatPreferences.getInstance(this) }
    private val deviceManager: DeviceManager by lazy { DeviceManager.getInstance(this) }
    private val usageManager: UsageManager by lazy { UsageManager.getInstance(this) }
    private val chatRepository: ChatRepository by lazy { ChatRepository.getInstance(this) }

    private lateinit var chipGroupEntities: ChipGroup
    private lateinit var chipEntity0: Chip
    private lateinit var chipEntity1: Chip
    private lateinit var chipEntity2: Chip
    private lateinit var chipEntity3: Chip
    private lateinit var editMessage: TextInputEditText
    private lateinit var btnSend: MaterialButton
    private lateinit var btnCancel: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        // Allow dismiss on touch outside
        setFinishOnTouchOutside(true)

        initViews()
        setupListeners()
        loadBoundEntities()

        // Make it look like a dialog
        window.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun initViews() {
        chipGroupEntities = findViewById(R.id.chipGroupEntities)
        chipEntity0 = findViewById(R.id.chipEntity0)
        chipEntity1 = findViewById(R.id.chipEntity1)
        chipEntity2 = findViewById(R.id.chipEntity2)
        chipEntity3 = findViewById(R.id.chipEntity3)
        editMessage = findViewById(R.id.edit_message)
        btnSend = findViewById(R.id.btn_send)
        btnCancel = findViewById(R.id.btn_cancel)

        // Auto-focus on message input
        editMessage.requestFocus()
    }

    private fun setupListeners() {
        btnSend.setOnClickListener {
            val msg = editMessage.text.toString()
            if (msg.isNotEmpty()) {
                sendMessageToSelectedEntities(msg)
            } else {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            finish()
        }

        // Handle keyboard "Send" action
        editMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                val msg = editMessage.text.toString()
                if (msg.isNotEmpty()) {
                    sendMessageToSelectedEntities(msg)
                }
                true
            } else {
                false
            }
        }
    }

    /**
     * Load bound entities and enable/disable chips accordingly
     */
    private fun loadBoundEntities() {
        lifecycleScope.launch {
            try {
                // v5: Filter by this device's ID
                val response = api.getAllEntities(deviceId = deviceManager.deviceId)
                val boundIds = response.entities.map { it.entityId }.toSet()

                // Update chip states based on bound entities
                updateChipState(chipEntity0, 0, boundIds)
                updateChipState(chipEntity1, 1, boundIds)
                updateChipState(chipEntity2, 2, boundIds)
                updateChipState(chipEntity3, 3, boundIds)

                // If no entities are bound, show message
                if (boundIds.isEmpty()) {
                    Toast.makeText(
                        this@MessageActivity,
                        "No entities connected. Bind entities first.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load entities")
                // Keep all chips enabled as fallback
            }
        }
    }

    private fun updateChipState(chip: Chip, entityId: Int, boundIds: Set<Int>) {
        val isBound = boundIds.contains(entityId)
        chip.isEnabled = isBound
        chip.isChecked = isBound && entityId == 0 // Auto-select entity 0 if bound
        chip.alpha = if (isBound) 1.0f else 0.5f
    }

    /**
     * Get list of selected entity IDs
     */
    private fun getSelectedEntityIds(): List<Int> {
        val selected = mutableListOf<Int>()
        if (chipEntity0.isChecked) selected.add(0)
        if (chipEntity1.isChecked) selected.add(1)
        if (chipEntity2.isChecked) selected.add(2)
        if (chipEntity3.isChecked) selected.add(3)
        return selected
    }

    /**
     * Send message to all selected entities using broadcast API
     */
    private fun sendMessageToSelectedEntities(text: String) {
        val selectedIds = getSelectedEntityIds()

        if (selectedIds.isEmpty()) {
            Toast.makeText(this, "Please select at least one entity", Toast.LENGTH_SHORT).show()
            return
        }

        // Check usage limit before sending
        if (!usageManager.canUseMessage()) {
            showUpgradeDialog()
            return
        }

        // Fire and Forget: Don't wait for server response
        // User will see the result on the wallpaper through status polling

        // Increment usage immediately (optimistic)
        usageManager.incrementUsage()

        // Save to preferences (for widget history - legacy)
        chatPrefs.saveLastMessage(text, selectedIds)

        // Close immediately so user can see wallpaper response
        finish()

        // Send message in background (fire and forget)
        lifecycleScope.launch {
            // Save to database FIRST (optimistic - before API call)
            val messageId = chatRepository.saveOutgoingMessage(
                text = text,
                entityIds = selectedIds,
                source = "android_widget"
            )

            try {
                val entityIdValue: Any = if (selectedIds.size == 1) {
                    selectedIds.first()
                } else {
                    selectedIds
                }

                val request = mapOf<String, Any>(
                    "deviceId" to deviceManager.deviceId,
                    "entityId" to entityIdValue,
                    "text" to text,
                    "source" to "android_widget"
                )
                val response = api.sendClientMessage(request)

                // Mark as synced after API success
                chatRepository.markMessageSynced(messageId)

                // Log push status
                val pushedCount = response.targets.count { it.pushed }
                Timber.d("Message sent to device ${deviceManager.deviceId} entities $selectedIds (push: $pushedCount/${response.targets.size})")

                // Update widget in background
                ChatWidgetProvider.updateWidgets(this@MessageActivity)
            } catch (e: Exception) {
                // Log error but don't show to user - they already closed the dialog
                // Message is still in database (isSynced = false)
                Timber.e(e, "Background message send failed (user already moved on)")
            }
        }
    }

    /**
     * Show upgrade dialog when limit is reached
     */
    private fun showUpgradeDialog() {
        AlertDialog.Builder(this)
            .setTitle("🔒 Daily Limit Reached")
            .setMessage(
                "You've used all ${UsageManager.FREE_TIER_LIMIT} free messages today.\n\n" +
                "Upgrade to Premium for unlimited messages!"
            )
            .setPositiveButton("✨ Upgrade") { _, _ ->
                // Open MainActivity which has the subscribe button
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Later", null)
            .show()
    }
}

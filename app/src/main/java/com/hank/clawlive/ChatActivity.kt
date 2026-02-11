package com.hank.clawlive

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.hank.clawlive.data.local.ChatPreferences
import com.hank.clawlive.data.local.DeviceManager
import com.hank.clawlive.data.local.LayoutPreferences
import com.hank.clawlive.data.local.UsageManager
import com.hank.clawlive.data.local.database.ChatMessage
import com.hank.clawlive.data.remote.ClawApiService
import com.hank.clawlive.data.remote.NetworkModule
import com.hank.clawlive.data.repository.ChatRepository
import com.hank.clawlive.ui.chat.ChatAdapter
import com.hank.clawlive.widget.ChatWidgetProvider
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class ChatActivity : AppCompatActivity() {

    private val api: ClawApiService by lazy { NetworkModule.api }
    private val chatRepository: ChatRepository by lazy { ChatRepository.getInstance(this) }
    private val chatPrefs: ChatPreferences by lazy { ChatPreferences.getInstance(this) }
    private val deviceManager: DeviceManager by lazy { DeviceManager.getInstance(this) }
    private val layoutPrefs: LayoutPreferences by lazy { LayoutPreferences.getInstance(this) }
    private val usageManager: UsageManager by lazy { UsageManager.getInstance(this) }

    private lateinit var recyclerChat: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var editMessage: TextInputEditText
    private lateinit var btnSend: MaterialButton
    private lateinit var btnBack: ImageButton
    private lateinit var topBar: LinearLayout
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var chipGroupFilter: ChipGroup
    private lateinit var chipAll: Chip
    private lateinit var chipEntity0: Chip
    private lateinit var chipEntity1: Chip
    private lateinit var chipEntity2: Chip
    private lateinit var chipEntity3: Chip
    private lateinit var chipMyMessages: Chip
    private lateinit var chipGroupTargets: ChipGroup
    private lateinit var chipTarget0: Chip
    private lateinit var chipTarget1: Chip
    private lateinit var chipTarget2: Chip
    private lateinit var chipTarget3: Chip
    private lateinit var inputSection: LinearLayout

    // Current filter state
    private var showAll = true
    private var filterEntityIds = mutableSetOf<Int>()
    private var showOnlyMyMessages = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_chat)

        initViews()
        setupEdgeToEdgeInsets()
        setupRecyclerView()
        setupTargetChips()
        setupListeners()
        observeMessages()
    }

    private fun initViews() {
        recyclerChat = findViewById(R.id.recyclerChat)
        editMessage = findViewById(R.id.editMessage)
        btnSend = findViewById(R.id.btnSend)
        btnBack = findViewById(R.id.btnBack)
        topBar = findViewById(R.id.topBar)
        layoutEmpty = findViewById(R.id.layoutEmpty)
        chipGroupFilter = findViewById(R.id.chipGroupFilter)
        chipAll = findViewById(R.id.chipAll)
        chipEntity0 = findViewById(R.id.chipEntity0)
        chipEntity1 = findViewById(R.id.chipEntity1)
        chipEntity2 = findViewById(R.id.chipEntity2)
        chipEntity3 = findViewById(R.id.chipEntity3)
        chipMyMessages = findViewById(R.id.chipMyMessages)
        chipGroupTargets = findViewById(R.id.chipGroupTargets)
        chipTarget0 = findViewById(R.id.chipTarget0)
        chipTarget1 = findViewById(R.id.chipTarget1)
        chipTarget2 = findViewById(R.id.chipTarget2)
        chipTarget3 = findViewById(R.id.chipTarget3)
        inputSection = findViewById(R.id.inputSection)
    }

    private fun setupEdgeToEdgeInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { _, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )

            // Apply top inset to top bar
            topBar.updatePadding(
                left = insets.left + dpToPx(12),
                top = insets.top + dpToPx(12),
                right = insets.right + dpToPx(12)
            )

            // Apply bottom inset to input section
            inputSection.updatePadding(
                left = insets.left + dpToPx(8),
                right = insets.right + dpToPx(8),
                bottom = insets.bottom + dpToPx(8)
            )

            WindowInsetsCompat.CONSUMED
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter()
        recyclerChat.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true // Start from bottom (newest messages at bottom)
            }
        }

        // Scroll to bottom when new messages arrive
        chatAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                recyclerChat.scrollToPosition(chatAdapter.itemCount - 1)
            }
        })
    }

    private fun setupTargetChips() {
        // Get registered entity IDs from LayoutPreferences
        val registeredIds = layoutPrefs.getRegisteredEntityIds()

        // Show only registered entities, default all checked
        chipTarget0.visibility = if (0 in registeredIds) {
            chipTarget0.isChecked = true
            View.VISIBLE
        } else {
            View.GONE
        }

        chipTarget1.visibility = if (1 in registeredIds) {
            chipTarget1.isChecked = true
            View.VISIBLE
        } else {
            View.GONE
        }

        chipTarget2.visibility = if (2 in registeredIds) {
            chipTarget2.isChecked = true
            View.VISIBLE
        } else {
            View.GONE
        }

        chipTarget3.visibility = if (3 in registeredIds) {
            chipTarget3.isChecked = true
            View.VISIBLE
        } else {
            View.GONE
        }

        // Hide entire target selection if no entities registered
        if (registeredIds.isEmpty()) {
            chipGroupTargets.visibility = View.GONE
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnSend.setOnClickListener {
            sendMessage()
        }

        // Handle keyboard "Send" action
        editMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }

        // Filter chip listeners
        setupFilterListeners()
    }

    private fun setupFilterListeners() {
        chipAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showAll = true
                showOnlyMyMessages = false
                filterEntityIds.clear()
                // Uncheck other chips
                chipEntity0.isChecked = false
                chipEntity1.isChecked = false
                chipEntity2.isChecked = false
                chipEntity3.isChecked = false
                chipMyMessages.isChecked = false
                refreshMessages()
            }
        }

        chipMyMessages.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showAll = false
                showOnlyMyMessages = true
                filterEntityIds.clear()
                chipAll.isChecked = false
                chipEntity0.isChecked = false
                chipEntity1.isChecked = false
                chipEntity2.isChecked = false
                chipEntity3.isChecked = false
                refreshMessages()
            }
        }

        // Entity filter chips
        val entityChips = listOf(chipEntity0, chipEntity1, chipEntity2, chipEntity3)
        entityChips.forEachIndexed { index, chip ->
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    showAll = false
                    showOnlyMyMessages = false
                    filterEntityIds.add(index)
                    chipAll.isChecked = false
                    chipMyMessages.isChecked = false
                } else {
                    filterEntityIds.remove(index)
                    // If no filters selected, default to "All"
                    if (filterEntityIds.isEmpty() && !showOnlyMyMessages) {
                        showAll = true
                        chipAll.isChecked = true
                    }
                }
                refreshMessages()
            }
        }
    }

    private fun observeMessages() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe messages
                launch {
                    chatRepository.getMessagesAscending(100).collectLatest { messages ->
                        updateMessageList(messages)
                    }
                }

                // Observe available entities for smart filter chips
                launch {
                    chatRepository.getDistinctEntityIds().collectLatest { entityIds ->
                        updateFilterChipVisibility(entityIds)
                    }
                }
            }
        }
    }

    private fun refreshMessages() {
        lifecycleScope.launch {
            chatRepository.getMessagesAscending(100).collectLatest { messages ->
                updateMessageList(messages)
            }
        }
    }

    private fun updateFilterChipVisibility(entityIds: List<Int>) {
        // Always show "All" and "My Messages" chips
        chipAll.visibility = View.VISIBLE
        chipMyMessages.visibility = View.VISIBLE

        // Show entity chips only if they have messages, with character emojis
        // Entity 0, 2 = LOBSTER 🦞, Entity 1, 3 = PIG 🐷
        if (0 in entityIds) {
            chipEntity0.visibility = View.VISIBLE
            chipEntity0.text = "🦞 Entity 0"
        } else {
            chipEntity0.visibility = View.GONE
        }

        if (1 in entityIds) {
            chipEntity1.visibility = View.VISIBLE
            chipEntity1.text = "🐷 Entity 1"
        } else {
            chipEntity1.visibility = View.GONE
        }

        if (2 in entityIds) {
            chipEntity2.visibility = View.VISIBLE
            chipEntity2.text = "🦞 Entity 2"
        } else {
            chipEntity2.visibility = View.GONE
        }

        if (3 in entityIds) {
            chipEntity3.visibility = View.VISIBLE
            chipEntity3.text = "🐷 Entity 3"
        } else {
            chipEntity3.visibility = View.GONE
        }
    }

    private fun updateMessageList(messages: List<ChatMessage>) {
        val filtered = if (showAll) {
            messages
        } else if (showOnlyMyMessages) {
            messages.filter { it.isFromUser }
        } else {
            messages.filter { msg ->
                // Include user messages that target any selected entity
                if (msg.isFromUser) {
                    val targets = msg.getTargetEntityIdList()
                    targets.any { it in filterEntityIds }
                } else {
                    // Include entity messages from selected entities
                    msg.fromEntityId in filterEntityIds
                }
            }
        }

        chatAdapter.submitList(filtered)

        // Show/hide empty state
        if (filtered.isEmpty()) {
            layoutEmpty.visibility = View.VISIBLE
            recyclerChat.visibility = View.GONE
        } else {
            layoutEmpty.visibility = View.GONE
            recyclerChat.visibility = View.VISIBLE
        }
    }

    private fun getSelectedTargets(): List<Int> {
        val selected = mutableListOf<Int>()
        if (chipTarget0.isChecked) selected.add(0)
        if (chipTarget1.isChecked) selected.add(1)
        if (chipTarget2.isChecked) selected.add(2)
        if (chipTarget3.isChecked) selected.add(3)
        return selected
    }

    private fun sendMessage() {
        val text = editMessage.text.toString().trim()
        if (text.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            return
        }

        // Get selected target entities
        val targetIds = getSelectedTargets()
        if (targetIds.isEmpty()) {
            Toast.makeText(this, "Please select at least one entity", Toast.LENGTH_SHORT).show()
            return
        }

        // Check usage limit
        if (!usageManager.canUseMessage()) {
            showUpgradeDialog()
            return
        }

        // Clear input
        editMessage.text?.clear()

        // Increment usage
        usageManager.incrementUsage()

        // Save to preferences (legacy widget support)
        chatPrefs.saveLastMessage(text, targetIds)

        // Send message
        lifecycleScope.launch {
            // Save to database first
            val messageId = chatRepository.saveOutgoingMessage(
                text = text,
                entityIds = targetIds,
                source = "android_chat"
            )

            try {
                val entityIdValue: Any = if (targetIds.size == 1) {
                    targetIds.first()
                } else {
                    targetIds
                }

                val request = mapOf<String, Any>(
                    "deviceId" to deviceManager.deviceId,
                    "entityId" to entityIdValue,
                    "text" to text,
                    "source" to "android_chat"
                )
                val response = api.sendClientMessage(request)

                // Mark as synced
                chatRepository.markMessageSynced(messageId)

                Timber.d("Message sent from ChatActivity to entities $targetIds")

                // Check push notification status
                val pushedCount = response.targets.count { it.pushed }
                val totalCount = response.targets.size

                if (pushedCount == 0 && totalCount > 0) {
                    // No entities received push notification
                    val pollingEntities = response.targets.filter { it.mode == "polling" }
                    if (pollingEntities.isNotEmpty()) {
                        Timber.w("Push notification unavailable for ${pollingEntities.size} entity(s), using polling mode")
                        Toast.makeText(
                            this@ChatActivity,
                            "Message queued (entities will receive via polling)",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else if (pushedCount < totalCount) {
                    // Some but not all entities received push
                    Timber.w("Push notification partial: $pushedCount/$totalCount entities")
                }

                // Update widget
                ChatWidgetProvider.updateWidgets(this@ChatActivity)
            } catch (e: Exception) {
                Timber.e(e, "Failed to send message")
                Toast.makeText(this@ChatActivity, "Send failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showUpgradeDialog() {
        AlertDialog.Builder(this)
            .setTitle("Daily Limit Reached")
            .setMessage(
                "You've used all ${UsageManager.FREE_TIER_LIMIT} free messages today.\n\n" +
                        "Upgrade to Premium for unlimited messages!"
            )
            .setPositiveButton("Upgrade") { _, _ ->
                // Could navigate to SettingsActivity or show billing flow
                finish()
            }
            .setNegativeButton("Later", null)
            .show()
    }
}

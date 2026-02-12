package com.hank.clawlive

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import android.widget.GridLayout
import com.hank.clawlive.data.local.DeviceManager
import com.hank.clawlive.data.local.EntityEmojiManager
import com.hank.clawlive.data.model.CharacterState
import com.hank.clawlive.data.model.EntityStatus
import com.hank.clawlive.data.remote.NetworkModule
import com.hank.clawlive.ui.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import com.hank.clawlive.data.repository.StateRepository
import com.hank.clawlive.service.BatteryMonitor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val api = NetworkModule.api
    private val deviceManager by lazy { DeviceManager.getInstance(this) }
    private val emojiManager by lazy { EntityEmojiManager.getInstance(this) }
    private val stateRepository by lazy {
        StateRepository(NetworkModule.api, this)
    }
    private var batteryMonitor: BatteryMonitor? = null

    companion object {
        private const val API_BASE_URL = "https://realbot-production.up.railway.app"
    }

    // UI elements
    private lateinit var btnSettings: ImageButton
    private lateinit var agentCardsContainer: LinearLayout
    private lateinit var emptyStateContainer: LinearLayout
    private lateinit var cardAddEntity: MaterialCardView
    private lateinit var addEntityHeader: LinearLayout
    private lateinit var addEntityContent: LinearLayout
    private lateinit var ivExpandArrow: ImageView
    private lateinit var chipGroupEntity: ChipGroup
    private lateinit var tvBindingCode: TextView
    private lateinit var tvCountdown: TextView
    private lateinit var btnGenerateCode: MaterialButton
    private lateinit var btnCopyCommand: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBroadcast: MaterialButton
    private lateinit var btnSetWallpaper: MaterialButton
    private lateinit var topBar: LinearLayout
    private lateinit var bottomActions: LinearLayout

    private var boundEntities: List<EntityStatus> = emptyList()
    private var isAddEntityExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_main)

        initViews()
        setupEdgeToEdgeInsets()
        setupClickListeners()
        observeUiState()
        initBatteryMonitor()
    }

    /**
     * Initialize battery monitoring to report real device battery level to backend.
     * This replaces the simulated battery decay on the server.
     */
    private fun initBatteryMonitor() {
        batteryMonitor = BatteryMonitor(this) { batteryLevel ->
            stateRepository.updateBatteryLevel(batteryLevel)
        }
        val initialLevel = batteryMonitor?.start() ?: -1
        if (initialLevel >= 0) {
            // Report initial battery level immediately
            lifecycleScope.launch {
                stateRepository.updateBatteryLevel(initialLevel)
            }
        }
    }

    override fun onDestroy() {
        batteryMonitor?.stop()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        loadBoundEntities()
    }

    private fun initViews() {
        btnSettings = findViewById(R.id.btnSettings)
        agentCardsContainer = findViewById(R.id.agentCardsContainer)
        emptyStateContainer = findViewById(R.id.emptyStateContainer)
        cardAddEntity = findViewById(R.id.cardAddEntity)
        addEntityHeader = findViewById(R.id.addEntityHeader)
        addEntityContent = findViewById(R.id.addEntityContent)
        ivExpandArrow = findViewById(R.id.ivExpandArrow)
        chipGroupEntity = findViewById(R.id.chipGroupEntity)
        tvBindingCode = findViewById(R.id.tvBindingCode)
        tvCountdown = findViewById(R.id.tvCountdown)
        btnGenerateCode = findViewById(R.id.btnGenerateCode)
        btnCopyCommand = findViewById(R.id.btnCopyCommand)
        progressBar = findViewById(R.id.progressBar)
        btnBroadcast = findViewById(R.id.btnBroadcast)
        btnSetWallpaper = findViewById(R.id.btnSetWallpaper)
        topBar = findViewById(R.id.topBar)
        bottomActions = findViewById(R.id.bottomActions)
    }

    /**
     * Apply WindowInsets for edge-to-edge display.
     * UI elements get proper padding to avoid system bars.
     */
    private fun setupEdgeToEdgeInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { _, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )

            // Apply top inset to top bar (status bar + cutout)
            topBar.updatePadding(
                left = insets.left + 16.dpToPx(),
                top = insets.top + 12.dpToPx(),
                right = insets.right + 16.dpToPx()
            )

            // Apply bottom inset to bottom actions (navigation bar)
            bottomActions.updatePadding(
                left = insets.left + 16.dpToPx(),
                right = insets.right + 16.dpToPx(),
                bottom = insets.bottom + 16.dpToPx()
            )

            WindowInsetsCompat.CONSUMED
        }
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    private fun setupClickListeners() {
        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        btnSetWallpaper.setOnClickListener {
            openWallpaperPicker()
        }

        btnBroadcast.setOnClickListener {
            showBroadcastDialog()
        }

        // Add Entity expandable section
        addEntityHeader.setOnClickListener {
            toggleAddEntitySection()
        }

        btnGenerateCode.setOnClickListener {
            viewModel.generateBindingCode()
        }

        btnCopyCommand.setOnClickListener {
            copyFullCommand()
        }

        tvBindingCode.setOnClickListener {
            copyFullCommand()
        }

        // Entity selection chips
        chipGroupEntity.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val selectedEntityId = when (checkedIds[0]) {
                    R.id.chipEntity0 -> 0
                    R.id.chipEntity1 -> 1
                    R.id.chipEntity2 -> 2
                    R.id.chipEntity3 -> 3
                    else -> 0
                }
                viewModel.selectEntity(selectedEntityId)
            }
        }
    }

    private fun toggleAddEntitySection() {
        isAddEntityExpanded = !isAddEntityExpanded
        addEntityContent.visibility = if (isAddEntityExpanded) View.VISIBLE else View.GONE
        ivExpandArrow.rotation = if (isAddEntityExpanded) 180f else 0f
    }

    private fun loadBoundEntities() {
        lifecycleScope.launch {
            try {
                val response = api.getAllEntities(deviceId = deviceManager.deviceId)
                boundEntities = response.entities.filter { it.isBound }
                updateAgentCards()
            } catch (e: Exception) {
                Timber.e(e, "Failed to load entities")
            }
        }
    }

    private fun updateAgentCards() {
        agentCardsContainer.removeAllViews()

        if (boundEntities.isEmpty()) {
            emptyStateContainer.visibility = View.VISIBLE
            btnBroadcast.visibility = View.GONE
            // Auto-expand add entity section when empty
            if (!isAddEntityExpanded) {
                toggleAddEntitySection()
            }
        } else {
            emptyStateContainer.visibility = View.GONE
            btnBroadcast.visibility = View.VISIBLE

            boundEntities.forEach { entity ->
                val cardView = LayoutInflater.from(this)
                    .inflate(R.layout.item_agent_card, agentCardsContainer, false)
                bindAgentCard(cardView, entity)
                agentCardsContainer.addView(cardView)
            }
        }
    }

    private fun bindAgentCard(view: View, entity: EntityStatus) {
        // Emoji Icon (tap to change)
        val iconView = view.findViewById<TextView>(R.id.tvEntityIcon)
        iconView.text = emojiManager.getEmoji(entity.entityId)
        iconView.setOnClickListener {
            showEmojiPicker(entity.entityId, iconView)
        }

        // Name
        val displayName = entity.name ?: getString(R.string.entity_format, entity.entityId)
        view.findViewById<TextView>(R.id.tvEntityName).text = displayName

        // ID
        view.findViewById<TextView>(R.id.tvEntityId).text = "#${entity.entityId}"

        // State Badge
        val badgeView = view.findViewById<TextView>(R.id.tvStateBadge)
        badgeView.text = entity.state.name
        badgeView.setBackgroundColor(getStateBadgeColor(entity.state))

        // Last Message
        view.findViewById<TextView>(R.id.tvLastMessage).text = entity.message

        // Message Time
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        view.findViewById<TextView>(R.id.tvMessageTime).text = timeFormat.format(Date(entity.lastUpdated))

        // Send Message Button
        view.findViewById<MaterialButton>(R.id.btnSendMessage).setOnClickListener {
            showSendMessageDialog(entity)
        }
    }

    private fun getStateBadgeColor(state: CharacterState): Int {
        return when (state) {
            CharacterState.IDLE -> Color.parseColor("#4CAF50")
            CharacterState.BUSY -> Color.parseColor("#2196F3")
            CharacterState.EATING -> Color.parseColor("#FF9800")
            CharacterState.SLEEPING -> Color.parseColor("#607D8B")
            CharacterState.EXCITED -> Color.parseColor("#E91E63")
        }
    }

    private fun showEmojiPicker(entityId: Int, iconView: TextView) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_emoji_picker, null)
        val grid = dialogView.findViewById<GridLayout>(R.id.emojiGrid)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val currentEmoji = emojiManager.getEmoji(entityId)

        EntityEmojiManager.EMOJI_OPTIONS.forEach { emoji ->
            val tv = TextView(this).apply {
                text = emoji
                textSize = 28f
                gravity = android.view.Gravity.CENTER
                val size = (52 * resources.displayMetrics.density).toInt()
                layoutParams = GridLayout.LayoutParams().apply {
                    width = size
                    height = size
                }
                setBackgroundResource(
                    if (emoji == currentEmoji) R.drawable.badge_background
                    else android.R.color.transparent
                )
                setOnClickListener {
                    emojiManager.setEmoji(entityId, emoji)
                    iconView.text = emoji
                    dialog.dismiss()
                }
            }
            grid.addView(tv)
        }

        dialog.show()
    }

    private fun showSendMessageDialog(entity: EntityStatus) {
        val displayName = entity.name ?: getString(R.string.entity_format, entity.entityId)

        val editText = EditText(this).apply {
            hint = getString(R.string.message_hint, displayName)
            setPadding(48, 32, 48, 32)
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.send_message))
            .setMessage(getString(R.string.to_format, displayName, entity.entityId))
            .setView(editText)
            .setPositiveButton(getString(R.string.send)) { _, _ ->
                val message = editText.text.toString().trim()
                if (message.isNotEmpty()) {
                    sendMessageToEntity(entity.entityId, message)
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showBroadcastDialog() {
        val editText = EditText(this).apply {
            hint = getString(R.string.broadcast_message_hint)
            setPadding(48, 32, 48, 32)
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.broadcast_to_all))
            .setMessage(getString(R.string.send_to_entities, boundEntities.size))
            .setView(editText)
            .setPositiveButton(getString(R.string.broadcast_to_all)) { _, _ ->
                val message = editText.text.toString().trim()
                if (message.isNotEmpty()) {
                    broadcastMessage(message)
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun sendMessageToEntity(entityId: Int, message: String) {
        // Fire and Forget: Show success immediately, send in background
        Toast.makeText(this@MainActivity, getString(R.string.sent), Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            try {
                val request = mapOf<String, Any>(
                    "deviceId" to deviceManager.deviceId,
                    "entityId" to entityId,
                    "text" to message,
                    "source" to "android_main"
                )
                api.sendClientMessage(request)
                delay(500)
                loadBoundEntities()
            } catch (e: Exception) {
                Timber.e(e, "Failed to send message (fire and forget)")
            }
        }
    }

    private fun broadcastMessage(message: String) {
        // Fire and Forget: Show success immediately, send in background
        Toast.makeText(this@MainActivity, getString(R.string.broadcast_sent), Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            try {
                val entityIds = boundEntities.map { it.entityId }
                val request = mapOf<String, Any>(
                    "deviceId" to deviceManager.deviceId,
                    "entityId" to entityIds,
                    "text" to message,
                    "source" to "android_broadcast"
                )
                api.sendClientMessage(request)
                delay(500)
                loadBoundEntities()
            } catch (e: Exception) {
                Timber.e(e, "Failed to broadcast (fire and forget)")
            }
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateBindingUi(state)
                }
            }
        }
    }

    private fun updateBindingUi(state: com.hank.clawlive.ui.BindingUiState) {
        // Update chip selection
        val selectedChipId = when (state.selectedEntityId) {
            0 -> R.id.chipEntity0
            1 -> R.id.chipEntity1
            2 -> R.id.chipEntity2
            3 -> R.id.chipEntity3
            else -> R.id.chipEntity0
        }
        if (chipGroupEntity.checkedChipId != selectedChipId) {
            chipGroupEntity.check(selectedChipId)
        }

        // Loading state
        if (state.isLoading) {
            progressBar.visibility = View.VISIBLE
            btnGenerateCode.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
            btnGenerateCode.visibility = View.VISIBLE
        }

        // Binding code
        if (state.bindingCode != null) {
            tvBindingCode.text = formatBindingCode(state.bindingCode)
            tvCountdown.text = getString(R.string.expires_in, state.remainingSeconds)
            btnGenerateCode.text = getString(R.string.regenerate)
        } else {
            tvBindingCode.text = getString(R.string.code_placeholder)
            tvCountdown.text = getString(R.string.tap_to_generate_code)
            btnGenerateCode.text = getString(R.string.generate)
        }

        // Refresh entities when bound status changes
        if (state.isBound) {
            loadBoundEntities()
        }

        // Error handling
        if (state.error != null) {
            Toast.makeText(this, state.error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    private fun copyFullCommand() {
        val state = viewModel.uiState.value
        if (state.bindingCode != null) {
            val command = generateOpenClawCommand(state.bindingCode)
            copyToClipboard(command)
            Toast.makeText(this, getString(R.string.command_copied), Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateOpenClawCommand(code: String): String {
        val appVersion = deviceManager.appVersion
        return """
Connect my Claw Live wallpaper device (v$appVersion):
POST $API_BASE_URL/api/bind
Body: {"code": "$code"}

After binding, use these tools:
- update_claw_status: Update wallpaper state
- wake_up_claw: Wake up the character
- get_claw_status: Check current status
        """.trimIndent()
    }

    private fun formatBindingCode(code: String): String {
        return if (code.length == 6) {
            "${code.substring(0, 3)} ${code.substring(3)}"
        } else {
            code
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("OpenClaw Command", text)
        clipboard.setPrimaryClip(clip)
    }

    private fun openWallpaperPicker() {
        // Open custom preview page for entity positioning and background selection
        startActivity(Intent(this, WallpaperPreviewActivity::class.java))
    }
}

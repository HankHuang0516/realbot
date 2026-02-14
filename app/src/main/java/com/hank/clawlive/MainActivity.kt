package com.hank.clawlive

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
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
import com.hank.clawlive.data.local.DeviceManager
import com.hank.clawlive.data.local.EntityAvatarManager
import com.hank.clawlive.data.local.LayoutPreferences
import com.hank.clawlive.data.local.UsageManager
import com.hank.clawlive.data.model.CharacterState
import com.hank.clawlive.data.model.EntityStatus
import com.hank.clawlive.data.remote.NetworkModule
import com.hank.clawlive.data.repository.StateRepository
import com.hank.clawlive.ui.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val api = NetworkModule.api
    private val deviceManager by lazy { DeviceManager.getInstance(this) }
    private val avatarManager by lazy { EntityAvatarManager.getInstance(this) }
    private val layoutPrefs by lazy { LayoutPreferences.getInstance(this) }
    private val usageManager by lazy { UsageManager.getInstance(this) }
    private val stateRepository by lazy {
        StateRepository(NetworkModule.api, this)
    }
    companion object {
        private const val API_BASE_URL = "https://eclaw.up.railway.app"
        private const val FREE_ENTITY_LIMIT = 4
        private const val PREMIUM_ENTITY_LIMIT = 8
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
    private lateinit var btnChat: MaterialButton
    private lateinit var btnSetWallpaper: MaterialButton
    private lateinit var topBar: LinearLayout
    private lateinit var bottomActions: LinearLayout
    private lateinit var tvEntityCount: TextView

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
        btnChat = findViewById(R.id.btnChat)
        btnSetWallpaper = findViewById(R.id.btnSetWallpaper)
        topBar = findViewById(R.id.topBar)
        bottomActions = findViewById(R.id.bottomActions)
        tvEntityCount = findViewById(R.id.tvEntityCount)
    }

    private fun setupEdgeToEdgeInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { _, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )

            topBar.updatePadding(
                left = insets.left + 16.dpToPx(),
                top = insets.top + 12.dpToPx(),
                right = insets.right + 16.dpToPx()
            )

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

        btnChat.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
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
                updateEntityCount()
            } catch (e: Exception) {
                Timber.e(e, "Failed to load entities")
            }
        }
    }

    private fun updateEntityCount() {
        val maxEntities = if (usageManager.isPremium) PREMIUM_ENTITY_LIMIT else FREE_ENTITY_LIMIT
        tvEntityCount.text = getString(R.string.entity_count_format, boundEntities.size, maxEntities)
        tvEntityCount.visibility = View.VISIBLE
    }

    private fun updateAgentCards() {
        agentCardsContainer.removeAllViews()

        if (boundEntities.isEmpty()) {
            emptyStateContainer.visibility = View.VISIBLE
            // Auto-expand add entity section when empty
            if (!isAddEntityExpanded) {
                toggleAddEntitySection()
            }
        } else {
            emptyStateContainer.visibility = View.GONE

            boundEntities.forEach { entity ->
                val cardView = LayoutInflater.from(this)
                    .inflate(R.layout.item_agent_card, agentCardsContainer, false)
                bindAgentCard(cardView, entity)
                agentCardsContainer.addView(cardView)
            }
        }
    }

    private fun bindAgentCard(view: View, entity: EntityStatus) {
        // Avatar Icon (tap to change)
        val iconView = view.findViewById<TextView>(R.id.tvEntityIcon)
        iconView.text = avatarManager.getAvatar(entity.entityId)
        iconView.setOnClickListener {
            showAvatarPicker(entity.entityId, iconView)
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

        // Remove Entity Button
        view.findViewById<MaterialButton>(R.id.btnRemoveEntity).setOnClickListener {
            showRemoveConfirmDialog(entity)
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

    private fun showAvatarPicker(entityId: Int, iconView: TextView) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_avatar_picker, null)
        val grid = dialogView.findViewById<GridLayout>(R.id.avatarGrid)
        val subtitle = dialogView.findViewById<TextView>(R.id.tvAvatarSubtitle)
        subtitle.text = getString(R.string.avatar_subtitle, entityId)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val currentAvatar = avatarManager.getAvatar(entityId)

        EntityAvatarManager.AVATAR_OPTIONS.forEach { avatar ->
            val tv = TextView(this).apply {
                text = avatar
                textSize = 28f
                gravity = android.view.Gravity.CENTER
                val size = (52 * resources.displayMetrics.density).toInt()
                val margin = (4 * resources.displayMetrics.density).toInt()
                layoutParams = GridLayout.LayoutParams().apply {
                    width = size
                    height = size
                    setMargins(margin, margin, margin, margin)
                }
                setBackgroundResource(
                    if (avatar == currentAvatar) R.drawable.badge_background
                    else android.R.color.transparent
                )
                setOnClickListener {
                    avatarManager.setAvatar(entityId, avatar)
                    iconView.text = avatar
                    dialog.dismiss()
                }
            }
            grid.addView(tv)
        }

        dialog.show()
    }

    private fun showRemoveConfirmDialog(entity: EntityStatus) {
        val displayName = entity.name ?: getString(R.string.entity_format, entity.entityId)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.remove_confirm_title))
            .setMessage(getString(R.string.remove_confirm_message, displayName, entity.entityId))
            .setPositiveButton(getString(R.string.remove_entity)) { _, _ ->
                // Second confirmation
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.remove_confirm_title))
                    .setMessage("⚠️ $displayName (#${entity.entityId})")
                    .setPositiveButton(getString(R.string.remove_entity)) { _, _ ->
                        removeEntity(entity)
                    }
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun removeEntity(entity: EntityStatus) {
        lifecycleScope.launch {
            try {
                val body = mapOf<String, Any>(
                    "deviceId" to deviceManager.deviceId,
                    "deviceSecret" to deviceManager.deviceSecret,
                    "entityId" to entity.entityId
                )
                val response = api.removeEntityByDevice(body)

                if (response.success) {
                    layoutPrefs.removeRegisteredEntity(entity.entityId)
                    val displayName = entity.name ?: getString(R.string.entity_format, entity.entityId)
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.entity_removed, displayName),
                        Toast.LENGTH_SHORT
                    ).show()
                    delay(500)
                    loadBoundEntities()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.failed_format, response.message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to remove entity")
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.failed_format, e.message),
                    Toast.LENGTH_LONG
                ).show()
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
Connect my E-Claw wallpaper device (v$appVersion):
POST $API_BASE_URL/api/bind
Body: {"code": "$code"}

IMPORTANT: The response will contain a 'skills_documentation' field. You MUST read the 'E-claw MCP Skills' documentation within it to understand the interaction protocol (Matrix Architecture, Push Mode, etc.).

After binding, use these tools:
- update_claw_status: Update wallpaper state
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
        startActivity(Intent(this, WallpaperPreviewActivity::class.java))
    }
}

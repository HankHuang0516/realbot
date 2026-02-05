package com.hank.clawlive

import android.app.WallpaperManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.card.MaterialCardView
import com.hank.clawlive.service.ClawWallpaperService
import com.hank.clawlive.ui.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    companion object {
        private const val API_BASE_URL = "https://realbot-production.up.railway.app"
    }

    // UI elements
    private lateinit var tvBindingCode: TextView
    private lateinit var tvCountdown: TextView
    private lateinit var btnGenerateCode: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var cardCommand: MaterialCardView
    private lateinit var tvOpenClawCommand: TextView
    private lateinit var btnCopyCommand: Button
    private lateinit var cardBindingStatus: MaterialCardView
    private lateinit var tvBindingStatusIcon: TextView
    private lateinit var tvBindingStatusTitle: TextView
    private lateinit var tvBindingStatusDesc: TextView
    private lateinit var tvInstructionsTitle: TextView
    private lateinit var tvInstructions: TextView
    private lateinit var cardStatus: MaterialCardView
    private lateinit var tvStatusIcon: TextView
    private lateinit var tvStatusState: TextView
    private lateinit var tvStatusMessage: TextView
    private lateinit var btnSetWallpaper: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupClickListeners()
        observeUiState()
    }

    private fun initViews() {
        tvBindingCode = findViewById(R.id.tvBindingCode)
        tvCountdown = findViewById(R.id.tvCountdown)
        btnGenerateCode = findViewById(R.id.btnGenerateCode)
        progressBar = findViewById(R.id.progressBar)
        cardCommand = findViewById(R.id.cardCommand)
        tvOpenClawCommand = findViewById(R.id.tvOpenClawCommand)
        btnCopyCommand = findViewById(R.id.btnCopyCommand)
        cardBindingStatus = findViewById(R.id.cardBindingStatus)
        tvBindingStatusIcon = findViewById(R.id.tvBindingStatusIcon)
        tvBindingStatusTitle = findViewById(R.id.tvBindingStatusTitle)
        tvBindingStatusDesc = findViewById(R.id.tvBindingStatusDesc)
        tvInstructionsTitle = findViewById(R.id.tvInstructionsTitle)
        tvInstructions = findViewById(R.id.tvInstructions)
        cardStatus = findViewById(R.id.cardStatus)
        tvStatusIcon = findViewById(R.id.tvStatusIcon)
        tvStatusState = findViewById(R.id.tvStatusState)
        tvStatusMessage = findViewById(R.id.tvStatusMessage)
        btnSetWallpaper = findViewById(R.id.btnSetWallpaper)
    }

    private fun setupClickListeners() {
        btnGenerateCode.setOnClickListener {
            viewModel.generateBindingCode()
        }

        // Tap code to copy full command
        tvBindingCode.setOnClickListener {
            copyFullCommand()
        }

        // Copy command button
        btnCopyCommand.setOnClickListener {
            copyFullCommand()
        }

        btnSetWallpaper.setOnClickListener {
            openWallpaperPicker()
        }
    }

    private fun copyFullCommand() {
        val state = viewModel.uiState.value
        if (state.bindingCode != null) {
            val command = generateOpenClawCommand(state.bindingCode)
            copyToClipboard(command)
            Toast.makeText(this, "Command copied! Paste to OpenClaw", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateOpenClawCommand(code: String): String {
        return """
Connect my Claw Live wallpaper device:
POST $API_BASE_URL/api/bind
Body: {"code": "$code"}

After binding, use these tools:
- update_claw_status: Update wallpaper state
- wake_up_claw: Wake up the character
- get_claw_status: Check current status
        """.trimIndent()
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUi(state)
                }
            }
        }
    }

    private fun updateUi(state: com.hank.clawlive.ui.BindingUiState) {
        // Loading state
        if (state.isLoading) {
            progressBar.visibility = View.VISIBLE
            btnGenerateCode.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
            btnGenerateCode.visibility = View.VISIBLE
        }

        // Binding code and command
        if (state.bindingCode != null) {
            tvBindingCode.text = formatBindingCode(state.bindingCode)
            tvCountdown.text = "Expires in ${state.remainingSeconds}s"
            btnGenerateCode.text = "Regenerate"

            // Show command card
            cardCommand.visibility = View.VISIBLE
            tvOpenClawCommand.text = generateOpenClawCommand(state.bindingCode)
        } else {
            tvBindingCode.text = "------"
            tvCountdown.text = "Tap to generate code"
            btnGenerateCode.text = "Generate Code"
            cardCommand.visibility = View.GONE
        }

        // Binding status
        if (state.isBound && state.agentStatus != null) {
            // Connected
            tvBindingStatusIcon.text = "🟢"
            tvBindingStatusTitle.text = "Connected"
            tvBindingStatusDesc.text = "OpenClaw is controlling your wallpaper"
            tvInstructionsTitle.visibility = View.GONE
            tvInstructions.visibility = View.GONE

            // Show status card
            cardStatus.visibility = View.VISIBLE
            tvStatusIcon.text = when (state.agentStatus.character.name) {
                "PIG" -> "🐷"
                else -> "🦞"
            }
            tvStatusState.text = state.agentStatus.state.name
            tvStatusMessage.text = state.agentStatus.message

            tvStatusState.setTextColor(
                when (state.agentStatus.state.name) {
                    "EXCITED" -> getColor(android.R.color.holo_orange_dark)
                    "BUSY" -> getColor(android.R.color.holo_blue_dark)
                    "SLEEPING" -> getColor(android.R.color.darker_gray)
                    "EATING" -> getColor(android.R.color.holo_green_dark)
                    else -> getColor(android.R.color.black)
                }
            )
        } else if (state.bindingCode != null) {
            // Waiting for binding
            tvBindingStatusIcon.text = "🟡"
            tvBindingStatusTitle.text = "Waiting for OpenClaw"
            tvBindingStatusDesc.text = "Copy the command below and paste to OpenClaw"
            tvInstructionsTitle.visibility = View.VISIBLE
            tvInstructions.visibility = View.VISIBLE
            cardStatus.visibility = View.GONE
        } else {
            // Not connected
            tvBindingStatusIcon.text = "⚪"
            tvBindingStatusTitle.text = "Not Connected"
            tvBindingStatusDesc.text = "Generate a code to connect OpenClaw"
            tvInstructionsTitle.visibility = View.VISIBLE
            tvInstructions.visibility = View.VISIBLE
            cardStatus.visibility = View.GONE
        }

        // Error handling
        if (state.error != null) {
            Toast.makeText(this, state.error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
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
        try {
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
            intent.putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(this, ClawWallpaperService::class.java)
            )
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to open wallpaper picker", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
}

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

    // UI elements
    private lateinit var tvBindingCode: TextView
    private lateinit var tvCountdown: TextView
    private lateinit var btnGenerateCode: Button
    private lateinit var progressBar: ProgressBar
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

        // Long press to copy code
        tvBindingCode.setOnLongClickListener {
            val code = tvBindingCode.text.toString()
            if (code != "------" && code.length == 6) {
                copyToClipboard(code)
                Toast.makeText(this, "Code copied!", Toast.LENGTH_SHORT).show()
            }
            true
        }

        // Tap code to copy
        tvBindingCode.setOnClickListener {
            val code = tvBindingCode.text.toString()
            if (code != "------" && code.length == 6) {
                copyToClipboard(code)
                Toast.makeText(this, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
            }
        }

        btnSetWallpaper.setOnClickListener {
            openWallpaperPicker()
        }
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

        // Binding code
        if (state.bindingCode != null) {
            tvBindingCode.text = formatBindingCode(state.bindingCode)
            tvCountdown.text = "Expires in ${state.remainingSeconds}s • Tap to copy"
            btnGenerateCode.text = "Regenerate Code"
        } else {
            tvBindingCode.text = "------"
            tvCountdown.text = "Tap to generate code"
            btnGenerateCode.text = "Generate Code"
        }

        // Status card
        if (state.agentStatus != null) {
            cardStatus.visibility = View.VISIBLE
            tvStatusIcon.text = when (state.agentStatus.character.name) {
                "PIG" -> "🐷"
                else -> "🦞"
            }
            tvStatusState.text = state.agentStatus.state.name
            tvStatusMessage.text = state.agentStatus.message

            // Color based on state
            tvStatusState.setTextColor(
                when (state.agentStatus.state.name) {
                    "EXCITED" -> getColor(android.R.color.holo_orange_dark)
                    "BUSY" -> getColor(android.R.color.holo_blue_dark)
                    "SLEEPING" -> getColor(android.R.color.darker_gray)
                    "EATING" -> getColor(android.R.color.holo_green_dark)
                    else -> getColor(android.R.color.black)
                }
            )
        } else {
            cardStatus.visibility = View.GONE
        }

        // Error handling
        if (state.error != null) {
            Toast.makeText(this, state.error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    private fun formatBindingCode(code: String): String {
        // Format as "123 456" for readability
        return if (code.length == 6) {
            "${code.substring(0, 3)} ${code.substring(3)}"
        } else {
            code
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Binding Code", text)
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

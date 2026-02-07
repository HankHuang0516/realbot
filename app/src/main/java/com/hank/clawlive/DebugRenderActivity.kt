package com.hank.clawlive

import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.hank.clawlive.data.local.DeviceManager
import com.hank.clawlive.data.local.LayoutPreferences
import com.hank.clawlive.data.remote.NetworkModule
import com.hank.clawlive.ui.LayoutEditorView
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Layout Editor Activity - allows users to drag entities to custom positions.
 * Replaces the old DebugRenderActivity.
 */
class DebugRenderActivity : AppCompatActivity() {

    private lateinit var layoutEditorView: LayoutEditorView
    private lateinit var switchCustomLayout: MaterialSwitch
    private lateinit var btnReset: MaterialButton
    private lateinit var btnDone: MaterialButton
    private lateinit var btnBack: ImageButton
    private lateinit var topBar: LinearLayout
    private lateinit var bottomControls: LinearLayout

    private val api = NetworkModule.api
    private val deviceManager by lazy { DeviceManager.getInstance(this) }
    private val layoutPrefs by lazy { LayoutPreferences.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_layout_editor)

        initViews()
        setupEdgeToEdgeInsets()
        setupListeners()
        loadBoundEntities()
    }

    private fun setupEdgeToEdgeInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { _, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )

            // Apply top inset to top bar (status bar + cutout)
            topBar.updatePadding(
                left = insets.left + dpToPx(16),
                top = insets.top + dpToPx(16),
                right = insets.right + dpToPx(16)
            )

            // Apply bottom inset to bottom controls (navigation bar)
            bottomControls.updatePadding(
                left = insets.left + dpToPx(16),
                right = insets.right + dpToPx(16),
                bottom = insets.bottom + dpToPx(16)
            )

            WindowInsetsCompat.CONSUMED
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun initViews() {
        layoutEditorView = findViewById(R.id.layoutEditorView)
        switchCustomLayout = findViewById(R.id.switchCustomLayout)
        btnReset = findViewById(R.id.btnReset)
        btnDone = findViewById(R.id.btnDone)
        btnBack = findViewById(R.id.btnBack)
        topBar = findViewById(R.id.topBar)
        bottomControls = findViewById(R.id.bottomControls)

        // Initialize switch state from preferences
        switchCustomLayout.isChecked = layoutPrefs.useCustomLayout
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnDone.setOnClickListener {
            // Save and exit
            Toast.makeText(this, "Layout saved", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnReset.setOnClickListener {
            // Reset positions to default
            layoutEditorView.resetPositions()
            Toast.makeText(this, "Positions reset", Toast.LENGTH_SHORT).show()
        }

        switchCustomLayout.setOnCheckedChangeListener { _, isChecked ->
            layoutPrefs.useCustomLayout = isChecked
            val message = if (isChecked) "Custom layout enabled" else "Using preset layout"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadBoundEntities() {
        lifecycleScope.launch {
            try {
                val response = api.getAllEntities(deviceId = deviceManager.deviceId)

                // Filter to only bound entities (API returns EntityStatus directly)
                val boundEntities = response.entities.filter { it.isBound }

                Timber.d("Loaded ${boundEntities.size} bound entities for layout editor")

                if (boundEntities.isEmpty()) {
                    Toast.makeText(
                        this@DebugRenderActivity,
                        "No bound entities. Bind an entity first.",
                        Toast.LENGTH_LONG
                    ).show()
                }

                layoutEditorView.setEntities(boundEntities)

            } catch (e: Exception) {
                Timber.e(e, "Failed to load entities")
                Toast.makeText(
                    this@DebugRenderActivity,
                    "Failed to load entities: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

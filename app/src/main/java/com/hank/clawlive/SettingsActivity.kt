package com.hank.clawlive

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
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
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.hank.clawlive.billing.BillingManager
import com.hank.clawlive.billing.SubscriptionState
import com.hank.clawlive.data.local.DeviceManager
import com.hank.clawlive.data.local.LayoutPreferences
import com.hank.clawlive.data.local.UsageManager
import com.hank.clawlive.data.model.FeedbackResponse
import com.hank.clawlive.data.remote.NetworkModule
import com.hank.clawlive.data.remote.TelemetryHelper
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.launch
import timber.log.Timber

class SettingsActivity : AppCompatActivity() {

    private lateinit var billingManager: BillingManager
    private lateinit var usageManager: UsageManager
    private val layoutPrefs: LayoutPreferences by lazy { LayoutPreferences.getInstance(this) }
    private val deviceManager: DeviceManager by lazy { DeviceManager.getInstance(this) }

    // UI elements
    private lateinit var cardSubscription: MaterialCardView
    private lateinit var layoutPremiumBadge: LinearLayout
    private lateinit var tvUsageCount: TextView
    private lateinit var tvEntityCount: TextView
    private lateinit var progressUsage: ProgressBar
    private lateinit var btnSubscribe: MaterialButton
    private lateinit var btnFeedback: LinearLayout
    private lateinit var btnMarkBug: MaterialButton
    private lateinit var tvMarkStatus: TextView
    private lateinit var btnPrivacyPolicy: MaterialButton
    private lateinit var btnWebPortal: MaterialButton
    private lateinit var tvViewFeedbackHistory: TextView
    private lateinit var chipGroupLanguage: ChipGroup
    private lateinit var chipLangEn: Chip
    private lateinit var chipLangZh: Chip
    private lateinit var btnBack: ImageButton
    private lateinit var topBar: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_settings)

        billingManager = BillingManager.getInstance(this)
        usageManager = UsageManager.getInstance(this)

        initViews()
        setupEdgeToEdgeInsets()
        setupClickListeners()
        loadCurrentLanguage()
        observeSubscriptionState()
        updateEntityCount()
        displayAppVersion()
    }

    private fun setupEdgeToEdgeInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { _, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )

            topBar.updatePadding(
                left = insets.left + dpToPx(8),
                top = insets.top + dpToPx(8),
                right = insets.right + dpToPx(8)
            )

            WindowInsetsCompat.CONSUMED
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onResume() {
        super.onResume()
        TelemetryHelper.trackPageView(this, "settings")
        billingManager.refreshState()
        updateUsageDisplay()
        updateEntityCount()
    }

    private fun initViews() {
        cardSubscription = findViewById(R.id.cardSubscription)
        layoutPremiumBadge = findViewById(R.id.layoutPremiumBadge)
        tvUsageCount = findViewById(R.id.tvUsageCount)
        progressUsage = findViewById(R.id.progressUsage)
        btnSubscribe = findViewById(R.id.btnSubscribe)
        chipGroupLanguage = findViewById(R.id.chipGroupLanguage)
        chipLangEn = findViewById(R.id.chipLangEn)
        chipLangZh = findViewById(R.id.chipLangZh)
        btnBack = findViewById(R.id.btnBack)
        topBar = findViewById(R.id.topBar)
        tvEntityCount = findViewById(R.id.tvEntityCount)
        btnFeedback = findViewById(R.id.btnFeedback)
        btnMarkBug = findViewById(R.id.btnMarkBug)
        tvMarkStatus = findViewById(R.id.tvMarkStatus)
        btnPrivacyPolicy = findViewById(R.id.btnPrivacyPolicy)
        btnWebPortal = findViewById(R.id.btnWebPortal)
        tvViewFeedbackHistory = findViewById(R.id.tvViewFeedbackHistory)
    }

    private fun setupClickListeners() {
        btnSubscribe.setOnClickListener {
            billingManager.launchPurchaseFlow(this)
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnFeedback.setOnClickListener {
            showFeedbackDialog()
        }

        btnMarkBug.setOnClickListener {
            markBugMoment()
        }

        btnPrivacyPolicy.setOnClickListener {
            startActivity(android.content.Intent(this, PrivacyPolicyActivity::class.java))
        }

        btnWebPortal.setOnClickListener {
            showWebPortalDialog()
        }

        tvViewFeedbackHistory.setOnClickListener {
            startActivity(Intent(this, FeedbackHistoryActivity::class.java))
        }

        // Language selection
        chipGroupLanguage.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val localeList = when (checkedIds[0]) {
                    R.id.chipLangZh -> LocaleListCompat.forLanguageTags("zh-TW")
                    else -> LocaleListCompat.forLanguageTags("en")
                }

                val current = AppCompatDelegate.getApplicationLocales()
                if (current.toLanguageTags() != localeList.toLanguageTags()) {
                     AppCompatDelegate.setApplicationLocales(localeList)
                }
            }
        }
    }

    private fun loadCurrentLanguage() {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (!locales.isEmpty) {
            // App has a specific locale set
            val tag = locales.toLanguageTags()
            if (tag.contains("zh")) {
                chipLangZh.isChecked = true
            } else {
                chipLangEn.isChecked = true
            }
        } else {
            // Follow system: Check system locale
            val systemLocale = LocaleListCompat.getDefault()
            val systemTag = systemLocale.toLanguageTags()
            if (systemTag.contains("zh")) {
                chipLangZh.isChecked = true
            } else {
                chipLangEn.isChecked = true
            }
        }
    }

    private fun updateUsageDisplay() {
        tvUsageCount.text = usageManager.getUsageDisplay()
        progressUsage.progress = (usageManager.getUsageProgress() * 100).toInt()

        // Change progress bar color when limit reached
        if (!usageManager.canUseMessage() && !usageManager.isPremium) {
            progressUsage.progressTintList = getColorStateList(android.R.color.holo_red_light)
        } else {
            progressUsage.progressTintList = android.content.res.ColorStateList.valueOf(0xFFFFD23F.toInt())
        }
    }

    private fun observeSubscriptionState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                billingManager.subscriptionState.collect { state ->
                    updateSubscriptionUi(state)
                }
            }
        }
    }

    private fun updateSubscriptionUi(state: SubscriptionState) {
        // Update usage display
        tvUsageCount.text = state.usageDisplay
        progressUsage.progress = (state.usageProgress * 100).toInt()

        // Show/hide premium badge and subscribe button
        if (state.isPremium) {
            layoutPremiumBadge.visibility = View.VISIBLE
            btnSubscribe.visibility = View.GONE
        } else {
            layoutPremiumBadge.visibility = View.GONE
            btnSubscribe.visibility = View.VISIBLE

            // Update button text with price if available
            if (state.subscriptionPrice.isNotEmpty()) {
                btnSubscribe.text = getString(R.string.unlock_unlimited, state.subscriptionPrice)
            }
        }

        // Change progress bar color when limit reached
        if (state.isLimitReached) {
            progressUsage.progressTintList = getColorStateList(android.R.color.holo_red_light)
        } else {
            progressUsage.progressTintList = android.content.res.ColorStateList.valueOf(0xFFFFD23F.toInt())
        }
    }

    private fun updateEntityCount() {
        val maxEntities = if (usageManager.isPremium) 8 else 4
        // Show local count first, then update from API
        val localCount = layoutPrefs.getRegisteredEntityIds().size
        tvEntityCount.text = "$localCount/$maxEntities"

        lifecycleScope.launch {
            try {
                val response = NetworkModule.api.getAllEntities(deviceId = deviceManager.deviceId)
                val boundCount = response.entities.size
                tvEntityCount.text = "$boundCount/$maxEntities"
            } catch (e: Exception) {
                Timber.e(e, "Failed to fetch entity count from API")
            }
        }
    }

    private fun showWebPortalDialog() {
        val deviceId = deviceManager.deviceId
        val deviceSecret = deviceManager.deviceSecret
        val portalUrl = "https://eclaw.up.railway.app/portal/"

        val msg = "Device ID:\n$deviceId\n\n" +
            "Device Secret:\n$deviceSecret\n\n" +
            "Portal URL:\n$portalUrl"

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.web_portal))
            .setMessage(msg)
            .setPositiveButton("Open Portal") { _, _ ->
                startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(portalUrl)))
            }
            .setNeutralButton("Copy Credentials") { _, _ ->
                val clip = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                clip.setPrimaryClip(ClipData.newPlainText("credentials",
                    "Device ID: $deviceId\nDevice Secret: $deviceSecret"))
                Toast.makeText(this, getString(R.string.confirm_copy), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private var feedbackCategory = "bug"

    private fun showFeedbackDialog() {
        val dialogBg = 0xFF1A1A1A.toInt()
        val cardBg = 0xFF2A2A2A.toInt()
        val borderColor = 0xFF444444.toInt()
        val accentColor = 0xFFFFD23F.toInt()
        val textPrimary = 0xFFFFFFFF.toInt()
        val textSecondary = 0xFF999999.toInt()

        feedbackCategory = "bug"

        // Root container
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(dialogBg)
            setPadding(dpToPx(20), dpToPx(16), dpToPx(20), dpToPx(16))
        }

        // Title
        val title = TextView(this).apply {
            text = getString(R.string.feedback)
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor(textPrimary)
        }
        root.addView(title)

        // Description
        val descText = TextView(this).apply {
            text = getString(R.string.feedback_auto_log_hint)
            setTextColor(textSecondary)
            textSize = 13f
            setPadding(0, dpToPx(8), 0, dpToPx(12))
        }
        root.addView(descText)

        // Category label
        val catLabel = TextView(this).apply {
            text = getString(R.string.feedback_category_label)
            setTextColor(textSecondary)
            textSize = 12f
            setPadding(0, 0, 0, dpToPx(8))
        }
        root.addView(catLabel)

        // Category buttons row
        val catRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 0, 0, dpToPx(16))
        }

        val categories = listOf(
            "bug" to getString(R.string.feedback_cat_bug),
            "feature" to getString(R.string.feedback_cat_feature),
            "question" to getString(R.string.feedback_cat_question)
        )

        val categoryButtons = mutableListOf<TextView>()

        fun updateCategoryButtons(selected: String) {
            feedbackCategory = selected
            categoryButtons.forEach { btn ->
                val isSelected = btn.tag == selected
                val bg = GradientDrawable().apply {
                    cornerRadius = dpToPx(20).toFloat()
                    if (isSelected) {
                        setColor(accentColor)
                        setStroke(dpToPx(1), accentColor)
                    } else {
                        setColor(cardBg)
                        setStroke(dpToPx(1), borderColor)
                    }
                }
                btn.background = bg
                btn.setTextColor(if (isSelected) 0xFF000000.toInt() else textSecondary)
                btn.setTypeface(null, if (isSelected) Typeface.BOLD else Typeface.NORMAL)
            }
        }

        categories.forEach { (value, label) ->
            val btn = TextView(this).apply {
                text = label
                tag = value
                textSize = 14f
                gravity = Gravity.CENTER
                setPadding(dpToPx(18), dpToPx(8), dpToPx(18), dpToPx(8))
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.marginEnd = dpToPx(8)
                layoutParams = lp
                setOnClickListener { updateCategoryButtons(value) }
            }
            categoryButtons.add(btn)
            catRow.addView(btn)
        }
        root.addView(catRow)
        updateCategoryButtons("bug")

        // Text input with dark styling
        val inputLayout = TextInputLayout(this).apply {
            hint = getString(R.string.feedback_hint)
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            boxStrokeColor = borderColor
            boxBackgroundColor = cardBg
            setHintTextColor(android.content.res.ColorStateList.valueOf(textSecondary))
            boxStrokeWidth = dpToPx(1)
        }
        val input = TextInputEditText(this).apply {
            maxLines = 5
            minLines = 3
            setTextColor(textPrimary)
        }
        inputLayout.addView(input)
        root.addView(inputLayout)

        // Result container (hidden initially)
        val resultContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
            val bg = GradientDrawable().apply {
                cornerRadius = dpToPx(8).toFloat()
                setColor(0xFF222222.toInt())
                setStroke(dpToPx(1), 0xFF333333.toInt())
            }
            background = bg
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.topMargin = dpToPx(12)
            layoutParams = lp
        }
        root.addView(resultContainer)

        val scrollView = ScrollView(this).apply {
            addView(root)
            setBackgroundColor(dialogBg)
        }

        val dialog = AlertDialog.Builder(this, R.style.Theme_ClawLiveCompanion_Dialog)
            .setView(scrollView)
            .setPositiveButton(R.string.send, null)
            .setNegativeButton(R.string.cancel, null)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.setOnShowListener {
            val posBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            posBtn.setTextColor(accentColor)
            posBtn.setTypeface(null, Typeface.BOLD)
            negBtn.setTextColor(textSecondary)

            posBtn.setOnClickListener {
                val message = input.text?.toString()?.trim() ?: ""
                if (message.isEmpty()) {
                    Toast.makeText(this, R.string.feedback_empty, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                posBtn.isEnabled = false
                posBtn.text = getString(R.string.feedback_sending)
                sendFeedback(message, feedbackCategory, dialog, resultContainer, posBtn)
            }
        }

        dialog.show()
    }

    private fun sendFeedback(
        message: String,
        category: String,
        dialog: AlertDialog,
        resultContainer: LinearLayout,
        sendBtn: android.widget.Button
    ) {
        val accentColor = 0xFFFFD23F.toInt()
        val textPrimary = 0xFFFFFFFF.toInt()
        val textSecondary = 0xFF999999.toInt()

        lifecycleScope.launch {
            try {
                val body = mapOf(
                    "deviceId" to deviceManager.deviceId,
                    "deviceSecret" to deviceManager.deviceSecret,
                    "message" to message,
                    "category" to category,
                    "appVersion" to (packageManager.getPackageInfo(packageName, 0).versionName ?: "")
                )
                val result = NetworkModule.api.sendFeedback(body)
                TelemetryHelper.trackAction("send_feedback", mapOf("category" to category))

                // Hide mark status after successful submit
                tvMarkStatus.visibility = View.GONE

                // Show result in dialog
                showFeedbackResult(result, resultContainer, dialog, sendBtn)

            } catch (e: Exception) {
                Timber.e(e, "Failed to send feedback")
                TelemetryHelper.trackError(e, mapOf("action" to "send_feedback"))
                Toast.makeText(this@SettingsActivity, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                sendBtn.isEnabled = true
                sendBtn.text = getString(R.string.send)
            }
        }
    }

    private fun showFeedbackResult(
        result: FeedbackResponse,
        container: LinearLayout,
        dialog: AlertDialog,
        sendBtn: android.widget.Button
    ) {
        val accentColor = 0xFFFFD23F.toInt()
        val textPrimary = 0xFFFFFFFF.toInt()
        val textSecondary = 0xFFAAAAAA.toInt()
        val successGreen = 0xFF4CAF50.toInt()

        container.removeAllViews()
        container.visibility = View.VISIBLE

        // Success header
        val header = TextView(this).apply {
            text = getString(R.string.feedback_result_title, result.feedbackId ?: 0)
            textSize = 15f
            setTypeface(null, Typeface.BOLD)
            setTextColor(successGreen)
        }
        container.addView(header)

        // Severity
        val severityText = TextView(this).apply {
            val severityColor = when (result.severity) {
                "critical" -> 0xFFF44336.toInt()
                "high" -> 0xFFFF9800.toInt()
                "medium" -> 0xFFFFC107.toInt()
                else -> 0xFF4CAF50.toInt()
            }
            text = getString(R.string.feedback_result_severity, result.severity ?: "low")
            textSize = 13f
            setTextColor(severityColor)
            setPadding(0, dpToPx(4), 0, 0)
        }
        container.addView(severityText)

        // Logs captured
        val logs = result.logsCaptured
        if (logs != null) {
            val logsText = TextView(this).apply {
                text = getString(R.string.feedback_result_logs, logs.telemetry, logs.serverLogs)
                textSize = 12f
                setTextColor(textSecondary)
                setPadding(0, dpToPx(4), 0, 0)
            }
            container.addView(logsText)
        }

        // Tags
        if (!result.tags.isNullOrEmpty()) {
            val tagsRow = HorizontalScrollView(this).apply {
                setPadding(0, dpToPx(6), 0, 0)
                isHorizontalScrollBarEnabled = false
            }
            val tagsContainer = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            result.tags.forEach { tag ->
                val tagView = TextView(this).apply {
                    text = tag
                    textSize = 11f
                    setTextColor(accentColor)
                    setPadding(dpToPx(8), dpToPx(3), dpToPx(8), dpToPx(3))
                    val bg = GradientDrawable().apply {
                        cornerRadius = dpToPx(10).toFloat()
                        setStroke(dpToPx(1), accentColor)
                        setColor(0x33FFD23F)
                    }
                    background = bg
                    val lp = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    lp.marginEnd = dpToPx(6)
                    layoutParams = lp
                }
                tagsContainer.addView(tagView)
            }
            tagsRow.addView(tagsContainer)
            container.addView(tagsRow)
        }

        // Diagnosis
        if (!result.diagnosis.isNullOrBlank()) {
            val diagText = TextView(this).apply {
                text = result.diagnosis
                textSize = 12f
                setTextColor(textSecondary)
                setPadding(0, dpToPx(8), 0, 0)
            }
            container.addView(diagText)
        }

        // GitHub issue link
        if (result.githubIssue != null) {
            val issueLink = TextView(this).apply {
                text = getString(R.string.feedback_result_issue, result.githubIssue.number)
                textSize = 13f
                setTextColor(accentColor)
                setPadding(0, dpToPx(8), 0, 0)
                paintFlags = paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
                setOnClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(result.githubIssue.url)))
                }
            }
            container.addView(issueLink)
        }

        // View all feedback link
        val viewAllLink = TextView(this).apply {
            text = getString(R.string.feedback_view_all)
            textSize = 13f
            setTextColor(accentColor)
            setPadding(0, dpToPx(8), 0, 0)
            paintFlags = paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
            setOnClickListener {
                dialog.dismiss()
                startActivity(Intent(this@SettingsActivity, FeedbackHistoryActivity::class.java))
            }
        }
        container.addView(viewAllLink)

        // Update dialog buttons
        sendBtn.text = getString(R.string.feedback_sent_short)
        sendBtn.isEnabled = false
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).text = getString(R.string.done)
    }

    private fun markBugMoment() {
        lifecycleScope.launch {
            try {
                val body = mapOf("deviceId" to deviceManager.deviceId)
                NetworkModule.api.markFeedback(body)
                tvMarkStatus.visibility = View.VISIBLE
                tvMarkStatus.text = getString(R.string.feedback_marked,
                    java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                        .format(java.util.Date()))
                btnMarkBug.text = getString(R.string.feedback_marked_btn)
                btnMarkBug.postDelayed({
                    btnMarkBug.text = getString(R.string.feedback_mark)
                }, 5000)
                TelemetryHelper.trackAction("mark_bug_moment")
            } catch (e: Exception) {
                Timber.e(e, "Failed to mark bug moment")
                TelemetryHelper.trackError(e, mapOf("action" to "mark_bug_moment"))
                Toast.makeText(this@SettingsActivity, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayAppVersion() {
        try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            val version = pInfo.versionName
            val tvAppVersion = findViewById<TextView>(R.id.tvAppVersion)
            tvAppVersion.text = getString(R.string.app_version, version)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

package com.hank.clawlive

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.hank.clawlive.billing.BillingManager
import com.hank.clawlive.billing.SubscriptionState
import com.hank.clawlive.data.local.LayoutPreferences
import com.hank.clawlive.data.local.UsageManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var billingManager: BillingManager
    private lateinit var usageManager: UsageManager
    private lateinit var layoutPrefs: LayoutPreferences

    // UI elements
    private lateinit var cardSubscription: MaterialCardView
    private lateinit var layoutPremiumBadge: LinearLayout
    private lateinit var tvUsageCount: TextView
    private lateinit var progressUsage: ProgressBar
    private lateinit var btnSubscribe: MaterialButton
    private lateinit var chipGroupLayout: ChipGroup
    private lateinit var chipLayoutSingle: Chip
    private lateinit var chipLayoutDual: Chip
    private lateinit var chipLayoutQuad: Chip
    private lateinit var tvLayoutDescription: TextView
    private lateinit var chipGroupLanguage: ChipGroup
    private lateinit var chipLangSystem: Chip
    private lateinit var chipLangEn: Chip
    private lateinit var chipLangZh: Chip
    private lateinit var btnEntityManager: MaterialButton
    private lateinit var btnDebugRender: MaterialButton
    private lateinit var btnBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        billingManager = BillingManager.getInstance(this)
        usageManager = UsageManager.getInstance(this)
        layoutPrefs = LayoutPreferences.getInstance(this)

        initViews()
        setupClickListeners()
        loadCurrentLayout()
        loadCurrentLanguage()
        observeSubscriptionState()
    }

    override fun onResume() {
        super.onResume()
        billingManager.refreshState()
        updateUsageDisplay()
    }

    private fun initViews() {
        cardSubscription = findViewById(R.id.cardSubscription)
        layoutPremiumBadge = findViewById(R.id.layoutPremiumBadge)
        tvUsageCount = findViewById(R.id.tvUsageCount)
        progressUsage = findViewById(R.id.progressUsage)
        btnSubscribe = findViewById(R.id.btnSubscribe)
        chipGroupLayout = findViewById(R.id.chipGroupLayout)
        chipLayoutSingle = findViewById(R.id.chipLayoutSingle)
        chipLayoutDual = findViewById(R.id.chipLayoutDual)
        chipLayoutQuad = findViewById(R.id.chipLayoutQuad)
        tvLayoutDescription = findViewById(R.id.tvLayoutDescription)
        chipGroupLanguage = findViewById(R.id.chipGroupLanguage)
        chipLangSystem = findViewById(R.id.chipLangSystem)
        chipLangEn = findViewById(R.id.chipLangEn)
        chipLangZh = findViewById(R.id.chipLangZh)
        btnEntityManager = findViewById(R.id.btnEntityManager)
        btnDebugRender = findViewById(R.id.btnDebugRender)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setupClickListeners() {
        btnSubscribe.setOnClickListener {
            billingManager.launchPurchaseFlow(this)
        }

        btnEntityManager.setOnClickListener {
            startActivity(Intent(this, EntityManagerActivity::class.java))
        }

        btnDebugRender.setOnClickListener {
            startActivity(Intent(this, DebugRenderActivity::class.java))
        }

        btnBack.setOnClickListener {
            finish()
        }

        // Layout selection
        chipGroupLayout.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                when (checkedIds[0]) {
                    R.id.chipLayoutSingle -> {
                        layoutPrefs.displayMode = LayoutPreferences.MODE_SINGLE
                        tvLayoutDescription.text = getString(R.string.desc_single)
                    }
                    R.id.chipLayoutDual -> {
                        layoutPrefs.displayMode = LayoutPreferences.MODE_DUAL
                        tvLayoutDescription.text = getString(R.string.desc_dual)
                    }
                    R.id.chipLayoutQuad -> {
                        layoutPrefs.displayMode = LayoutPreferences.MODE_QUAD
                        tvLayoutDescription.text = getString(R.string.desc_quad)
                    }
                }
            }
        }

        // Language selection
        chipGroupLanguage.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val localeList = when (checkedIds[0]) {
                    R.id.chipLangEn -> LocaleListCompat.forLanguageTags("en")
                    R.id.chipLangZh -> LocaleListCompat.forLanguageTags("zh-TW")
                    else -> LocaleListCompat.getEmptyLocaleList() // System Default
                }
                
                // Only set if different to avoid loop/unnecessary recreation? 
                // AppCompatDelegate handles check internally, but we can check.
                val current = AppCompatDelegate.getApplicationLocales()
                if (current.toLanguageTags() != localeList.toLanguageTags()) {
                     AppCompatDelegate.setApplicationLocales(localeList)
                }
            }
        }
    }

    private fun loadCurrentLayout() {
        val mode = layoutPrefs.displayMode
        when (mode) {
            LayoutPreferences.MODE_SINGLE -> {
                chipLayoutSingle.isChecked = true
                tvLayoutDescription.text = getString(R.string.desc_single)
            }
            LayoutPreferences.MODE_DUAL -> {
                chipLayoutDual.isChecked = true
                tvLayoutDescription.text = getString(R.string.desc_dual)
            }
            LayoutPreferences.MODE_QUAD -> {
                chipLayoutQuad.isChecked = true
                tvLayoutDescription.text = getString(R.string.desc_quad)
            }
        }
    }

    private fun loadCurrentLanguage() {
        val locales = AppCompatDelegate.getApplicationLocales()
        val tag = locales.toLanguageTags()
        when {
            tag.startsWith("en") -> chipLangEn.isChecked = true
            tag.startsWith("zh") -> chipLangZh.isChecked = true
            else -> chipLangSystem.isChecked = true
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
}

package com.hank.clawlive

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.hank.clawlive.billing.BillingManager
import com.hank.clawlive.data.local.DeviceManager
import com.hank.clawlive.data.local.LayoutPreferences
import com.hank.clawlive.data.model.BorrowBinding
import com.hank.clawlive.data.model.OfficialBorrowStatusResponse
import com.hank.clawlive.data.remote.NetworkModule
import kotlinx.coroutines.launch
import timber.log.Timber

class OfficialBorrowActivity : AppCompatActivity() {

    private val api = NetworkModule.api
    private val deviceManager by lazy { DeviceManager.getInstance(this) }
    private val layoutPrefs by lazy { LayoutPreferences.getInstance(this) }
    private val billingManager by lazy { BillingManager.getInstance(this) }

    private lateinit var chipGroupEntity: ChipGroup
    private lateinit var progressLoading: ProgressBar
    private lateinit var btnBindFree: MaterialButton
    private lateinit var btnBindPersonal: MaterialButton
    private lateinit var tvAvailability: TextView
    private lateinit var tvPersonalPrice: TextView

    private var selectedEntityId = 0
    private var borrowStatus: OfficialBorrowStatusResponse? = null
    private var pendingBindEntityId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_official_borrow)

        setupWindowInsets()
        setupViews()
        loadBorrowStatus()
    }

    override fun onResume() {
        super.onResume()
        billingManager.refreshState()

        // After purchase completes, auto-bind the pending entity
        val entityId = pendingBindEntityId
        if (entityId != null && billingManager.subscriptionState.value.hasBorrowSubscription) {
            pendingBindEntityId = null
            completePersonalBind(entityId)
        }
    }

    private fun setupWindowInsets() {
        val topBar = findViewById<View>(R.id.topBar)
        ViewCompat.setOnApplyWindowInsetsListener(topBar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top + 12)
            insets
        }
    }

    private fun setupViews() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        chipGroupEntity = findViewById(R.id.chipGroupEntity)
        progressLoading = findViewById(R.id.progressLoading)
        btnBindFree = findViewById(R.id.btnBindFree)
        btnBindPersonal = findViewById(R.id.btnBindPersonal)
        tvAvailability = findViewById(R.id.tvAvailability)
        tvPersonalPrice = findViewById(R.id.tvPersonalPrice)

        // Entity chip selection
        chipGroupEntity.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chipId = checkedIds.first()
                selectedEntityId = when (chipId) {
                    R.id.chipEntity0 -> 0
                    R.id.chipEntity1 -> 1
                    R.id.chipEntity2 -> 2
                    R.id.chipEntity3 -> 3
                    else -> 0
                }
                updateButtonStates()
            }
        }

        btnBindFree.setOnClickListener { bindFree() }
        btnBindPersonal.setOnClickListener { bindPersonal() }

        // Apply strikethrough to original price
        findViewById<TextView>(R.id.tvPriceOriginal).paintFlags =
            findViewById<TextView>(R.id.tvPriceOriginal).paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        // Price is now shown via layout (strikethrough original + discount)
        // Google Play price override is optional - the layout already shows NT$288
        val borrowPrice = billingManager.subscriptionState.value.borrowSubscriptionPrice
        if (borrowPrice.isNotEmpty()) {
            tvPersonalPrice.text = borrowPrice
        }
    }

    private fun loadBorrowStatus() {
        progressLoading.visibility = View.VISIBLE
        btnBindFree.isEnabled = false
        btnBindPersonal.isEnabled = false

        lifecycleScope.launch {
            try {
                val status = api.getOfficialBorrowStatus(deviceManager.deviceId)
                borrowStatus = status

                // Update availability display
                if (status.personal.available > 0) {
                    tvAvailability.text = getString(R.string.remaining_bots, status.personal.available)
                    tvAvailability.setTextColor(0xFF4CAF50.toInt())
                } else {
                    tvAvailability.text = getString(R.string.sold_out)
                    tvAvailability.setTextColor(0xFFFF5252.toInt())
                }

                updateButtonStates()
            } catch (e: Exception) {
                Timber.e(e, "Failed to load borrow status")
                Toast.makeText(this@OfficialBorrowActivity, getString(R.string.failed_format, e.message), Toast.LENGTH_SHORT).show()
            } finally {
                progressLoading.visibility = View.GONE
            }
        }
    }

    private fun updateButtonStates() {
        val status = borrowStatus ?: return

        // Check if selected entity already has an official binding
        val existingBinding = status.bindings.find { it.entityId == selectedEntityId }

        if (existingBinding != null) {
            // Already bound - show bound state with unbind option
            when (existingBinding.botType) {
                "free" -> {
                    btnBindFree.text = getString(R.string.already_bound_free)
                    btnBindFree.isEnabled = false
                    btnBindPersonal.text = getString(R.string.unbind)
                    btnBindPersonal.isEnabled = true
                    btnBindPersonal.setOnClickListener { confirmUnbind(existingBinding) }
                }
                "personal" -> {
                    btnBindPersonal.text = getString(R.string.already_bound_personal)
                    btnBindPersonal.isEnabled = false
                    btnBindFree.text = getString(R.string.unbind)
                    btnBindFree.isEnabled = true
                    btnBindFree.setOnClickListener { confirmUnbind(existingBinding) }
                }
            }
        } else {
            // Not bound - show available actions, restore click listeners
            btnBindFree.setOnClickListener { bindFree() }
            btnBindPersonal.setOnClickListener { bindPersonal() }

            // Check if device already has a free binding on another entity (limit 1 per device)
            val hasFreeBoundElsewhere = status.bindings.any { it.botType == "free" }
            if (hasFreeBoundElsewhere) {
                val boundEntity = status.bindings.first { it.botType == "free" }.entityId
                btnBindFree.text = getString(R.string.bound_on_entity, boundEntity)
                btnBindFree.isEnabled = false
            } else {
                btnBindFree.text = getString(R.string.use_free_version)
                btnBindFree.isEnabled = status.free.available
            }

            btnBindPersonal.text = getString(R.string.subscribe_and_bind)
            btnBindPersonal.isEnabled = status.personal.available > 0
        }

        // Check if entity slot is already occupied by a non-official binding
        val registeredIds = layoutPrefs.getRegisteredEntityIds()
        if (registeredIds.contains(selectedEntityId) && existingBinding == null) {
            // Slot occupied by regular binding
            btnBindFree.isEnabled = false
            btnBindPersonal.isEnabled = false
            btnBindFree.text = getString(R.string.entity_already_bound, selectedEntityId)
            btnBindPersonal.text = getString(R.string.entity_already_bound, selectedEntityId)
        }
    }

    private fun bindFree() {
        btnBindFree.isEnabled = false
        progressLoading.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = api.bindFreeBorrow(mapOf(
                    "deviceId" to deviceManager.deviceId,
                    "deviceSecret" to deviceManager.deviceSecret,
                    "entityId" to selectedEntityId
                ))

                if (response.success) {
                    layoutPrefs.addRegisteredEntity(selectedEntityId)
                    Toast.makeText(this@OfficialBorrowActivity, getString(R.string.bind_success), Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this@OfficialBorrowActivity, getString(R.string.bind_failed, response.error ?: response.message ?: "Unknown"), Toast.LENGTH_SHORT).show()
                    btnBindFree.isEnabled = true
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to bind free bot")
                Toast.makeText(this@OfficialBorrowActivity, getString(R.string.bind_failed, e.message), Toast.LENGTH_SHORT).show()
                btnBindFree.isEnabled = true
            } finally {
                progressLoading.visibility = View.GONE
            }
        }
    }

    private fun confirmUnbind(binding: BorrowBinding) {
        val titleRes = if (binding.botType == "personal") R.string.unbind_personal_title else R.string.unbind_free_title
        val messageRes = if (binding.botType == "personal") R.string.unbind_personal_message else R.string.unbind_free_message

        AlertDialog.Builder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
            .setTitle(titleRes)
            .setMessage(messageRes)
            .setPositiveButton(R.string.unbind) { _, _ -> unbindEntity() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun unbindEntity() {
        progressLoading.visibility = View.VISIBLE
        btnBindFree.isEnabled = false
        btnBindPersonal.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = api.unbindBorrow(mapOf(
                    "deviceId" to deviceManager.deviceId,
                    "deviceSecret" to deviceManager.deviceSecret,
                    "entityId" to selectedEntityId
                ))

                if (response.success) {
                    layoutPrefs.removeRegisteredEntity(selectedEntityId)
                    Toast.makeText(this@OfficialBorrowActivity, getString(R.string.unbind_success), Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    // Reload status to refresh UI
                    loadBorrowStatus()
                } else {
                    Toast.makeText(this@OfficialBorrowActivity, getString(R.string.unbind_failed, response.message ?: "Unknown"), Toast.LENGTH_SHORT).show()
                    updateButtonStates()
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to unbind official bot")
                Toast.makeText(this@OfficialBorrowActivity, getString(R.string.unbind_failed, e.message), Toast.LENGTH_SHORT).show()
                updateButtonStates()
            } finally {
                progressLoading.visibility = View.GONE
            }
        }
    }

    private fun bindPersonal() {
        if (BuildConfig.DEBUG) {
            // Test mode: skip payment, bind directly
            completePersonalBind(selectedEntityId)
            return
        }
        // Production: require payment per entity
        pendingBindEntityId = selectedEntityId
        billingManager.launchBorrowPurchaseFlow(this)
    }

    private fun completePersonalBind(entityId: Int) {
        btnBindPersonal.isEnabled = false
        progressLoading.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = api.bindPersonalBorrow(mapOf(
                    "deviceId" to deviceManager.deviceId,
                    "deviceSecret" to deviceManager.deviceSecret,
                    "entityId" to entityId
                ))

                if (response.success) {
                    layoutPrefs.addRegisteredEntity(entityId)

                    // Verify subscription on backend
                    try {
                        api.verifyBorrowSubscription(mapOf(
                            "deviceId" to deviceManager.deviceId,
                            "deviceSecret" to deviceManager.deviceSecret,
                            "entityId" to entityId
                        ))
                    } catch (_: Exception) { }

                    Toast.makeText(this@OfficialBorrowActivity, getString(R.string.bind_success), Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    val errorMsg = when (response.error) {
                        "sold_out" -> getString(R.string.sold_out)
                        else -> response.error ?: response.message ?: "Unknown"
                    }
                    Toast.makeText(this@OfficialBorrowActivity, getString(R.string.bind_failed, errorMsg), Toast.LENGTH_SHORT).show()
                    btnBindPersonal.isEnabled = true
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to bind personal bot")
                Toast.makeText(this@OfficialBorrowActivity, getString(R.string.bind_failed, e.message), Toast.LENGTH_SHORT).show()
                btnBindPersonal.isEnabled = true
            } finally {
                progressLoading.visibility = View.GONE
            }
        }
    }
}

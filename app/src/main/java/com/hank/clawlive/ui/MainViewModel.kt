package com.hank.clawlive.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hank.clawlive.data.local.DeviceManager
import com.hank.clawlive.data.model.AgentStatus
import com.hank.clawlive.data.model.DeviceStatusRequest
import com.hank.clawlive.data.model.RegisterRequest
import com.hank.clawlive.data.remote.NetworkModule
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

data class BindingUiState(
    val isLoading: Boolean = false,
    val bindingCode: String? = null,
    val remainingSeconds: Int = 0,
    val isBound: Boolean = false,
    val agentStatus: AgentStatus? = null,
    val error: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val deviceManager = DeviceManager.getInstance(application)
    private val api = NetworkModule.api

    private val _uiState = MutableStateFlow(BindingUiState())
    val uiState: StateFlow<BindingUiState> = _uiState.asStateFlow()

    init {
        // Check if already has a valid binding code
        val savedCode = deviceManager.currentBindingCode
        val expiry = deviceManager.bindingCodeExpiry
        val now = System.currentTimeMillis()

        if (savedCode != null && expiry > now) {
            val remaining = ((expiry - now) / 1000).toInt()
            _uiState.value = _uiState.value.copy(
                bindingCode = savedCode,
                remainingSeconds = remaining
            )
            startCountdown()
        }

        // Start polling for status if we have credentials
        startStatusPolling()
    }

    /**
     * Generate a new binding code
     */
    fun generateBindingCode() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val request = RegisterRequest(
                    deviceId = deviceManager.deviceId,
                    deviceSecret = deviceManager.deviceSecret
                )

                Timber.d("Registering device: ${deviceManager.deviceId}")

                val response = api.registerDevice(request)

                if (response.success) {
                    val expiryTime = System.currentTimeMillis() + (response.expiresIn * 1000L)
                    deviceManager.currentBindingCode = response.bindingCode
                    deviceManager.bindingCodeExpiry = expiryTime

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        bindingCode = response.bindingCode,
                        remainingSeconds = response.expiresIn
                    )

                    Timber.d("Binding code generated: ${response.bindingCode}")
                    startCountdown()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to generate binding code"
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Error generating binding code")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Network error: ${e.message}"
                )
            }
        }
    }

    /**
     * Start countdown timer for binding code expiry
     */
    private fun startCountdown() {
        viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0) {
                delay(1000)
                val newRemaining = _uiState.value.remainingSeconds - 1
                _uiState.value = _uiState.value.copy(remainingSeconds = newRemaining)

                if (newRemaining <= 0) {
                    _uiState.value = _uiState.value.copy(bindingCode = null)
                    deviceManager.currentBindingCode = null
                }
            }
        }
    }

    /**
     * Start polling for agent status
     */
    private fun startStatusPolling() {
        viewModelScope.launch {
            while (true) {
                try {
                    val request = DeviceStatusRequest(
                        deviceId = deviceManager.deviceId,
                        deviceSecret = deviceManager.deviceSecret
                    )

                    val status = api.getDeviceStatus(request)
                    _uiState.value = _uiState.value.copy(
                        agentStatus = status,
                        isBound = true
                    )

                    Timber.d("Status updated: ${status.state} - ${status.message}")
                } catch (e: Exception) {
                    // Device might not be registered yet, or network error
                    Timber.d("Status polling: ${e.message}")
                }

                delay(5000) // Poll every 5 seconds
            }
        }
    }

    /**
     * Reset device and generate new credentials
     */
    fun resetDevice() {
        deviceManager.reset()
        _uiState.value = BindingUiState()
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

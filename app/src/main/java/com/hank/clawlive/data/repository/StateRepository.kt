package com.hank.clawlive.data.repository

import android.content.Context
import com.hank.clawlive.data.local.DeviceManager
import com.hank.clawlive.data.model.AgentStatus
import com.hank.clawlive.data.model.DeviceStatusRequest
import com.hank.clawlive.data.remote.ClawApiService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber

class StateRepository(
    private val api: ClawApiService,
    private val context: Context
) {
    private val deviceManager by lazy { DeviceManager.getInstance(context) }

    /**
     * Polls the API every [intervalMs] for agent status
     * Uses device credentials for authentication
     */
    fun getStatusFlow(intervalMs: Long = 5_000): Flow<AgentStatus> = flow {
        while (true) {
            try {
                val request = DeviceStatusRequest(
                    deviceId = deviceManager.deviceId,
                    deviceSecret = deviceManager.deviceSecret
                )

                val status = api.getDeviceStatus(request)
                emit(status)
                Timber.d("Status fetched: ${status.state} - ${status.message}")

            } catch (e: Exception) {
                Timber.e(e, "Error fetching status")
                // Emit a default status on error
                emit(
                    AgentStatus(
                        message = "Connecting...",
                        state = com.hank.clawlive.data.model.CharacterState.IDLE
                    )
                )
            }
            delay(intervalMs)
        }
    }

    /**
     * Wake up the agent (called when user taps the wallpaper)
     */
    suspend fun wakeUp() {
        try {
            // For now, just log. In future, could POST to a device wakeup endpoint
            Timber.d("Wake Up Signal Sent from device: ${deviceManager.deviceId}")
        } catch (e: Exception) {
            Timber.e(e, "Error sending wakeup")
        }
    }
}

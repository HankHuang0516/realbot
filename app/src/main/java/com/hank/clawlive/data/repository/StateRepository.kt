package com.hank.clawlive.data.repository

import android.content.Context
import com.hank.clawlive.data.local.DeviceManager
import com.hank.clawlive.data.local.LayoutPreferences
import com.hank.clawlive.data.model.AgentStatus
import com.hank.clawlive.data.model.CharacterState
import com.hank.clawlive.data.model.EntityStatus
import com.hank.clawlive.data.model.MultiEntityResponse
import com.hank.clawlive.data.remote.ClawApiService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber

class StateRepository(
    private val api: ClawApiService,
    private val context: Context
) {
    private val layoutPrefs = LayoutPreferences.getInstance(context)
    private val deviceManager = DeviceManager.getInstance(context)
    private val chatRepository = ChatRepository.getInstance(context)

    /**
     * Polls the API every [intervalMs] for single entity status.
     * Now uses deviceId for v5 matrix architecture.
     */
    fun getStatusFlow(intervalMs: Long = 5_000): Flow<AgentStatus> = flow {
        while (true) {
            try {
                val status = api.getAgentStatus(
                    deviceId = deviceManager.deviceId,
                    entityId = 0,
                    appVersion = deviceManager.appVersion
                )
                emit(status)
                Timber.d("API Status fetched: ${status.character} - ${status.state}")
            } catch (e: Exception) {
                Timber.e(e, "Error fetching from API")
                emit(
                    AgentStatus(
                        message = "Lost: ${e.message}\nRetrying...",
                        state = CharacterState.SLEEPING
                    )
                )
            }
            delay(intervalMs)
        }
    }

    /**
     * Polls the API every [intervalMs] for multi-entity status.
     * Filters to only show entities for THIS device (v5 matrix architecture).
     * Also processes entity messages for chat history.
     */
    fun getMultiEntityStatusFlow(intervalMs: Long = 5_000): Flow<MultiEntityResponse> = flow {
        while (true) {
            try {
                // v5: Filter by deviceId on server side
                val response = api.getAllEntities(deviceId = deviceManager.deviceId)

                // Additional client-side filter for registered entities
                val registeredIds = layoutPrefs.getRegisteredEntityIds()
                val filteredEntities = if (registeredIds.isEmpty()) {
                    // If no registrations yet, show all entities for this device
                    response.entities
                } else {
                    response.entities.filter { it.entityId in registeredIds }
                }

                // Process entity messages for chat history (with deduplication)
                filteredEntities.forEach { entity ->
                    try {
                        chatRepository.processEntityMessage(entity)
                    } catch (e: Exception) {
                        Timber.e(e, "Error processing entity message for chat history")
                    }
                }

                val filteredResponse = MultiEntityResponse(
                    entities = filteredEntities,
                    activeCount = filteredEntities.size,
                    maxEntities = response.maxEntities
                )

                emit(filteredResponse)
                Timber.d("Multi-entity status: ${filteredEntities.size} entities for device ${deviceManager.deviceId}")
            } catch (e: Exception) {
                Timber.e(e, "Error fetching multi-entity status")
                // Fallback to single error entity
                emit(
                    MultiEntityResponse(
                        entities = listOf(
                            EntityStatus(
                                entityId = 0,
                                message = "Connection error: ${e.message}",
                                state = CharacterState.SLEEPING
                            )
                        ),
                        activeCount = 1
                    )
                )
            }
            delay(intervalMs)
        }
    }

    /**
     * Wake up the agent (called when user taps the wallpaper)
     * Note: wakeUp now requires botSecret which client doesn't have.
     * This is a best-effort call that may fail silently.
     */
    suspend fun wakeUp(entityId: Int = 0) {
        try {
            val body = mapOf(
                "deviceId" to deviceManager.deviceId,
                "entityId" to entityId.toString()
            )
            api.wakeUpAgent(body)
            Timber.d("Wake Up Signal Sent for device ${deviceManager.deviceId} entity $entityId")
        } catch (e: Exception) {
            Timber.e(e, "Error sending wakeup webhook (may require botSecret)")
        }
    }

    /**
     * Report real device battery level to backend.
     * This replaces the simulated battery decay on the server.
     */
    suspend fun updateBatteryLevel(batteryLevel: Int) {
        try {
            val body = mapOf(
                "deviceId" to deviceManager.deviceId,
                "deviceSecret" to deviceManager.deviceSecret,
                "batteryLevel" to batteryLevel
            )
            api.updateBatteryLevel(body)
            Timber.d("Battery level reported: $batteryLevel%")
        } catch (e: Exception) {
            Timber.e(e, "Error reporting battery level")
        }
    }
}

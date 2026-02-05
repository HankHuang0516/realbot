package com.hank.clawlive.data.repository

import android.content.Context
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

    /**
     * Polls the API every [intervalMs] for single entity status (backward compatible).
     */
    fun getStatusFlow(intervalMs: Long = 5_000): Flow<AgentStatus> = flow {
        while (true) {
            try {
                val status = api.getAgentStatus()
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
     * Returns all active entities on the wallpaper.
     */
    fun getMultiEntityStatusFlow(intervalMs: Long = 5_000): Flow<MultiEntityResponse> = flow {
        while (true) {
            try {
                val response = api.getAllEntities()
                emit(response)
                Timber.d("Multi-entity status: ${response.activeCount} entities")
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
     */
    suspend fun wakeUp() {
        try {
            api.wakeUpAgent()
            Timber.d("Wake Up Signal Sent to Backend")
        } catch (e: Exception) {
            Timber.e(e, "Error sending wakeup webhook")
        }
    }
}

package com.hank.clawlive.data.repository

import com.hank.clawlive.data.model.AgentStatus
import com.hank.clawlive.data.model.CharacterState
import com.hank.clawlive.data.remote.ClawApiService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber

class StateRepository(private val api: ClawApiService) {

    // Polls the API every [intervalMs]
    fun getStatusFlow(intervalMs: Long = 30_000): Flow<AgentStatus> = flow {
        while (true) {
            try {
                // For Proof of Concept, until we have a real server, 
                // we might want to emit some mock data occasionally if network fails,
                // or just try to fetch real data.
                // emit(api.getAgentStatus())
                
                // MOCKING DATA LOCALLY FOR NOW (To demonstrate "Database" connection working)
                // In real implementation, uncomment the line above and remove mock logic below.
                val mockStatus = fetchMockStatus()
                emit(mockStatus)
                
            } catch (e: Exception) {
                Timber.e(e, "Error fetching status")
                // Emit an error state or keep previous (flow will just delay and retry)
                emit(AgentStatus(message = "Connection Error: ${e.localizedMessage}"))
            }
            delay(intervalMs)
        }
    }

    // Temporary mock to simulate backend changes
    private var mockCounter = 0
    private fun fetchMockStatus(): AgentStatus {
        mockCounter++
        val states = CharacterState.values()
        val randomState = states[mockCounter % states.size]
        
        // Randomize character type every few updates to simulate form changing
        val charTypes = com.hank.clawlive.data.model.CharacterType.values()
        val randomChar = charTypes[(mockCounter / 2) % charTypes.size] // Switch every 2 updates
        
        return AgentStatus(
            character = randomChar,
            state = randomState,
            message = "Server Update #$mockCounter\nForm: ${randomChar.name}",
            batteryLevel = (100 - mockCounter).coerceAtLeast(0)
        )
    }

    suspend fun wakeUp() {
        // Simulate a network call to wake up the bot
        delay(500) // Latency simulation
        // In a real app, this would POST to /api/wakeup
        Timber.d("Wake Up Signal Sent!")
    }
}

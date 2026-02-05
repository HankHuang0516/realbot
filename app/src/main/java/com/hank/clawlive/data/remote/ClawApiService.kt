package com.hank.clawlive.data.remote

import com.hank.clawlive.data.model.AgentStatus
import retrofit2.http.GET

interface ClawApiService {
    // Ideally this URL would be dynamic or configurable
    // For now, we assume a standard endpoint relative to the base URL
    @GET("api/status")
    suspend fun getAgentStatus(): AgentStatus
}

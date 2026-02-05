package com.hank.clawlive.data.remote

import com.hank.clawlive.data.model.AgentStatus
import com.hank.clawlive.data.model.ApiResponse
import com.hank.clawlive.data.model.DeviceStatusRequest
import com.hank.clawlive.data.model.MultiEntityResponse
import com.hank.clawlive.data.model.RegisterRequest
import com.hank.clawlive.data.model.RegisterResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ClawApiService {

    // ============================================
    // SINGLE ENTITY (Backward Compatible)
    // ============================================

    // Get status for entity 0 (default)
    @GET("api/status")
    suspend fun getAgentStatus(): AgentStatus

    // Device registration - get binding code
    @POST("api/device/register")
    suspend fun registerDevice(@Body request: RegisterRequest): RegisterResponse

    // Device status - using deviceId + secret
    @POST("api/device/status")
    suspend fun getDeviceStatus(@Body request: DeviceStatusRequest): AgentStatus

    // Webhook to wake up agent
    @POST("api/wakeup")
    suspend fun wakeUpAgent(): ApiResponse

    // Send message to bot
    @POST("api/client/speak")
    suspend fun sendClientMessage(@Body body: Map<String, String>): ApiResponse

    // ============================================
    // MULTI-ENTITY ENDPOINTS
    // ============================================

    // Get all active entities
    @GET("api/entities")
    suspend fun getAllEntities(): MultiEntityResponse

    // Get status with all=true query param (alternative to /api/entities)
    @GET("api/status")
    suspend fun getAllEntitiesStatus(@Query("all") all: Boolean = true): MultiEntityResponse
}



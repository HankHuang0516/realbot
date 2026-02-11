package com.hank.clawlive.data.remote

import com.hank.clawlive.data.model.AgentStatus
import com.hank.clawlive.data.model.ApiResponse
import com.hank.clawlive.data.model.ClientMessageResponse
import com.hank.clawlive.data.model.DeviceStatusRequest
import com.hank.clawlive.data.model.MultiEntityResponse
import com.hank.clawlive.data.model.RegisterRequest
import com.hank.clawlive.data.model.RegisterResponse
import com.hank.clawlive.data.model.SpawnEntityRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Query

interface ClawApiService {

    // ============================================
    // v5 MATRIX ARCHITECTURE
    // All APIs now require deviceId for proper isolation
    // ============================================

    // Get status for specific device + entity
    @GET("api/status")
    suspend fun getAgentStatus(
        @Query("deviceId") deviceId: String,
        @Query("entityId") entityId: Int = 0,
        @Query("appVersion") appVersion: String? = null
    ): AgentStatus

    // Device registration - get binding code
    @POST("api/device/register")
    suspend fun registerDevice(@Body request: RegisterRequest): RegisterResponse

    // Device status - using deviceId + secret
    @POST("api/device/status")
    suspend fun getDeviceStatus(@Body request: DeviceStatusRequest): AgentStatus

    // Webhook to wake up agent (now requires deviceId in body)
    @POST("api/wakeup")
    suspend fun wakeUpAgent(@Body body: Map<String, String>): ApiResponse

    // Send message to bot (requires deviceId)
    // entityId can be: Int, List<Int>, or "all" for broadcast
    @POST("api/client/speak")
    suspend fun sendClientMessage(@Body body: Map<String, @JvmSuppressWildcards Any>): ClientMessageResponse

    // ============================================
    // MULTI-ENTITY ENDPOINTS
    // ============================================

    // Get all active entities (optionally filter by deviceId)
    @GET("api/entities")
    suspend fun getAllEntities(@Query("deviceId") deviceId: String? = null): MultiEntityResponse

    // Remove entity by bot (requires botSecret)
    // Use @HTTP instead of @DELETE to allow request body
    @HTTP(method = "DELETE", path = "api/entity", hasBody = true)
    suspend fun removeEntity(@Body body: Map<String, String>): ApiResponse

    // Remove entity by device owner (requires deviceSecret)
    @HTTP(method = "DELETE", path = "api/device/entity", hasBody = true)
    suspend fun removeEntityByDevice(@Body body: Map<String, @JvmSuppressWildcards Any>): ApiResponse

    // ============================================
    // BATTERY REPORTING
    // ============================================

    // Report real device battery level to backend
    @POST("api/device/battery")
    suspend fun updateBatteryLevel(@Body body: Map<String, @JvmSuppressWildcards Any>): ApiResponse
}

package com.hank.clawlive.data.remote

import com.hank.clawlive.data.model.*
import retrofit2.http.*

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
    @HTTP(method = "DELETE", path = "api/entity", hasBody = true)
    suspend fun removeEntity(@Body body: Map<String, String>): ApiResponse

    // Remove entity by device owner (requires deviceSecret)
    @HTTP(method = "DELETE", path = "api/device/entity", hasBody = true)
    suspend fun removeEntityByDevice(@Body body: Map<String, @JvmSuppressWildcards Any>): ApiResponse

    // ============================================
    // FEEDBACK
    // ============================================

    @POST("api/feedback")
    suspend fun sendFeedback(@Body body: Map<String, String>): ApiResponse

    // ============================================
    // BOT POLLING ENDPOINTS
    // ============================================

    @POST("api/bot/pending-messages")
    suspend fun getPendingMessages(@Body body: Map<String, String>): PendingMessagesResponse

    // ============================================
    // MISSION CONTROL DASHBOARD
    // ============================================

    /**
     * 取得完整 Dashboard
     */
    @GET("api/mission/dashboard")
    suspend fun getMissionDashboard(@QueryMap params: Map<String, String>): MissionDashboardResponse

    /**
     * 上傳完整 Dashboard (用戶手動)
     */
    @POST("api/mission/dashboard")
    suspend fun uploadMissionDashboard(
        @QueryMap params: Map<String, String>,
        @Body dashboard: MissionDashboardSnapshot
    ): MissionDashboardResponse

    /**
     * 取得所有任務
     */
    @GET("api/mission/items")
    suspend fun getMissionItems(@QueryMap params: Map<String, String>): MissionItemsResponse

    /**
     * 新增任務
     */
    @POST("api/mission/items")
    suspend fun addMissionItem(
        @QueryMap params: Map<String, String>,
        @Body item: MissionItem,
        @Query("list") list: String
    ): MissionItemResponse

    /**
     * 取得筆記 (Bots 可讀)
     */
    @GET("api/mission/notes")
    suspend fun getMissionNotes(@QueryMap params: Map<String, String>): MissionNotesResponse

    /**
     * 取得規則
     */
    @GET("api/mission/rules")
    suspend fun getMissionRules(@QueryMap params: Map<String, String>): MissionRulesResponse
}

// ============ Mission Control Response Models ============

data class MissionDashboardResponse(
    val success: Boolean,
    val dashboard: MissionDashboardSnapshot?,
    val version: Int? = null,
    val message: String? = null,
    val error: String? = null
)

data class MissionItemsResponse(
    val success: Boolean,
    val items: List<MissionItem>,
    val error: String? = null
)

data class MissionItemResponse(
    val success: Boolean,
    val item: MissionItem?,
    val error: String? = null
)

data class MissionNotesResponse(
    val success: Boolean,
    val notes: List<MissionNote>,
    val error: String? = null
)

data class MissionRulesResponse(
    val success: Boolean,
    val rules: List<MissionRule>,
    val error: String? = null
)

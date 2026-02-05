package com.hank.clawlive.data.remote

import com.hank.clawlive.data.model.AgentStatus
import com.hank.clawlive.data.model.BindingResponse
import com.hank.clawlive.data.model.DeviceStatusRequest
import com.hank.clawlive.data.model.RegisterRequest
import com.hank.clawlive.data.model.RegisterResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ClawApiService {

    // Old endpoint (requires JWT - for OpenClaw)
    @GET("api/status")
    suspend fun getAgentStatus(): AgentStatus

    // Device registration - get binding code
    @POST("api/device/register")
    suspend fun registerDevice(@Body request: RegisterRequest): RegisterResponse

    // Device status - using deviceId + secret
    @POST("api/device/status")
    suspend fun getDeviceStatus(@Body request: DeviceStatusRequest): AgentStatus
}

package com.hank.clawlive.data.model

import com.google.gson.annotations.SerializedName

// Request to register device and get binding code
data class RegisterRequest(
    @SerializedName("entityId") val entityId: Int = 0, // 0-7
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("deviceSecret") val deviceSecret: String,
    @SerializedName("appVersion") val appVersion: String? = null, // e.g., "1.0.3"
    @SerializedName("isTestDevice") val isTestDevice: Boolean = false
)

// Response from device registration
data class RegisterResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("entityId") val entityId: Int = 0,
    @SerializedName("bindingCode") val bindingCode: String,
    @SerializedName("expiresIn") val expiresIn: Int // seconds
)

// Response from entity refresh
data class RefreshEntityResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("error") val error: String? = null,
    @SerializedName("webhookBroken") val webhookBroken: Boolean = false,
    @SerializedName("cooldown_remaining") val cooldownRemaining: Int? = null,
    @SerializedName("botType") val botType: String? = null,
    @SerializedName("pollingMode") val pollingMode: Boolean = false,
    @SerializedName("hint") val hint: String? = null
)

// Request to get device status
data class DeviceStatusRequest(
    @SerializedName("entityId") val entityId: Int = 0,
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("deviceSecret") val deviceSecret: String,
    @SerializedName("appVersion") val appVersion: String? = null // e.g., "1.0.3"
)

// Response from OpenClaw binding (for reference)
data class BindingResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("token") val token: String?,
    @SerializedName("webhookSecret") val webhookSecret: String?,
    @SerializedName("deviceId") val deviceId: String?
)

// Request to spawn a new entity
data class SpawnEntityRequest(
    @SerializedName("entityId") val entityId: Int? = null, // null for auto-assign
    @SerializedName("character") val character: String = "LOBSTER",
    @SerializedName("message") val message: String? = null
)

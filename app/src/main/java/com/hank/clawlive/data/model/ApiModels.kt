package com.hank.clawlive.data.model

import com.google.gson.annotations.SerializedName

// Request to register device and get binding code
data class RegisterRequest(
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("deviceSecret") val deviceSecret: String
)

// Response from device registration
data class RegisterResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("bindingCode") val bindingCode: String,
    @SerializedName("expiresIn") val expiresIn: Int // seconds
)

// Request to get device status
data class DeviceStatusRequest(
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("deviceSecret") val deviceSecret: String
)

// Response from OpenClaw binding (for reference)
data class BindingResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("token") val token: String?,
    @SerializedName("webhookSecret") val webhookSecret: String?,
    @SerializedName("deviceId") val deviceId: String?
)

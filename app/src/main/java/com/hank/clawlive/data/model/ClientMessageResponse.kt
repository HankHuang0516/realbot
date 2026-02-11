package com.hank.clawlive.data.model

/**
 * Response from /api/client/speak endpoint
 * Includes push notification status for each target entity
 */
data class ClientMessageResponse(
    val success: Boolean,
    val message: String,
    val targets: List<MessageTarget> = emptyList(),
    val broadcast: Boolean = false
)

data class MessageTarget(
    val entityId: Int,
    val pushed: Boolean,
    val mode: String // "push" or "polling"
)

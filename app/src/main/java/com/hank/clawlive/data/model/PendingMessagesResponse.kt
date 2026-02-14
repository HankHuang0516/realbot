package com.hank.clawlive.data.model

/**
 * Response from /api/bot/pending-messages
 * Bot polls for pending messages when webhook is not available
 */
data class PendingMessagesResponse(
    val success: Boolean,
    val deviceId: String,
    val entityId: Int,
    val messages: List<BotMessageItem>,
    val messageCount: Int,
    val mode: String  // "push" or "polling"
)

/**
 * Individual bot message item from pending-messages API
 */
data class BotMessageItem(
    val text: String,
    val from: String,
    val fromEntityId: Int,
    val fromCharacter: String,
    val timestamp: Long,
    val read: Boolean = false,
    val delivered: Boolean = false
)

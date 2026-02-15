package com.hank.clawlive.data.repository

import android.content.Context
import com.hank.clawlive.data.local.database.ChatDatabase
import com.hank.clawlive.data.local.database.ChatMessage
import com.hank.clawlive.data.local.database.ChatMessageDao
import com.hank.clawlive.data.local.database.MessageType
import com.hank.clawlive.data.model.EntityStatus
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

/**
 * Repository for managing chat message history
 */
class ChatRepository private constructor(
    private val chatDao: ChatMessageDao
) {
    companion object {
        @Volatile
        private var INSTANCE: ChatRepository? = null

        fun getInstance(context: Context): ChatRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ChatRepository(
                    ChatDatabase.getDao(context)
                ).also { INSTANCE = it }
            }
        }

        /** Deduplication time window in milliseconds (5 minutes) */
        private const val DEDUP_WINDOW_MS = 5 * 60 * 1000L
    }

    // ===== Message Flows for UI =====

    /**
     * Get all messages (newest first) as Flow
     */
    fun getRecentMessages(limit: Int = 100): Flow<List<ChatMessage>> {
        return chatDao.getRecentMessages(limit)
    }

    /**
     * Get messages in ascending order (oldest first) for chat display
     */
    fun getMessagesAscending(limit: Int = 500): Flow<List<ChatMessage>> {
        return chatDao.getMessagesAscending(limit)
    }

    /**
     * Get messages for a specific entity
     */
    fun getMessagesForEntity(entityId: Int, limit: Int = 50): Flow<List<ChatMessage>> {
        return chatDao.getMessagesForEntity(entityId, limit)
    }

    /**
     * Get only user-sent messages
     */
    fun getUserMessages(limit: Int = 50): Flow<List<ChatMessage>> {
        return chatDao.getUserMessages(limit)
    }

    /**
     * Get distinct entity IDs that have messages (for smart filter chips)
     */
    fun getDistinctEntityIds(): Flow<List<Int>> {
        return chatDao.getDistinctEntityIds()
    }

    // ===== Outgoing Messages (User -> Entity) =====

    /**
     * Save outgoing user message BEFORE sending to API
     * Returns the inserted message ID for tracking sync status
     */
    suspend fun saveOutgoingMessage(
        text: String,
        entityIds: List<Int>,
        source: String = "android_widget"
    ): Long {
        val messageType = if (entityIds.size > 1) {
            MessageType.USER_BROADCAST
        } else {
            MessageType.USER_TO_ENTITY
        }

        val message = ChatMessage(
            text = text,
            isFromUser = true,
            messageType = messageType,
            source = source,
            targetEntityIds = entityIds.joinToString(","),
            isSynced = false // Will be marked true after API success
        )

        val id = chatDao.insert(message)
        Timber.d("Saved outgoing message (id=$id): ${text.take(30)}...")
        return id
    }

    /**
     * Mark message as synced after API call succeeds
     */
    suspend fun markMessageSynced(messageId: Long) {
        chatDao.markSynced(messageId)
        Timber.d("Message $messageId marked as synced")
    }

    /**
     * Mark message as delivered with entity IDs that confirmed receipt
     */
    suspend fun markMessageDelivered(messageId: Long, entityIds: List<Int>) {
        val deliveredTo = entityIds.joinToString(",")
        chatDao.markDelivered(messageId, deliveredTo)
        Timber.d("Message $messageId delivered to entities: $deliveredTo")
    }

    // ===== Incoming Messages (Entity -> User) =====

    /**
     * Process incoming entity message from status polling
     * Uses deduplication to avoid storing the same message twice
     */
    suspend fun processEntityMessage(entity: EntityStatus) {
        // Skip if message is empty, default, or passive state
        val passiveMessages = listOf(
            "Loading...",
            "No message",
            "Waiting...",
            "Zzz...",
            "Ready",
            "Idle",
            "Standing by"
        )

        if (entity.message.isBlank() ||
            passiveMessages.any { entity.message.contains(it, ignoreCase = true) }) {
            return
        }

        // Generate deduplication key: entityId + message hash + timestamp bucket
        val timestampBucket = entity.lastUpdated / DEDUP_WINDOW_MS
        val deduplicationKey = "${entity.entityId}:${entity.message.hashCode()}:$timestampBucket"

        // Check if we already have this message
        if (chatDao.existsByDeduplicationKey(deduplicationKey)) {
            // Already stored, skip
            return
        }

        // Detect message type based on source pattern
        val (messageType, cleanedText) = parseEntityMessage(entity.message)

        val message = ChatMessage(
            text = cleanedText,
            timestamp = entity.lastUpdated,
            isFromUser = false,
            messageType = messageType,
            fromEntityId = entity.entityId,
            fromEntityName = entity.name,
            fromEntityCharacter = entity.character,
            deduplicationKey = deduplicationKey,
            isSynced = true
        )

        chatDao.insert(message)
        Timber.d("Saved entity message from Entity ${entity.entityId}: ${cleanedText.take(30)}...")

        // Periodically prune old messages to prevent database bloat
        pruneOldMessages()
    }

    /**
     * Parse entity message to detect type and clean up text
     * Pattern for entity-to-entity: "entity:{ID}:{CHARACTER}: {message}"
     */
    private fun parseEntityMessage(message: String): Pair<MessageType, String> {
        // Check for entity-to-entity pattern: "entity:0:LOBSTER: Hello"
        val entityPattern = Regex("^entity:(\\d+):([A-Z]+):\\s*(.*)$")
        val match = entityPattern.find(message)

        return if (match != null) {
            // Entity-to-entity message
            val actualMessage = match.groupValues[3]
            Pair(MessageType.ENTITY_TO_ENTITY, actualMessage)
        } else {
            // Regular entity response
            Pair(MessageType.ENTITY_RESPONSE, message)
        }
    }

    // ===== Maintenance =====

    /**
     * Get last N messages for widget (async)
     */
    suspend fun getLastMessagesForWidget(limit: Int = 5): List<ChatMessage> {
        return chatDao.getLastMessages(limit)
    }

    /**
     * Get recent messages synchronously for widget (called with runBlocking)
     * Returns messages in descending order (newest first)
     */
    suspend fun getRecentMessagesSync(limit: Int = 5): List<ChatMessage> {
        return chatDao.getLastMessages(limit)
    }

    /**
     * Get total message count
     */
    suspend fun getMessageCount(): Int {
        return chatDao.getMessageCount()
    }

    /**
     * Prune old messages to prevent database bloat
     * Keeps the last 500 messages by default
     */
    suspend fun pruneOldMessages(keepCount: Int = 500) {
        val currentCount = chatDao.getMessageCount()
        if (currentCount > keepCount) {
            chatDao.pruneOldMessages(keepCount)
            Timber.d("Pruned messages: kept last $keepCount of $currentCount")
        }
    }

    /**
     * Clear all messages (for testing/reset)
     */
    suspend fun clearAllMessages() {
        chatDao.clearAll()
        Timber.d("Cleared all chat messages")
    }
}

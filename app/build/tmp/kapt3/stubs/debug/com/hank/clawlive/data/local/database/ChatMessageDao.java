package com.hank.clawlive.data.local.database;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0010\t\n\u0002\b\u000b\bg\u0018\u00002\u00020\u0001J\u000e\u0010\u0002\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u00a7@\u00a2\u0006\u0002\u0010\tJ\u0016\u0010\n\u001a\u00020\u000b2\u0006\u0010\u0007\u001a\u00020\bH\u00a7@\u00a2\u0006\u0002\u0010\tJ\u0014\u0010\f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u000e0\rH\'J\u0018\u0010\u000f\u001a\u0004\u0018\u00010\u00102\u0006\u0010\u0011\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0012J\u001e\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00100\u000e2\b\b\u0002\u0010\u0014\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0012J\u000e\u0010\u0015\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0004J\u001e\u0010\u0016\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\u000e0\r2\b\b\u0002\u0010\u0014\u001a\u00020\u0006H\'J&\u0010\u0017\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\u000e0\r2\u0006\u0010\u0011\u001a\u00020\u00062\b\b\u0002\u0010\u0014\u001a\u00020\u0006H\'J\u001e\u0010\u0018\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\u000e0\r2\b\b\u0002\u0010\u0014\u001a\u00020\u0006H\'J\u001e\u0010\u0019\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\u000e0\r2\b\b\u0002\u0010\u0014\u001a\u00020\u0006H\'J\u0016\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u0010H\u00a7@\u00a2\u0006\u0002\u0010\u001dJ\u001c\u0010\u001e\u001a\u00020\u00032\f\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00100\u000eH\u00a7@\u00a2\u0006\u0002\u0010 J\u0016\u0010!\u001a\u00020\u00032\u0006\u0010\"\u001a\u00020\u001bH\u00a7@\u00a2\u0006\u0002\u0010#J\u0018\u0010$\u001a\u00020\u00032\b\b\u0002\u0010%\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0012\u00a8\u0006&"}, d2 = {"Lcom/hank/clawlive/data/local/database/ChatMessageDao;", "", "clearAll", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "countByDeduplicationKey", "", "key", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "existsByDeduplicationKey", "", "getDistinctEntityIds", "Lkotlinx/coroutines/flow/Flow;", "", "getLastEntityMessage", "Lcom/hank/clawlive/data/local/database/ChatMessage;", "entityId", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getLastMessages", "limit", "getMessageCount", "getMessagesAscending", "getMessagesForEntity", "getRecentMessages", "getUserMessages", "insert", "", "message", "(Lcom/hank/clawlive/data/local/database/ChatMessage;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertAll", "messages", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "markSynced", "messageId", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "pruneOldMessages", "keepCount", "app_debug"})
@androidx.room.Dao()
public abstract interface ChatMessageDao {
    
    /**
     * Get all messages ordered by timestamp (newest first)
     * Used for combined chat view
     */
    @androidx.room.Query(value = "SELECT * FROM chat_messages ORDER BY timestamp DESC LIMIT :limit")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.hank.clawlive.data.local.database.ChatMessage>> getRecentMessages(int limit);
    
    /**
     * Get all messages ordered by timestamp (oldest first for display)
     */
    @androidx.room.Query(value = "SELECT * FROM chat_messages ORDER BY timestamp ASC LIMIT :limit")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.hank.clawlive.data.local.database.ChatMessage>> getMessagesAscending(int limit);
    
    /**
     * Get messages for specific entity (both sent to and received from)
     */
    @androidx.room.Query(value = "\n        SELECT * FROM chat_messages\n        WHERE targetEntityIds LIKE \'%\' || :entityId || \'%\'\n           OR fromEntityId = :entityId\n        ORDER BY timestamp DESC\n        LIMIT :limit\n    ")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.hank.clawlive.data.local.database.ChatMessage>> getMessagesForEntity(int entityId, int limit);
    
    /**
     * Get only user-sent messages
     */
    @androidx.room.Query(value = "SELECT * FROM chat_messages WHERE isFromUser = 1 ORDER BY timestamp DESC LIMIT :limit")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.hank.clawlive.data.local.database.ChatMessage>> getUserMessages(int limit);
    
    /**
     * Get last N messages for widget display (synchronous)
     */
    @androidx.room.Query(value = "SELECT * FROM chat_messages ORDER BY timestamp DESC LIMIT :limit")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getLastMessages(int limit, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.hank.clawlive.data.local.database.ChatMessage>> $completion);
    
    /**
     * Insert new message
     */
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insert(@org.jetbrains.annotations.NotNull()
    com.hank.clawlive.data.local.database.ChatMessage message, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    /**
     * Insert multiple messages
     */
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertAll(@org.jetbrains.annotations.NotNull()
    java.util.List<com.hank.clawlive.data.local.database.ChatMessage> messages, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * Check for duplicate by deduplication key
     */
    @androidx.room.Query(value = "SELECT COUNT(*) FROM chat_messages WHERE deduplicationKey = :key")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object countByDeduplicationKey(@org.jetbrains.annotations.NotNull()
    java.lang.String key, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion);
    
    /**
     * Check if message with this dedup key already exists
     */
    @androidx.room.Query(value = "SELECT EXISTS(SELECT 1 FROM chat_messages WHERE deduplicationKey = :key)")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object existsByDeduplicationKey(@org.jetbrains.annotations.NotNull()
    java.lang.String key, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion);
    
    /**
     * Get distinct entity IDs that have messages (for smart filter chips)
     */
    @androidx.room.Query(value = "\n        SELECT DISTINCT fromEntityId FROM chat_messages\n        WHERE fromEntityId IS NOT NULL\n        ORDER BY fromEntityId\n    ")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<java.lang.Integer>> getDistinctEntityIds();
    
    /**
     * Get the latest entity message for comparison
     */
    @androidx.room.Query(value = "\n        SELECT * FROM chat_messages\n        WHERE fromEntityId = :entityId AND messageType = \'ENTITY_RESPONSE\'\n        ORDER BY timestamp DESC\n        LIMIT 1\n    ")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getLastEntityMessage(int entityId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.hank.clawlive.data.local.database.ChatMessage> $completion);
    
    /**
     * Mark message as synced after API success
     */
    @androidx.room.Query(value = "UPDATE chat_messages SET isSynced = 1 WHERE id = :messageId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object markSynced(long messageId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * Get total message count
     */
    @androidx.room.Query(value = "SELECT COUNT(*) FROM chat_messages")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getMessageCount(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion);
    
    /**
     * Delete old messages, keeping only the last N
     */
    @androidx.room.Query(value = "\n        DELETE FROM chat_messages\n        WHERE id NOT IN (SELECT id FROM chat_messages ORDER BY timestamp DESC LIMIT :keepCount)\n    ")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object pruneOldMessages(int keepCount, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * Clear all messages
     */
    @androidx.room.Query(value = "DELETE FROM chat_messages")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object clearAll(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
    }
}
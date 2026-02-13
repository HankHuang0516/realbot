package com.hank.clawlive.data.repository;

/**
 * Repository for managing chat message history
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000P\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\n\u0018\u0000 +2\u00020\u0001:\u0001+B\u000f\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0005\u001a\u00020\u0006H\u0086@\u00a2\u0006\u0002\u0010\u0007J\u0012\u0010\b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\n0\tJ\u001e\u0010\f\u001a\b\u0012\u0004\u0012\u00020\r0\n2\b\b\u0002\u0010\u000e\u001a\u00020\u000bH\u0086@\u00a2\u0006\u0002\u0010\u000fJ\u000e\u0010\u0010\u001a\u00020\u000bH\u0086@\u00a2\u0006\u0002\u0010\u0007J\u001c\u0010\u0011\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\n0\t2\b\b\u0002\u0010\u000e\u001a\u00020\u000bJ$\u0010\u0012\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\n0\t2\u0006\u0010\u0013\u001a\u00020\u000b2\b\b\u0002\u0010\u000e\u001a\u00020\u000bJ\u001c\u0010\u0014\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\n0\t2\b\b\u0002\u0010\u000e\u001a\u00020\u000bJ\u001e\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\r0\n2\b\b\u0002\u0010\u000e\u001a\u00020\u000bH\u0086@\u00a2\u0006\u0002\u0010\u000fJ\u001c\u0010\u0016\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\n0\t2\b\b\u0002\u0010\u000e\u001a\u00020\u000bJ\u0016\u0010\u0017\u001a\u00020\u00062\u0006\u0010\u0018\u001a\u00020\u0019H\u0086@\u00a2\u0006\u0002\u0010\u001aJ\u001c\u0010\u001b\u001a\u000e\u0012\u0004\u0012\u00020\u001d\u0012\u0004\u0012\u00020\u001e0\u001c2\u0006\u0010\u001f\u001a\u00020\u001eH\u0002J\u0016\u0010 \u001a\u00020\u00062\u0006\u0010!\u001a\u00020\"H\u0086@\u00a2\u0006\u0002\u0010#J\u0018\u0010$\u001a\u00020\u00062\b\b\u0002\u0010%\u001a\u00020\u000bH\u0086@\u00a2\u0006\u0002\u0010\u000fJ.\u0010&\u001a\u00020\u00192\u0006\u0010\'\u001a\u00020\u001e2\f\u0010(\u001a\b\u0012\u0004\u0012\u00020\u000b0\n2\b\b\u0002\u0010)\u001a\u00020\u001eH\u0086@\u00a2\u0006\u0002\u0010*R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006,"}, d2 = {"Lcom/hank/clawlive/data/repository/ChatRepository;", "", "chatDao", "Lcom/hank/clawlive/data/local/database/ChatMessageDao;", "(Lcom/hank/clawlive/data/local/database/ChatMessageDao;)V", "clearAllMessages", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getDistinctEntityIds", "Lkotlinx/coroutines/flow/Flow;", "", "", "getLastMessagesForWidget", "Lcom/hank/clawlive/data/local/database/ChatMessage;", "limit", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getMessageCount", "getMessagesAscending", "getMessagesForEntity", "entityId", "getRecentMessages", "getRecentMessagesSync", "getUserMessages", "markMessageSynced", "messageId", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "parseEntityMessage", "Lkotlin/Pair;", "Lcom/hank/clawlive/data/local/database/MessageType;", "", "message", "processEntityMessage", "entity", "Lcom/hank/clawlive/data/model/EntityStatus;", "(Lcom/hank/clawlive/data/model/EntityStatus;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "pruneOldMessages", "keepCount", "saveOutgoingMessage", "text", "entityIds", "source", "(Ljava/lang/String;Ljava/util/List;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Companion", "app_debug"})
public final class ChatRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.hank.clawlive.data.local.database.ChatMessageDao chatDao = null;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile com.hank.clawlive.data.repository.ChatRepository INSTANCE;
    
    /**
     * Deduplication time window in milliseconds (5 minutes)
     */
    private static final long DEDUP_WINDOW_MS = 300000L;
    @org.jetbrains.annotations.NotNull()
    public static final com.hank.clawlive.data.repository.ChatRepository.Companion Companion = null;
    
    private ChatRepository(com.hank.clawlive.data.local.database.ChatMessageDao chatDao) {
        super();
    }
    
    /**
     * Get all messages (newest first) as Flow
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.hank.clawlive.data.local.database.ChatMessage>> getRecentMessages(int limit) {
        return null;
    }
    
    /**
     * Get messages in ascending order (oldest first) for chat display
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.hank.clawlive.data.local.database.ChatMessage>> getMessagesAscending(int limit) {
        return null;
    }
    
    /**
     * Get messages for a specific entity
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.hank.clawlive.data.local.database.ChatMessage>> getMessagesForEntity(int entityId, int limit) {
        return null;
    }
    
    /**
     * Get only user-sent messages
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.hank.clawlive.data.local.database.ChatMessage>> getUserMessages(int limit) {
        return null;
    }
    
    /**
     * Get distinct entity IDs that have messages (for smart filter chips)
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<java.lang.Integer>> getDistinctEntityIds() {
        return null;
    }
    
    /**
     * Save outgoing user message BEFORE sending to API
     * Returns the inserted message ID for tracking sync status
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object saveOutgoingMessage(@org.jetbrains.annotations.NotNull()
    java.lang.String text, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.Integer> entityIds, @org.jetbrains.annotations.NotNull()
    java.lang.String source, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    /**
     * Mark message as synced after API call succeeds
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object markMessageSynced(long messageId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * Process incoming entity message from status polling
     * Uses deduplication to avoid storing the same message twice
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object processEntityMessage(@org.jetbrains.annotations.NotNull()
    com.hank.clawlive.data.model.EntityStatus entity, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * Parse entity message to detect type and clean up text
     * Pattern for entity-to-entity: "entity:{ID}:{CHARACTER}: {message}"
     */
    private final kotlin.Pair<com.hank.clawlive.data.local.database.MessageType, java.lang.String> parseEntityMessage(java.lang.String message) {
        return null;
    }
    
    /**
     * Get last N messages for widget (async)
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getLastMessagesForWidget(int limit, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.hank.clawlive.data.local.database.ChatMessage>> $completion) {
        return null;
    }
    
    /**
     * Get recent messages synchronously for widget (called with runBlocking)
     * Returns messages in descending order (newest first)
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getRecentMessagesSync(int limit, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.hank.clawlive.data.local.database.ChatMessage>> $completion) {
        return null;
    }
    
    /**
     * Get total message count
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getMessageCount(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    /**
     * Prune old messages to prevent database bloat
     * Keeps the last 500 messages by default
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object pruneOldMessages(int keepCount, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * Clear all messages (for testing/reset)
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object clearAllMessages(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0007\u001a\u00020\u00062\u0006\u0010\b\u001a\u00020\tR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\n"}, d2 = {"Lcom/hank/clawlive/data/repository/ChatRepository$Companion;", "", "()V", "DEDUP_WINDOW_MS", "", "INSTANCE", "Lcom/hank/clawlive/data/repository/ChatRepository;", "getInstance", "context", "Landroid/content/Context;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.hank.clawlive.data.repository.ChatRepository getInstance(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
            return null;
        }
    }
}
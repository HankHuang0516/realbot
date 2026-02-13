package com.hank.clawlive.data.local.database;

/**
 * Chat message entity for Room database
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\'\n\u0002\u0010 \n\u0002\b\u0003\b\u0087\b\u0018\u00002\u00020\u0001B\u0083\u0001\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0003\u0012\u0006\u0010\u0007\u001a\u00020\b\u0012\u0006\u0010\t\u001a\u00020\n\u0012\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u000e\u0012\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0002\u0010\u0012\u001a\u00020\b\u00a2\u0006\u0002\u0010\u0013J\t\u0010$\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010%\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010&\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\t\u0010\'\u001a\u00020\bH\u00c6\u0003J\t\u0010(\u001a\u00020\u0005H\u00c6\u0003J\t\u0010)\u001a\u00020\u0003H\u00c6\u0003J\t\u0010*\u001a\u00020\bH\u00c6\u0003J\t\u0010+\u001a\u00020\nH\u00c6\u0003J\u000b\u0010,\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010-\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u0010\u0010.\u001a\u0004\u0018\u00010\u000eH\u00c6\u0003\u00a2\u0006\u0002\u0010\u0018J\u000b\u0010/\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u0092\u0001\u00100\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\n2\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u000e2\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010\u0012\u001a\u00020\bH\u00c6\u0001\u00a2\u0006\u0002\u00101J\u0013\u00102\u001a\u00020\b2\b\u00103\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\u0006\u00104\u001a\u00020\u0005J\f\u00105\u001a\b\u0012\u0004\u0012\u00020\u000e06J\t\u00107\u001a\u00020\u000eH\u00d6\u0001J\t\u00108\u001a\u00020\u0005H\u00d6\u0001R\u0013\u0010\u0011\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0013\u0010\u0010\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0015R\u0015\u0010\r\u001a\u0004\u0018\u00010\u000e\u00a2\u0006\n\n\u0002\u0010\u0019\u001a\u0004\b\u0017\u0010\u0018R\u0013\u0010\u000f\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0015R\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001cR\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\u001dR\u0011\u0010\u0012\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u001dR\u0011\u0010\t\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001fR\u0013\u0010\u000b\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u0015R\u0013\u0010\f\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\u0015R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010\u0015R\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010\u001c\u00a8\u00069"}, d2 = {"Lcom/hank/clawlive/data/local/database/ChatMessage;", "", "id", "", "text", "", "timestamp", "isFromUser", "", "messageType", "Lcom/hank/clawlive/data/local/database/MessageType;", "source", "targetEntityIds", "fromEntityId", "", "fromEntityName", "fromEntityCharacter", "deduplicationKey", "isSynced", "(JLjava/lang/String;JZLcom/hank/clawlive/data/local/database/MessageType;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Z)V", "getDeduplicationKey", "()Ljava/lang/String;", "getFromEntityCharacter", "getFromEntityId", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getFromEntityName", "getId", "()J", "()Z", "getMessageType", "()Lcom/hank/clawlive/data/local/database/MessageType;", "getSource", "getTargetEntityIds", "getText", "getTimestamp", "component1", "component10", "component11", "component12", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(JLjava/lang/String;JZLcom/hank/clawlive/data/local/database/MessageType;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Z)Lcom/hank/clawlive/data/local/database/ChatMessage;", "equals", "other", "getSenderDisplayName", "getTargetEntityIdList", "", "hashCode", "toString", "app_debug"})
@androidx.room.Entity(tableName = "chat_messages")
public final class ChatMessage {
    @androidx.room.PrimaryKey(autoGenerate = true)
    private final long id = 0L;
    
    /**
     * Message text content
     */
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String text = null;
    
    /**
     * Timestamp in milliseconds
     */
    private final long timestamp = 0L;
    
    /**
     * true = RIGHT side (user sent), false = LEFT side (received from entity)
     */
    private final boolean isFromUser = false;
    
    /**
     * Message type for categorization
     */
    @org.jetbrains.annotations.NotNull()
    private final com.hank.clawlive.data.local.database.MessageType messageType = null;
    
    /**
     * For user messages: source identifier (android_widget, android_main, etc.)
     */
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String source = null;
    
    /**
     * For user messages: target entity IDs (comma-separated for broadcast, e.g., "0,1,2")
     */
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String targetEntityIds = null;
    
    /**
     * For entity messages: which entity sent it (0-3)
     */
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer fromEntityId = null;
    
    /**
     * For entity messages: entity name
     */
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String fromEntityName = null;
    
    /**
     * For entity messages: entity character type (LOBSTER, PIG, etc.)
     */
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String fromEntityCharacter = null;
    
    /**
     * Deduplication key: entityId + messageHash + timestampBucket
     */
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String deduplicationKey = null;
    
    /**
     * true after API confirms message was sent successfully
     */
    private final boolean isSynced = false;
    
    public ChatMessage(long id, @org.jetbrains.annotations.NotNull()
    java.lang.String text, long timestamp, boolean isFromUser, @org.jetbrains.annotations.NotNull()
    com.hank.clawlive.data.local.database.MessageType messageType, @org.jetbrains.annotations.Nullable()
    java.lang.String source, @org.jetbrains.annotations.Nullable()
    java.lang.String targetEntityIds, @org.jetbrains.annotations.Nullable()
    java.lang.Integer fromEntityId, @org.jetbrains.annotations.Nullable()
    java.lang.String fromEntityName, @org.jetbrains.annotations.Nullable()
    java.lang.String fromEntityCharacter, @org.jetbrains.annotations.Nullable()
    java.lang.String deduplicationKey, boolean isSynced) {
        super();
    }
    
    public final long getId() {
        return 0L;
    }
    
    /**
     * Message text content
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getText() {
        return null;
    }
    
    /**
     * Timestamp in milliseconds
     */
    public final long getTimestamp() {
        return 0L;
    }
    
    /**
     * true = RIGHT side (user sent), false = LEFT side (received from entity)
     */
    public final boolean isFromUser() {
        return false;
    }
    
    /**
     * Message type for categorization
     */
    @org.jetbrains.annotations.NotNull()
    public final com.hank.clawlive.data.local.database.MessageType getMessageType() {
        return null;
    }
    
    /**
     * For user messages: source identifier (android_widget, android_main, etc.)
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getSource() {
        return null;
    }
    
    /**
     * For user messages: target entity IDs (comma-separated for broadcast, e.g., "0,1,2")
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getTargetEntityIds() {
        return null;
    }
    
    /**
     * For entity messages: which entity sent it (0-3)
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getFromEntityId() {
        return null;
    }
    
    /**
     * For entity messages: entity name
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getFromEntityName() {
        return null;
    }
    
    /**
     * For entity messages: entity character type (LOBSTER, PIG, etc.)
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getFromEntityCharacter() {
        return null;
    }
    
    /**
     * Deduplication key: entityId + messageHash + timestampBucket
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getDeduplicationKey() {
        return null;
    }
    
    /**
     * true after API confirms message was sent successfully
     */
    public final boolean isSynced() {
        return false;
    }
    
    /**
     * Get display name for the message sender
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getSenderDisplayName() {
        return null;
    }
    
    /**
     * Get target entity IDs as a list
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.Integer> getTargetEntityIdList() {
        return null;
    }
    
    public final long component1() {
        return 0L;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component10() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component11() {
        return null;
    }
    
    public final boolean component12() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component2() {
        return null;
    }
    
    public final long component3() {
        return 0L;
    }
    
    public final boolean component4() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hank.clawlive.data.local.database.MessageType component5() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component7() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component8() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hank.clawlive.data.local.database.ChatMessage copy(long id, @org.jetbrains.annotations.NotNull()
    java.lang.String text, long timestamp, boolean isFromUser, @org.jetbrains.annotations.NotNull()
    com.hank.clawlive.data.local.database.MessageType messageType, @org.jetbrains.annotations.Nullable()
    java.lang.String source, @org.jetbrains.annotations.Nullable()
    java.lang.String targetEntityIds, @org.jetbrains.annotations.Nullable()
    java.lang.Integer fromEntityId, @org.jetbrains.annotations.Nullable()
    java.lang.String fromEntityName, @org.jetbrains.annotations.Nullable()
    java.lang.String fromEntityCharacter, @org.jetbrains.annotations.Nullable()
    java.lang.String deduplicationKey, boolean isSynced) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}
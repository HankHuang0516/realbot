package com.hank.clawlive.data.local.database;

/**
 * Message types for chat history display
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0006\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/hank/clawlive/data/local/database/MessageType;", "", "(Ljava/lang/String;I)V", "USER_TO_ENTITY", "USER_BROADCAST", "ENTITY_RESPONSE", "ENTITY_TO_ENTITY", "app_release"})
public enum MessageType {
    /*public static final*/ USER_TO_ENTITY /* = new USER_TO_ENTITY() */,
    /*public static final*/ USER_BROADCAST /* = new USER_BROADCAST() */,
    /*public static final*/ ENTITY_RESPONSE /* = new ENTITY_RESPONSE() */,
    /*public static final*/ ENTITY_TO_ENTITY /* = new ENTITY_TO_ENTITY() */;
    
    MessageType() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.hank.clawlive.data.local.database.MessageType> getEntries() {
        return null;
    }
}
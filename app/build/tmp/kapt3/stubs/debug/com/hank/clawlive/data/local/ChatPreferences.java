package com.hank.clawlive.data.local;

/**
 * Manages chat-related preferences for widget display
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\b\n\u0002\u0010\t\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\u0010\b\n\u0002\b\u0002\u0018\u0000 \u001f2\u00020\u0001:\u0001\u001fB\u000f\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\u0017\u001a\u00020\u0018J\u0006\u0010\u0019\u001a\u00020\u0006J\u001c\u0010\u001a\u001a\u00020\u00182\u0006\u0010\u001b\u001a\u00020\u00062\f\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u001e0\u001dR(\u0010\u0007\u001a\u0004\u0018\u00010\u00062\b\u0010\u0005\u001a\u0004\u0018\u00010\u00068F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b\b\u0010\t\"\u0004\b\n\u0010\u000bR(\u0010\f\u001a\u0004\u0018\u00010\u00062\b\u0010\u0005\u001a\u0004\u0018\u00010\u00068F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b\r\u0010\t\"\u0004\b\u000e\u0010\u000bR$\u0010\u0010\u001a\u00020\u000f2\u0006\u0010\u0005\u001a\u00020\u000f8F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b\u0011\u0010\u0012\"\u0004\b\u0013\u0010\u0014R\u000e\u0010\u0015\u001a\u00020\u0016X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006 "}, d2 = {"Lcom/hank/clawlive/data/local/ChatPreferences;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "value", "", "lastMessage", "getLastMessage", "()Ljava/lang/String;", "setLastMessage", "(Ljava/lang/String;)V", "lastMessageEntityIds", "getLastMessageEntityIds", "setLastMessageEntityIds", "", "lastMessageTimestamp", "getLastMessageTimestamp", "()J", "setLastMessageTimestamp", "(J)V", "prefs", "Landroid/content/SharedPreferences;", "clear", "", "getWidgetDisplayText", "saveLastMessage", "message", "entityIds", "", "", "Companion", "app_debug"})
public final class ChatPreferences {
    @org.jetbrains.annotations.NotNull()
    private final android.content.SharedPreferences prefs = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PREFS_NAME = "chat_prefs";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_LAST_MESSAGE = "last_message";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_LAST_TIMESTAMP = "last_timestamp";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_LAST_ENTITY_IDS = "last_entity_ids";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String DEFAULT_PLACEHOLDER = "Tap to chat with entities...";
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile com.hank.clawlive.data.local.ChatPreferences instance;
    @org.jetbrains.annotations.NotNull()
    public static final com.hank.clawlive.data.local.ChatPreferences.Companion Companion = null;
    
    private ChatPreferences(android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getLastMessage() {
        return null;
    }
    
    public final void setLastMessage(@org.jetbrains.annotations.Nullable()
    java.lang.String value) {
    }
    
    public final long getLastMessageTimestamp() {
        return 0L;
    }
    
    public final void setLastMessageTimestamp(long value) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getLastMessageEntityIds() {
        return null;
    }
    
    public final void setLastMessageEntityIds(@org.jetbrains.annotations.Nullable()
    java.lang.String value) {
    }
    
    /**
     * Save message with metadata
     */
    public final void saveLastMessage(@org.jetbrains.annotations.NotNull()
    java.lang.String message, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.Integer> entityIds) {
    }
    
    /**
     * Get formatted display text for widget
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getWidgetDisplayText() {
        return null;
    }
    
    /**
     * Clear chat history
     */
    public final void clear() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u000b\u001a\u00020\n2\u0006\u0010\f\u001a\u00020\rR\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lcom/hank/clawlive/data/local/ChatPreferences$Companion;", "", "()V", "DEFAULT_PLACEHOLDER", "", "KEY_LAST_ENTITY_IDS", "KEY_LAST_MESSAGE", "KEY_LAST_TIMESTAMP", "PREFS_NAME", "instance", "Lcom/hank/clawlive/data/local/ChatPreferences;", "getInstance", "context", "Landroid/content/Context;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.hank.clawlive.data.local.ChatPreferences getInstance(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
            return null;
        }
    }
}
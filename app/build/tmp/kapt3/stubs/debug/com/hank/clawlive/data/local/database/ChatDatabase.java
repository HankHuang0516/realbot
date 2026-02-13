package com.hank.clawlive.data.local.database;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\'\u0018\u0000 \u00052\u00020\u0001:\u0001\u0005B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H&\u00a8\u0006\u0006"}, d2 = {"Lcom/hank/clawlive/data/local/database/ChatDatabase;", "Landroidx/room/RoomDatabase;", "()V", "chatMessageDao", "Lcom/hank/clawlive/data/local/database/ChatMessageDao;", "Companion", "app_debug"})
@androidx.room.Database(entities = {com.hank.clawlive.data.local.database.ChatMessage.class}, version = 1, exportSchema = false)
@androidx.room.TypeConverters(value = {com.hank.clawlive.data.local.database.Converters.class})
public abstract class ChatDatabase extends androidx.room.RoomDatabase {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String DATABASE_NAME = "chat_history.db";
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile com.hank.clawlive.data.local.database.ChatDatabase INSTANCE;
    @org.jetbrains.annotations.NotNull()
    public static final com.hank.clawlive.data.local.database.ChatDatabase.Companion Companion = null;
    
    public ChatDatabase() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.hank.clawlive.data.local.database.ChatMessageDao chatMessageDao();
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0007\u001a\u00020\u00062\u0006\u0010\b\u001a\u00020\tH\u0002J\u000e\u0010\n\u001a\u00020\u000b2\u0006\u0010\b\u001a\u00020\tJ\u000e\u0010\f\u001a\u00020\u00062\u0006\u0010\b\u001a\u00020\tR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\r"}, d2 = {"Lcom/hank/clawlive/data/local/database/ChatDatabase$Companion;", "", "()V", "DATABASE_NAME", "", "INSTANCE", "Lcom/hank/clawlive/data/local/database/ChatDatabase;", "buildDatabase", "context", "Landroid/content/Context;", "getDao", "Lcom/hank/clawlive/data/local/database/ChatMessageDao;", "getInstance", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.hank.clawlive.data.local.database.ChatDatabase getInstance(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
            return null;
        }
        
        private final com.hank.clawlive.data.local.database.ChatDatabase buildDatabase(android.content.Context context) {
            return null;
        }
        
        /**
         * Get DAO directly (convenience method)
         */
        @org.jetbrains.annotations.NotNull()
        public final com.hank.clawlive.data.local.database.ChatMessageDao getDao(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
            return null;
        }
    }
}
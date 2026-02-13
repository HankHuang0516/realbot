package com.hank.clawlive.data.local.database;

/**
 * Type converters for Room database
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0010\u0010\u0007\u001a\u00020\u00062\u0006\u0010\u0005\u001a\u00020\u0004H\u0007\u00a8\u0006\b"}, d2 = {"Lcom/hank/clawlive/data/local/database/Converters;", "", "()V", "fromMessageType", "", "value", "Lcom/hank/clawlive/data/local/database/MessageType;", "toMessageType", "app_release"})
public final class Converters {
    
    public Converters() {
        super();
    }
    
    @androidx.room.TypeConverter()
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String fromMessageType(@org.jetbrains.annotations.NotNull()
    com.hank.clawlive.data.local.database.MessageType value) {
        return null;
    }
    
    @androidx.room.TypeConverter()
    @org.jetbrains.annotations.NotNull()
    public final com.hank.clawlive.data.local.database.MessageType toMessageType(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
        return null;
    }
}
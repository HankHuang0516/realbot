package com.hank.clawlive.data.local;

/**
 * Manages entity avatar icons.
 * Each entity (identified by entityId) has a customizable avatar emoji.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\u0018\u0000 \u000e2\u00020\u0001:\u0001\u000eB\u000f\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nJ\u0016\u0010\u000b\u001a\u00020\f2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\r\u001a\u00020\bR\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"Lcom/hank/clawlive/data/local/EntityAvatarManager;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "prefs", "Landroid/content/SharedPreferences;", "getAvatar", "", "entityId", "", "setAvatar", "", "avatar", "Companion", "app_debug"})
public final class EntityAvatarManager {
    @org.jetbrains.annotations.NotNull()
    private final android.content.SharedPreferences prefs = null;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile com.hank.clawlive.data.local.EntityAvatarManager INSTANCE;
    
    /**
     * Default avatar for each entity slot
     */
    @org.jetbrains.annotations.NotNull()
    private static final java.util.Map<java.lang.Integer, java.lang.String> DEFAULT_AVATARS = null;
    
    /**
     * 30 avatar options organized by category (6 columns x 5 rows)
     */
    @org.jetbrains.annotations.NotNull()
    private static final java.util.List<java.lang.String> AVATAR_OPTIONS = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.hank.clawlive.data.local.EntityAvatarManager.Companion Companion = null;
    
    private EntityAvatarManager(android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getAvatar(int entityId) {
        return null;
    }
    
    public final void setAvatar(int entityId, @org.jetbrains.annotations.NotNull()
    java.lang.String avatar) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010$\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u000f\u001a\u00020\u000e2\u0006\u0010\u0010\u001a\u00020\u0011R\u0017\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007R\u001d\u0010\b\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u00050\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0010\u0010\r\u001a\u0004\u0018\u00010\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lcom/hank/clawlive/data/local/EntityAvatarManager$Companion;", "", "()V", "AVATAR_OPTIONS", "", "", "getAVATAR_OPTIONS", "()Ljava/util/List;", "DEFAULT_AVATARS", "", "", "getDEFAULT_AVATARS", "()Ljava/util/Map;", "INSTANCE", "Lcom/hank/clawlive/data/local/EntityAvatarManager;", "getInstance", "context", "Landroid/content/Context;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.hank.clawlive.data.local.EntityAvatarManager getInstance(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
            return null;
        }
        
        /**
         * Default avatar for each entity slot
         */
        @org.jetbrains.annotations.NotNull()
        public final java.util.Map<java.lang.Integer, java.lang.String> getDEFAULT_AVATARS() {
            return null;
        }
        
        /**
         * 30 avatar options organized by category (6 columns x 5 rows)
         */
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<java.lang.String> getAVATAR_OPTIONS() {
            return null;
        }
    }
}
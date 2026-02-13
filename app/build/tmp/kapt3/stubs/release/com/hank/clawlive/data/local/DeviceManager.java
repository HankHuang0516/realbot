package com.hank.clawlive.data.local;

/**
 * Manages device credentials securely using EncryptedSharedPreferences
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\r\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\u0018\u0000 &2\u00020\u0001:\u0001&B\u000f\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0018\u0010#\u001a\u00020\"2\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u001f\u001a\u00020 H\u0002J\u0006\u0010$\u001a\u00020%R\u0016\u0010\u0005\u001a\n \u0006*\u0004\u0018\u00010\u00030\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0011\u0010\u0007\u001a\u00020\b8F\u00a2\u0006\u0006\u001a\u0004\b\t\u0010\nR$\u0010\r\u001a\u00020\f2\u0006\u0010\u000b\u001a\u00020\f8F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b\u000e\u0010\u000f\"\u0004\b\u0010\u0010\u0011R(\u0010\u0012\u001a\u0004\u0018\u00010\b2\b\u0010\u000b\u001a\u0004\u0018\u00010\b8F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b\u0013\u0010\n\"\u0004\b\u0014\u0010\u0015R\u0011\u0010\u0016\u001a\u00020\b8F\u00a2\u0006\u0006\u001a\u0004\b\u0017\u0010\nR\u0011\u0010\u0018\u001a\u00020\b8F\u00a2\u0006\u0006\u001a\u0004\b\u0019\u0010\nR$\u0010\u001b\u001a\u00020\u001a2\u0006\u0010\u000b\u001a\u00020\u001a8F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b\u001b\u0010\u001c\"\u0004\b\u001d\u0010\u001eR\u000e\u0010\u001f\u001a\u00020 X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010!\u001a\u00020\"X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\'"}, d2 = {"Lcom/hank/clawlive/data/local/DeviceManager;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "appContext", "kotlin.jvm.PlatformType", "appVersion", "", "getAppVersion", "()Ljava/lang/String;", "value", "", "bindingCodeExpiry", "getBindingCodeExpiry", "()J", "setBindingCodeExpiry", "(J)V", "currentBindingCode", "getCurrentBindingCode", "setCurrentBindingCode", "(Ljava/lang/String;)V", "deviceId", "getDeviceId", "deviceSecret", "getDeviceSecret", "", "isBound", "()Z", "setBound", "(Z)V", "masterKey", "Landroidx/security/crypto/MasterKey;", "prefs", "Landroid/content/SharedPreferences;", "createEncryptedPrefs", "reset", "", "Companion", "app_release"})
public final class DeviceManager {
    private final android.content.Context appContext = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.security.crypto.MasterKey masterKey = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.SharedPreferences prefs = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PREFS_NAME = "realbot_device_prefs";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_DEVICE_ID = "device_id";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_DEVICE_SECRET = "device_secret";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_IS_BOUND = "is_bound";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_BINDING_CODE = "binding_code";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_BINDING_EXPIRY = "binding_expiry";
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile com.hank.clawlive.data.local.DeviceManager instance;
    @org.jetbrains.annotations.NotNull()
    public static final com.hank.clawlive.data.local.DeviceManager.Companion Companion = null;
    
    private DeviceManager(android.content.Context context) {
        super();
    }
    
    private final android.content.SharedPreferences createEncryptedPrefs(android.content.Context context, androidx.security.crypto.MasterKey masterKey) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getDeviceId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getDeviceSecret() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getAppVersion() {
        return null;
    }
    
    public final boolean isBound() {
        return false;
    }
    
    public final void setBound(boolean value) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getCurrentBindingCode() {
        return null;
    }
    
    public final void setCurrentBindingCode(@org.jetbrains.annotations.Nullable()
    java.lang.String value) {
    }
    
    public final long getBindingCodeExpiry() {
        return 0L;
    }
    
    public final void setBindingCodeExpiry(long value) {
    }
    
    /**
     * Reset device credentials (for re-binding)
     */
    public final void reset() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\f\u001a\u00020\u000b2\u0006\u0010\r\u001a\u00020\u000eR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u0010\u0010\n\u001a\u0004\u0018\u00010\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"Lcom/hank/clawlive/data/local/DeviceManager$Companion;", "", "()V", "KEY_BINDING_CODE", "", "KEY_BINDING_EXPIRY", "KEY_DEVICE_ID", "KEY_DEVICE_SECRET", "KEY_IS_BOUND", "PREFS_NAME", "instance", "Lcom/hank/clawlive/data/local/DeviceManager;", "getInstance", "context", "Landroid/content/Context;", "app_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.hank.clawlive.data.local.DeviceManager getInstance(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
            return null;
        }
    }
}
package com.hank.clawlive.data.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000V\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000e2\b\b\u0002\u0010\u0010\u001a\u00020\u0011J\u0016\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00130\u000e2\b\b\u0002\u0010\u0010\u001a\u00020\u0011J\u0010\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u0015H\u0002J\u0018\u0010\u0017\u001a\u00020\u00182\b\b\u0002\u0010\u0019\u001a\u00020\u001aH\u0086@\u00a2\u0006\u0002\u0010\u001bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001c"}, d2 = {"Lcom/hank/clawlive/data/repository/StateRepository;", "", "api", "Lcom/hank/clawlive/data/remote/ClawApiService;", "context", "Landroid/content/Context;", "(Lcom/hank/clawlive/data/remote/ClawApiService;Landroid/content/Context;)V", "chatRepository", "Lcom/hank/clawlive/data/repository/ChatRepository;", "deviceManager", "Lcom/hank/clawlive/data/local/DeviceManager;", "layoutPrefs", "Lcom/hank/clawlive/data/local/LayoutPreferences;", "getMultiEntityStatusFlow", "Lkotlinx/coroutines/flow/Flow;", "Lcom/hank/clawlive/data/model/MultiEntityResponse;", "intervalMs", "", "getStatusFlow", "Lcom/hank/clawlive/data/model/AgentStatus;", "translateSystemMessage", "Lcom/hank/clawlive/data/model/EntityStatus;", "entity", "wakeUp", "", "entityId", "", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_release"})
public final class StateRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.hank.clawlive.data.remote.ClawApiService api = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final com.hank.clawlive.data.local.LayoutPreferences layoutPrefs = null;
    @org.jetbrains.annotations.NotNull()
    private final com.hank.clawlive.data.local.DeviceManager deviceManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.hank.clawlive.data.repository.ChatRepository chatRepository = null;
    
    public StateRepository(@org.jetbrains.annotations.NotNull()
    com.hank.clawlive.data.remote.ClawApiService api, @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    /**
     * Polls the API every [intervalMs] for single entity status.
     * Now uses deviceId for v5 matrix architecture.
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<com.hank.clawlive.data.model.AgentStatus> getStatusFlow(long intervalMs) {
        return null;
    }
    
    /**
     * Polls the API every [intervalMs] for multi-entity status.
     * Filters to only show entities for THIS device (v5 matrix architecture).
     * Also processes entity messages for chat history.
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<com.hank.clawlive.data.model.MultiEntityResponse> getMultiEntityStatusFlow(long intervalMs) {
        return null;
    }
    
    /**
     * Wake up the agent (called when user taps the wallpaper)
     * Note: wakeUp now requires botSecret which client doesn't have.
     * This is a best-effort call that may fail silently.
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object wakeUp(int entityId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * Translate system marker messages to localized user-facing text.
     * Backend sends markers like [SYSTEM:WEBHOOK_ERROR] which need to be
     * converted to the device's locale language.
     */
    private final com.hank.clawlive.data.model.EntityStatus translateSystemMessage(com.hank.clawlive.data.model.EntityStatus entity) {
        return null;
    }
}
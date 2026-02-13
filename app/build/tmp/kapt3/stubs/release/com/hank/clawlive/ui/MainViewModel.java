package com.hank.clawlive.ui;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\u0014\u001a\u00020\u0015J\u0006\u0010\u0016\u001a\u00020\u0015J\u0006\u0010\u0017\u001a\u00020\u0015J\u000e\u0010\u0018\u001a\u00020\u00152\u0006\u0010\u0019\u001a\u00020\u001aJ\b\u0010\u001b\u001a\u00020\u0015H\u0002J\b\u0010\u001c\u001a\u00020\u0015H\u0002R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\n\u001a\u0004\u0018\u00010\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00070\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013\u00a8\u0006\u001d"}, d2 = {"Lcom/hank/clawlive/ui/MainViewModel;", "Landroidx/lifecycle/AndroidViewModel;", "application", "Landroid/app/Application;", "(Landroid/app/Application;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/hank/clawlive/ui/BindingUiState;", "api", "Lcom/hank/clawlive/data/remote/ClawApiService;", "countdownJob", "Lkotlinx/coroutines/Job;", "deviceManager", "Lcom/hank/clawlive/data/local/DeviceManager;", "layoutPrefs", "Lcom/hank/clawlive/data/local/LayoutPreferences;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "clearError", "", "generateBindingCode", "resetDevice", "selectEntity", "entityId", "", "startCountdown", "startStatusPolling", "app_release"})
public final class MainViewModel extends androidx.lifecycle.AndroidViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.hank.clawlive.data.local.DeviceManager deviceManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.hank.clawlive.data.local.LayoutPreferences layoutPrefs = null;
    @org.jetbrains.annotations.NotNull()
    private final com.hank.clawlive.data.remote.ClawApiService api = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.hank.clawlive.ui.BindingUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.hank.clawlive.ui.BindingUiState> uiState = null;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job countdownJob;
    
    public MainViewModel(@org.jetbrains.annotations.NotNull()
    android.app.Application application) {
        super(null);
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.hank.clawlive.ui.BindingUiState> getUiState() {
        return null;
    }
    
    /**
     * Select which entity to register (0-3)
     */
    public final void selectEntity(int entityId) {
    }
    
    /**
     * Generate a new binding code for the selected entity
     */
    public final void generateBindingCode() {
    }
    
    /**
     * Start countdown timer for binding code expiry
     */
    private final void startCountdown() {
    }
    
    /**
     * Start polling for agent status
     */
    private final void startStatusPolling() {
    }
    
    /**
     * Reset device and generate new credentials
     */
    public final void resetDevice() {
    }
    
    /**
     * Clear error message
     */
    public final void clearError() {
    }
}
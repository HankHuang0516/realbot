package com.hank.clawlive;

/**
 * Layout Editor Activity - allows users to drag entities to custom positions.
 * Replaces the old DebugRenderActivity.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\\\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001dH\u0002J\b\u0010\u001f\u001a\u00020 H\u0002J\b\u0010!\u001a\u00020 H\u0002J\u0012\u0010\"\u001a\u00020 2\b\u0010#\u001a\u0004\u0018\u00010$H\u0014J\b\u0010%\u001a\u00020 H\u0002J\b\u0010&\u001a\u00020 H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\nX\u0082.\u00a2\u0006\u0002\n\u0000R\u001b\u0010\f\u001a\u00020\r8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0010\u0010\u0011\u001a\u0004\b\u000e\u0010\u000fR\u000e\u0010\u0012\u001a\u00020\u0013X\u0082.\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0014\u001a\u00020\u00158BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0018\u0010\u0011\u001a\u0004\b\u0016\u0010\u0017R\u000e\u0010\u0019\u001a\u00020\u001aX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001b\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u0006\'"}, d2 = {"Lcom/hank/clawlive/DebugRenderActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "api", "Lcom/hank/clawlive/data/remote/ClawApiService;", "bottomControls", "Landroid/widget/LinearLayout;", "btnBack", "Landroid/widget/ImageButton;", "btnDone", "Lcom/google/android/material/button/MaterialButton;", "btnReset", "deviceManager", "Lcom/hank/clawlive/data/local/DeviceManager;", "getDeviceManager", "()Lcom/hank/clawlive/data/local/DeviceManager;", "deviceManager$delegate", "Lkotlin/Lazy;", "layoutEditorView", "Lcom/hank/clawlive/ui/LayoutEditorView;", "layoutPrefs", "Lcom/hank/clawlive/data/local/LayoutPreferences;", "getLayoutPrefs", "()Lcom/hank/clawlive/data/local/LayoutPreferences;", "layoutPrefs$delegate", "switchCustomLayout", "Lcom/google/android/material/materialswitch/MaterialSwitch;", "topBar", "dpToPx", "", "dp", "initViews", "", "loadBoundEntities", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "setupEdgeToEdgeInsets", "setupListeners", "app_release"})
public final class DebugRenderActivity extends androidx.appcompat.app.AppCompatActivity {
    private com.hank.clawlive.ui.LayoutEditorView layoutEditorView;
    private com.google.android.material.materialswitch.MaterialSwitch switchCustomLayout;
    private com.google.android.material.button.MaterialButton btnReset;
    private com.google.android.material.button.MaterialButton btnDone;
    private android.widget.ImageButton btnBack;
    private android.widget.LinearLayout topBar;
    private android.widget.LinearLayout bottomControls;
    @org.jetbrains.annotations.NotNull()
    private final com.hank.clawlive.data.remote.ClawApiService api = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy deviceManager$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy layoutPrefs$delegate = null;
    
    public DebugRenderActivity() {
        super();
    }
    
    private final com.hank.clawlive.data.local.DeviceManager getDeviceManager() {
        return null;
    }
    
    private final com.hank.clawlive.data.local.LayoutPreferences getLayoutPrefs() {
        return null;
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void setupEdgeToEdgeInsets() {
    }
    
    private final int dpToPx(int dp) {
        return 0;
    }
    
    private final void initViews() {
    }
    
    private final void setupListeners() {
    }
    
    private final void loadBoundEntities() {
    }
}
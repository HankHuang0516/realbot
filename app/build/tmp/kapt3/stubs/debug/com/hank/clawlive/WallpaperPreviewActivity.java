package com.hank.clawlive;

/**
 * Wallpaper Preview Activity - allows users to:
 * 1. Preview and position entities on wallpaper
 * 2. Select a custom background photo
 * 3. Set the live wallpaper
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000h\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0010\u0011\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\b\n\u0000\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\"\u001a\u00020#H\u0002J\b\u0010$\u001a\u00020#H\u0002J\u0012\u0010%\u001a\u00020#2\b\u0010&\u001a\u0004\u0018\u00010\'H\u0014J\b\u0010(\u001a\u00020#H\u0002J\b\u0010)\u001a\u00020#H\u0002J\b\u0010*\u001a\u00020#H\u0002J\b\u0010+\u001a\u00020#H\u0002J\b\u0010,\u001a\u00020#H\u0002J\f\u0010-\u001a\u00020.*\u00020.H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\nX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\nX\u0082.\u00a2\u0006\u0002\n\u0000R\u001b\u0010\r\u001a\u00020\u000e8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0011\u0010\u0012\u001a\u0004\b\u000f\u0010\u0010R\u001b\u0010\u0013\u001a\u00020\u00148BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0017\u0010\u0012\u001a\u0004\b\u0015\u0010\u0016R\u001a\u0010\u0018\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001b0\u001a0\u0019X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001c\u001a\u00020\u001dX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001e\u001a\u00020\u001fX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010 \u001a\u00020\u001fX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010!\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u0006/"}, d2 = {"Lcom/hank/clawlive/WallpaperPreviewActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "api", "Lcom/hank/clawlive/data/remote/ClawApiService;", "bottomControls", "Landroid/widget/LinearLayout;", "btnBack", "Landroid/widget/ImageButton;", "btnReset", "Lcom/google/android/material/button/MaterialButton;", "btnSelectPhoto", "btnSetWallpaper", "deviceManager", "Lcom/hank/clawlive/data/local/DeviceManager;", "getDeviceManager", "()Lcom/hank/clawlive/data/local/DeviceManager;", "deviceManager$delegate", "Lkotlin/Lazy;", "layoutPrefs", "Lcom/hank/clawlive/data/local/LayoutPreferences;", "getLayoutPrefs", "()Lcom/hank/clawlive/data/local/LayoutPreferences;", "layoutPrefs$delegate", "photoPickerLauncher", "Landroidx/activity/result/ActivityResultLauncher;", "", "", "previewView", "Lcom/hank/clawlive/ui/WallpaperPreviewView;", "switchBackground", "Lcom/google/android/material/materialswitch/MaterialSwitch;", "switchCustomLayout", "topBar", "initViews", "", "loadBoundEntities", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "openPhotoPicker", "openWallpaperChooser", "setupEdgeToEdgeInsets", "setupListeners", "updatePhotoButtonVisibility", "dpToPx", "", "app_debug"})
public final class WallpaperPreviewActivity extends androidx.appcompat.app.AppCompatActivity {
    private com.hank.clawlive.ui.WallpaperPreviewView previewView;
    private com.google.android.material.materialswitch.MaterialSwitch switchCustomLayout;
    private com.google.android.material.materialswitch.MaterialSwitch switchBackground;
    private com.google.android.material.button.MaterialButton btnSelectPhoto;
    private com.google.android.material.button.MaterialButton btnReset;
    private com.google.android.material.button.MaterialButton btnSetWallpaper;
    private android.widget.ImageButton btnBack;
    private android.widget.LinearLayout topBar;
    private android.widget.LinearLayout bottomControls;
    @org.jetbrains.annotations.NotNull()
    private final com.hank.clawlive.data.remote.ClawApiService api = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy deviceManager$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy layoutPrefs$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.activity.result.ActivityResultLauncher<java.lang.String[]> photoPickerLauncher = null;
    
    public WallpaperPreviewActivity() {
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
    
    private final void initViews() {
    }
    
    /**
     * Apply WindowInsets for edge-to-edge display.
     * Background extends to edges, but interactive UI avoids system bars.
     */
    private final void setupEdgeToEdgeInsets() {
    }
    
    private final int dpToPx(int $this$dpToPx) {
        return 0;
    }
    
    private final void setupListeners() {
    }
    
    private final void updatePhotoButtonVisibility() {
    }
    
    private final void openPhotoPicker() {
    }
    
    private final void openWallpaperChooser() {
    }
    
    private final void loadBoundEntities() {
    }
}
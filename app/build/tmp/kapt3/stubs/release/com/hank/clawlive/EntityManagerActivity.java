package com.hank.clawlive;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000l\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\rH\u0002J\u0010\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u001bH\u0002J\u0010\u0010\u001d\u001a\u00020\u001b2\u0006\u0010\u001e\u001a\u00020\u001fH\u0002J\b\u0010 \u001a\u00020\u0018H\u0002J\b\u0010!\u001a\u00020\u0018H\u0002J\u0012\u0010\"\u001a\u00020\u00182\b\u0010#\u001a\u0004\u0018\u00010$H\u0014J\u0010\u0010%\u001a\u00020\u00182\u0006\u0010&\u001a\u00020\u001bH\u0002J\b\u0010\'\u001a\u00020\u0018H\u0002J\b\u0010(\u001a\u00020\u0018H\u0002J\u0010\u0010)\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\rH\u0002J\u0010\u0010*\u001a\u00020\u00182\u0006\u0010+\u001a\u00020,H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082.\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0015X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\u0015X\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u0006-"}, d2 = {"Lcom/hank/clawlive/EntityManagerActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "api", "Lcom/hank/clawlive/data/remote/ClawApiService;", "bottomBar", "Landroid/widget/LinearLayout;", "btnBack", "Landroid/widget/ImageButton;", "btnRefresh", "Lcom/google/android/material/button/MaterialButton;", "currentEntities", "", "Lcom/hank/clawlive/data/model/EntityStatus;", "deviceManager", "Lcom/hank/clawlive/data/local/DeviceManager;", "entityListContainer", "layoutPrefs", "Lcom/hank/clawlive/data/local/LayoutPreferences;", "topBar", "tvEmptyState", "Landroid/widget/TextView;", "tvEntityCount", "addEntityCard", "", "entity", "dpToPx", "", "dp", "getStateBadgeBackground", "state", "", "initViews", "loadEntities", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "removeEntity", "entityId", "setupEdgeToEdgeInsets", "setupListeners", "showRemoveConfirmDialog", "updateEntityList", "response", "Lcom/hank/clawlive/data/model/MultiEntityResponse;", "app_release"})
public final class EntityManagerActivity extends androidx.appcompat.app.AppCompatActivity {
    private android.widget.ImageButton btnBack;
    private android.widget.TextView tvEntityCount;
    private android.widget.LinearLayout entityListContainer;
    private android.widget.TextView tvEmptyState;
    private com.google.android.material.button.MaterialButton btnRefresh;
    private android.widget.LinearLayout topBar;
    private android.widget.LinearLayout bottomBar;
    private com.hank.clawlive.data.local.LayoutPreferences layoutPrefs;
    private com.hank.clawlive.data.local.DeviceManager deviceManager;
    @org.jetbrains.annotations.NotNull()
    private final com.hank.clawlive.data.remote.ClawApiService api = null;
    @org.jetbrains.annotations.NotNull()
    private java.util.List<com.hank.clawlive.data.model.EntityStatus> currentEntities;
    
    public EntityManagerActivity() {
        super();
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
    
    private final void loadEntities() {
    }
    
    private final void updateEntityList(com.hank.clawlive.data.model.MultiEntityResponse response) {
    }
    
    private final void addEntityCard(com.hank.clawlive.data.model.EntityStatus entity) {
    }
    
    private final int getStateBadgeBackground(java.lang.String state) {
        return 0;
    }
    
    private final void showRemoveConfirmDialog(com.hank.clawlive.data.model.EntityStatus entity) {
    }
    
    private final void removeEntity(int entityId) {
    }
}
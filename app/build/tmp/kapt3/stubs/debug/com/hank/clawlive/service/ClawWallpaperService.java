package com.hank.clawlive.service;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\u00020\u0001:\u0001\bB\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H\u0016J\f\u0010\u0005\u001a\u00060\u0006R\u00020\u0001H\u0016J\b\u0010\u0007\u001a\u00020\u0004H\u0016\u00a8\u0006\t"}, d2 = {"Lcom/hank/clawlive/service/ClawWallpaperService;", "Landroid/service/wallpaper/WallpaperService;", "()V", "onCreate", "", "onCreateEngine", "Landroid/service/wallpaper/WallpaperService$Engine;", "onDestroy", "ClawEngine", "app_debug"})
public final class ClawWallpaperService extends android.service.wallpaper.WallpaperService {
    
    public ClawWallpaperService() {
        super();
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public android.service.wallpaper.WallpaperService.Engine onCreateEngine() {
        return null;
    }
    
    @java.lang.Override()
    public void onDestroy() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000^\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0086\u0004\u0018\u00002\u00060\u0001R\u00020\u0002B\u0005\u00a2\u0006\u0002\u0010\u0003J\b\u0010\u0016\u001a\u00020\u0017H\u0002J\b\u0010\u0018\u001a\u00020\u0017H\u0002J\u0012\u0010\u0019\u001a\u00020\u00172\b\u0010\u001a\u001a\u0004\u0018\u00010\u001bH\u0016J\u0012\u0010\u001c\u001a\u00020\u00172\b\u0010\u001d\u001a\u0004\u0018\u00010\u001bH\u0016J\u0012\u0010\u001e\u001a\u00020\u00172\b\u0010\u001f\u001a\u0004\u0018\u00010 H\u0016J\u0010\u0010!\u001a\u00020\u00172\u0006\u0010\u0015\u001a\u00020\u0010H\u0016R\u0014\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0014X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\""}, d2 = {"Lcom/hank/clawlive/service/ClawWallpaperService$ClawEngine;", "Landroid/service/wallpaper/WallpaperService$Engine;", "Landroid/service/wallpaper/WallpaperService;", "(Lcom/hank/clawlive/service/ClawWallpaperService;)V", "currentEntities", "", "Lcom/hank/clawlive/data/model/EntityStatus;", "currentStatus", "Lcom/hank/clawlive/data/model/AgentStatus;", "drawRunnable", "Ljava/lang/Runnable;", "engineScope", "Lkotlinx/coroutines/CoroutineScope;", "handler", "Landroid/os/Handler;", "multiEntityMode", "", "renderer", "Lcom/hank/clawlive/engine/ClawRenderer;", "repository", "Lcom/hank/clawlive/data/repository/StateRepository;", "visible", "draw", "", "observeStatus", "onCreate", "surfaceHolder", "Landroid/view/SurfaceHolder;", "onSurfaceDestroyed", "holder", "onTouchEvent", "event", "Landroid/view/MotionEvent;", "onVisibilityChanged", "app_debug"})
    public final class ClawEngine extends android.service.wallpaper.WallpaperService.Engine {
        @org.jetbrains.annotations.NotNull()
        private final android.os.Handler handler = null;
        private boolean visible = false;
        @org.jetbrains.annotations.NotNull()
        private final com.hank.clawlive.engine.ClawRenderer renderer = null;
        @org.jetbrains.annotations.NotNull()
        private final com.hank.clawlive.data.repository.StateRepository repository = null;
        @org.jetbrains.annotations.NotNull()
        private final kotlinx.coroutines.CoroutineScope engineScope = null;
        private final boolean multiEntityMode = true;
        @org.jetbrains.annotations.NotNull()
        private com.hank.clawlive.data.model.AgentStatus currentStatus;
        @org.jetbrains.annotations.NotNull()
        private java.util.List<com.hank.clawlive.data.model.EntityStatus> currentEntities;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.Runnable drawRunnable = null;
        
        public ClawEngine() {
            super();
        }
        
        @java.lang.Override()
        public void onCreate(@org.jetbrains.annotations.Nullable()
        android.view.SurfaceHolder surfaceHolder) {
        }
        
        private final void observeStatus() {
        }
        
        @java.lang.Override()
        public void onVisibilityChanged(boolean visible) {
        }
        
        @java.lang.Override()
        public void onTouchEvent(@org.jetbrains.annotations.Nullable()
        android.view.MotionEvent event) {
        }
        
        @java.lang.Override()
        public void onSurfaceDestroyed(@org.jetbrains.annotations.Nullable()
        android.view.SurfaceHolder holder) {
        }
        
        private final void draw() {
        }
    }
}
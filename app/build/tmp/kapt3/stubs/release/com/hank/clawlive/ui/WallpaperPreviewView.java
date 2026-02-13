package com.hank.clawlive.ui;

/**
 * Custom View for wallpaper preview with drag-and-drop entity positioning
 * and pinch-to-resize support.
 *
 * Gestures:
 * - 1 finger drag: Move entity position
 * - 2 finger pinch: Resize entity
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000x\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010%\n\u0002\u0018\u0002\n\u0002\u0010\u0007\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0011\n\u0002\u0018\u0002\n\u0002\b\u0005\u0018\u00002\u00020\u0001B%\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ(\u0010&\u001a\u00020\u00072\u0006\u0010\'\u001a\u00020\u00072\u0006\u0010(\u001a\u00020\u00072\u0006\u0010)\u001a\u00020\u00072\u0006\u0010*\u001a\u00020\u0007H\u0002J \u0010+\u001a\u00020\f2\u0006\u0010,\u001a\u00020\f2\u0006\u0010)\u001a\u00020\u00072\u0006\u0010*\u001a\u00020\u0007H\u0002J\u0010\u0010-\u001a\u00020.2\u0006\u0010/\u001a\u000200H\u0002J0\u00101\u001a\u00020.2\u0006\u0010/\u001a\u0002002\u0006\u00102\u001a\u00020\u00122\u0006\u00103\u001a\u00020\u00162\u0006\u00104\u001a\u00020\u00162\u0006\u00105\u001a\u00020\u0016H\u0002J\u0010\u00106\u001a\u00020.2\u0006\u0010/\u001a\u000200H\u0002J\u0018\u00107\u001a\u00020\u00072\u0006\u00108\u001a\u00020\u00162\u0006\u00109\u001a\u00020\u0016H\u0002J\n\u0010:\u001a\u0004\u0018\u00010\fH\u0002J$\u0010;\u001a\u000e\u0012\u0004\u0012\u00020\u0016\u0012\u0004\u0012\u00020\u00160\u00152\u0006\u0010<\u001a\u00020\u00072\u0006\u0010=\u001a\u00020\u0007H\u0002J\b\u0010>\u001a\u00020.H\u0014J\u0010\u0010?\u001a\u00020.2\u0006\u0010/\u001a\u000200H\u0014J\u0010\u0010@\u001a\u00020\u001b2\u0006\u0010A\u001a\u00020BH\u0016J\u0006\u0010C\u001a\u00020.J\u0006\u0010D\u001a\u00020.J\u0014\u0010E\u001a\u00020.2\f\u0010F\u001a\b\u0012\u0004\u0012\u00020\u00120\u0011R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000b\u001a\u0004\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\r\u001a\u0004\u0018\u00010\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00120\u0011X\u0082\u000e\u00a2\u0006\u0002\n\u0000R&\u0010\u0013\u001a\u001a\u0012\u0004\u0012\u00020\u0007\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0016\u0012\u0004\u0012\u00020\u00160\u00150\u0014X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0017\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\u00160\u0014X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0018\u001a\u00020\u0016X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0019\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001a\u001a\u00020\u001bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001c\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001d\u001a\u00020\u0016X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001e\u001a\u00020\u0016X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001f\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010 \u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010!\u001a\u00020\"X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010#\u001a\u00020$X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010%\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006G"}, d2 = {"Lcom/hank/clawlive/ui/WallpaperPreviewView;", "Landroid/view/View;", "context", "Landroid/content/Context;", "attrs", "Landroid/util/AttributeSet;", "defStyleAttr", "", "(Landroid/content/Context;Landroid/util/AttributeSet;I)V", "backgroundPaint", "Landroid/graphics/Paint;", "cachedBackgroundBitmap", "Landroid/graphics/Bitmap;", "cachedBackgroundUri", "", "draggingEntityIndex", "entities", "", "Lcom/hank/clawlive/data/model/EntityStatus;", "entityPositions", "", "Lkotlin/Pair;", "", "entityScales", "hitRadiusFactor", "indicatorPaint", "isScaling", "", "labelPaint", "lastTouchX", "lastTouchY", "lastViewHeight", "lastViewWidth", "layoutPrefs", "Lcom/hank/clawlive/data/local/LayoutPreferences;", "scaleGestureDetector", "Landroid/view/ScaleGestureDetector;", "scalingEntityIndex", "calculateSampleSize", "imageWidth", "imageHeight", "targetWidth", "targetHeight", "centerCropScale", "source", "drawBackground", "", "canvas", "Landroid/graphics/Canvas;", "drawEntityPreview", "entity", "cx", "cy", "scale", "drawGridLines", "findEntityAtPosition", "touchX", "touchY", "getBackgroundBitmap", "getDefaultPosition", "index", "count", "onDetachedFromWindow", "onDraw", "onTouchEvent", "event", "Landroid/view/MotionEvent;", "refreshBackground", "resetPositions", "setEntities", "boundEntities", "app_release"})
public final class WallpaperPreviewView extends android.view.View {
    @org.jetbrains.annotations.NotNull()
    private final com.hank.clawlive.data.local.LayoutPreferences layoutPrefs = null;
    @org.jetbrains.annotations.NotNull()
    private java.util.List<com.hank.clawlive.data.model.EntityStatus> entities;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Map<java.lang.Integer, kotlin.Pair<java.lang.Float, java.lang.Float>> entityPositions = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Map<java.lang.Integer, java.lang.Float> entityScales = null;
    private int draggingEntityIndex = -1;
    private float lastTouchX = 0.0F;
    private float lastTouchY = 0.0F;
    private int scalingEntityIndex = -1;
    private boolean isScaling = false;
    private final float hitRadiusFactor = 0.12F;
    @org.jetbrains.annotations.Nullable()
    private android.graphics.Bitmap cachedBackgroundBitmap;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String cachedBackgroundUri;
    private int lastViewWidth = 0;
    private int lastViewHeight = 0;
    @org.jetbrains.annotations.NotNull()
    private final android.graphics.Paint backgroundPaint = null;
    @org.jetbrains.annotations.NotNull()
    private final android.graphics.Paint indicatorPaint = null;
    @org.jetbrains.annotations.NotNull()
    private final android.graphics.Paint labelPaint = null;
    @org.jetbrains.annotations.NotNull()
    private final android.view.ScaleGestureDetector scaleGestureDetector = null;
    
    @kotlin.jvm.JvmOverloads()
    public WallpaperPreviewView(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.Nullable()
    android.util.AttributeSet attrs, int defStyleAttr) {
        super(null);
    }
    
    /**
     * Set entities to display. Only bound entities should be passed.
     */
    public final void setEntities(@org.jetbrains.annotations.NotNull()
    java.util.List<com.hank.clawlive.data.model.EntityStatus> boundEntities) {
    }
    
    /**
     * Refresh background image from preferences
     */
    public final void refreshBackground() {
    }
    
    /**
     * Reset all positions and scales to default
     */
    public final void resetPositions() {
    }
    
    /**
     * Get default position for entity based on index and count
     */
    private final kotlin.Pair<java.lang.Float, java.lang.Float> getDefaultPosition(int index, int count) {
        return null;
    }
    
    @java.lang.Override()
    protected void onDraw(@org.jetbrains.annotations.NotNull()
    android.graphics.Canvas canvas) {
    }
    
    /**
     * Draw background image or solid color
     */
    private final void drawBackground(android.graphics.Canvas canvas) {
    }
    
    /**
     * Get cached background bitmap, loading if needed
     */
    private final android.graphics.Bitmap getBackgroundBitmap() {
        return null;
    }
    
    private final int calculateSampleSize(int imageWidth, int imageHeight, int targetWidth, int targetHeight) {
        return 0;
    }
    
    private final android.graphics.Bitmap centerCropScale(android.graphics.Bitmap source, int targetWidth, int targetHeight) {
        return null;
    }
    
    /**
     * Draw light grid lines for positioning reference
     */
    private final void drawGridLines(android.graphics.Canvas canvas) {
    }
    
    /**
     * Draw entity preview (simplified lobster shape)
     */
    private final void drawEntityPreview(android.graphics.Canvas canvas, com.hank.clawlive.data.model.EntityStatus entity, float cx, float cy, float scale) {
    }
    
    @java.lang.Override()
    public boolean onTouchEvent(@org.jetbrains.annotations.NotNull()
    android.view.MotionEvent event) {
        return false;
    }
    
    private final int findEntityAtPosition(float touchX, float touchY) {
        return 0;
    }
    
    @java.lang.Override()
    protected void onDetachedFromWindow() {
    }
    
    @kotlin.jvm.JvmOverloads()
    public WallpaperPreviewView(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super(null);
    }
    
    @kotlin.jvm.JvmOverloads()
    public WallpaperPreviewView(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.Nullable()
    android.util.AttributeSet attrs) {
        super(null);
    }
}
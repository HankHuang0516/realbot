package com.hank.clawlive.ui;

/**
 * Custom View for drag-and-drop entity positioning with pinch-to-resize.
 * Users can drag entities to custom positions and pinch to resize them.
 *
 * Gestures:
 * - 1 finger drag: Move entity position
 * - 2 finger pinch: Resize entity
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000j\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010%\n\u0002\u0018\u0002\n\u0002\u0010\u0007\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u000e\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u00002\u00020\u0001B%\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ0\u0010\u001f\u001a\u00020 2\u0006\u0010!\u001a\u00020\"2\u0006\u0010#\u001a\u00020\f2\u0006\u0010$\u001a\u00020\u00102\u0006\u0010%\u001a\u00020\u00102\u0006\u0010&\u001a\u00020\u0010H\u0002J\u0010\u0010\'\u001a\u00020 2\u0006\u0010!\u001a\u00020\"H\u0002J\u0018\u0010(\u001a\u00020\u00072\u0006\u0010)\u001a\u00020\u00102\u0006\u0010*\u001a\u00020\u0010H\u0002J$\u0010+\u001a\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00100\u000f2\u0006\u0010,\u001a\u00020\u00072\u0006\u0010-\u001a\u00020\u0007H\u0002J\u0010\u0010.\u001a\u00020 2\u0006\u0010!\u001a\u00020\"H\u0014J\u0010\u0010/\u001a\u00020\u00162\u0006\u00100\u001a\u000201H\u0016J\u0006\u00102\u001a\u00020 J\u0014\u00103\u001a\u00020 2\f\u00104\u001a\b\u0012\u0004\u0012\u00020\f0\u000bR\u000e\u0010\t\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\f0\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R&\u0010\r\u001a\u001a\u0012\u0004\u0012\u00020\u0007\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00100\u000f0\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0011\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\u00100\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0010X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0014X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0016X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0017\u001a\u00020\u0014X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0018\u001a\u00020\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0019\u001a\u00020\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001a\u001a\u00020\u001bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001c\u001a\u00020\u001dX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001e\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u00065"}, d2 = {"Lcom/hank/clawlive/ui/LayoutEditorView;", "Landroid/view/View;", "context", "Landroid/content/Context;", "attrs", "Landroid/util/AttributeSet;", "defStyleAttr", "", "(Landroid/content/Context;Landroid/util/AttributeSet;I)V", "draggingEntityIndex", "entities", "", "Lcom/hank/clawlive/data/model/EntityStatus;", "entityPositions", "", "Lkotlin/Pair;", "", "entityScales", "hitRadiusFactor", "indicatorPaint", "Landroid/graphics/Paint;", "isScaling", "", "labelPaint", "lastTouchX", "lastTouchY", "layoutPrefs", "Lcom/hank/clawlive/data/local/LayoutPreferences;", "scaleGestureDetector", "Landroid/view/ScaleGestureDetector;", "scalingEntityIndex", "drawEntityPreview", "", "canvas", "Landroid/graphics/Canvas;", "entity", "cx", "cy", "scale", "drawGridLines", "findEntityAtPosition", "touchX", "touchY", "getDefaultPosition", "index", "count", "onDraw", "onTouchEvent", "event", "Landroid/view/MotionEvent;", "resetPositions", "setEntities", "boundEntities", "app_release"})
public final class LayoutEditorView extends android.view.View {
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
    @org.jetbrains.annotations.NotNull()
    private final android.graphics.Paint indicatorPaint = null;
    @org.jetbrains.annotations.NotNull()
    private final android.graphics.Paint labelPaint = null;
    @org.jetbrains.annotations.NotNull()
    private final android.view.ScaleGestureDetector scaleGestureDetector = null;
    
    @kotlin.jvm.JvmOverloads()
    public LayoutEditorView(@org.jetbrains.annotations.NotNull()
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
     * Draw light grid lines for positioning reference
     */
    private final void drawGridLines(android.graphics.Canvas canvas) {
    }
    
    /**
     * Draw entity preview (simplified render)
     */
    private final void drawEntityPreview(android.graphics.Canvas canvas, com.hank.clawlive.data.model.EntityStatus entity, float cx, float cy, float scale) {
    }
    
    @java.lang.Override()
    public boolean onTouchEvent(@org.jetbrains.annotations.NotNull()
    android.view.MotionEvent event) {
        return false;
    }
    
    /**
     * Find entity at touch position using hit test
     */
    private final int findEntityAtPosition(float touchX, float touchY) {
        return 0;
    }
    
    @kotlin.jvm.JvmOverloads()
    public LayoutEditorView(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super(null);
    }
    
    @kotlin.jvm.JvmOverloads()
    public LayoutEditorView(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.Nullable()
    android.util.AttributeSet attrs) {
        super(null);
    }
}
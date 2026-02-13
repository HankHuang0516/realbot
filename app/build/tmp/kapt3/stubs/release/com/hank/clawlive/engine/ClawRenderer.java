package com.hank.clawlive.engine;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0080\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\u0010\u0007\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u001c\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J2\u0010\u001c\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u001f\u0012\u0004\u0012\u00020\u001f0\u001e0\u001d2\u0006\u0010 \u001a\u00020\u001f2\u0006\u0010!\u001a\u00020\u001f2\u0006\u0010\"\u001a\u00020\u0014H\u0002J:\u0010#\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u001f\u0012\u0004\u0012\u00020\u001f0\u001e0\u001d2\u0006\u0010 \u001a\u00020\u001f2\u0006\u0010!\u001a\u00020\u001f2\u0006\u0010\"\u001a\u00020\u00142\u0006\u0010$\u001a\u00020\u001fH\u0002JD\u0010%\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u001f\u0012\u0004\u0012\u00020\u001f0\u001e0\u001d2\u0006\u0010 \u001a\u00020\u001f2\u0006\u0010!\u001a\u00020\u001f2\u0006\u0010\"\u001a\u00020\u00142\u0010\b\u0002\u0010&\u001a\n\u0012\u0004\u0012\u00020\'\u0018\u00010\u001dH\u0002J:\u0010(\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u001f\u0012\u0004\u0012\u00020\u001f0\u001e0\u001d2\u0006\u0010 \u001a\u00020\u001f2\u0006\u0010!\u001a\u00020\u001f2\u0006\u0010\"\u001a\u00020\u00142\u0006\u0010$\u001a\u00020\u001fH\u0002J:\u0010)\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u001f\u0012\u0004\u0012\u00020\u001f0\u001e0\u001d2\u0006\u0010 \u001a\u00020\u001f2\u0006\u0010!\u001a\u00020\u001f2\u0006\u0010\"\u001a\u00020\u00142\u0006\u0010$\u001a\u00020\u001fH\u0002J(\u0010*\u001a\u00020\u00142\u0006\u0010+\u001a\u00020\u00142\u0006\u0010,\u001a\u00020\u00142\u0006\u0010-\u001a\u00020\u00142\u0006\u0010.\u001a\u00020\u0014H\u0002J2\u0010/\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u001f\u0012\u0004\u0012\u00020\u001f0\u001e0\u001d2\u0006\u0010 \u001a\u00020\u001f2\u0006\u0010!\u001a\u00020\u001f2\u0006\u0010\"\u001a\u00020\u0014H\u0002J \u00100\u001a\u00020\r2\u0006\u00101\u001a\u00020\r2\u0006\u0010-\u001a\u00020\u00142\u0006\u0010.\u001a\u00020\u0014H\u0002J\u0016\u00102\u001a\u0002032\u0006\u00104\u001a\u0002052\u0006\u00106\u001a\u000207J0\u00108\u001a\u0002032\u0006\u00104\u001a\u0002052\u0006\u00109\u001a\u00020\u00142\u0006\u0010:\u001a\u00020\u001f2\u0006\u0010;\u001a\u00020\u001f2\u0006\u0010<\u001a\u00020\u001fH\u0002J0\u0010=\u001a\u0002032\u0006\u00104\u001a\u0002052\u0006\u0010>\u001a\u00020\u001f2\u0006\u0010?\u001a\u00020\u001f2\u0006\u0010@\u001a\u00020\'2\u0006\u0010<\u001a\u00020\u001fH\u0002J\u0018\u0010A\u001a\u0002032\u0006\u00104\u001a\u0002052\u0006\u0010@\u001a\u00020\'H\u0002J8\u0010B\u001a\u0002032\u0006\u00104\u001a\u0002052\u0006\u0010@\u001a\u00020\'2\u0006\u0010C\u001a\u00020\u001f2\u0006\u0010D\u001a\u00020\u001f2\u0006\u0010<\u001a\u00020\u001f2\u0006\u0010E\u001a\u00020\u001fH\u0002J\u001c\u0010F\u001a\u0002032\u0006\u00104\u001a\u0002052\f\u0010&\u001a\b\u0012\u0004\u0012\u00020\'0\u001dJ8\u0010G\u001a\u0002032\u0006\u00104\u001a\u0002052\u0006\u0010@\u001a\u00020\'2\u0006\u0010C\u001a\u00020\u001f2\u0006\u0010H\u001a\u00020\u001f2\u0006\u0010<\u001a\u00020\u001f2\u0006\u0010E\u001a\u00020\u001fH\u0002JP\u0010I\u001a\u0002032\u0006\u00104\u001a\u0002052\u0006\u0010>\u001a\u00020\u001f2\u0006\u0010?\u001a\u00020\u001f2\u0006\u0010J\u001a\u00020\u001f2\u0006\u0010K\u001a\u00020\u001f2\u0006\u0010L\u001a\u00020\u001f2\u0006\u0010M\u001a\u00020\u001f2\u0006\u0010N\u001a\u00020\u00062\u0006\u0010O\u001a\u00020\u0006H\u0002J\u001a\u0010P\u001a\u0004\u0018\u00010\r2\u0006\u0010 \u001a\u00020\u00142\u0006\u0010!\u001a\u00020\u0014H\u0002J\u0010\u0010Q\u001a\u00020\u001f2\u0006\u0010\"\u001a\u00020\u0014H\u0002J\u0010\u0010R\u001a\u00020\u000f2\u0006\u0010S\u001a\u00020TH\u0002J\u0006\u0010U\u001a\u000203J\u000e\u0010V\u001a\u0002032\u0006\u0010W\u001a\u00020\u0012R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\f\u001a\u0004\u0018\u00010\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000e\u001a\u0004\u0018\u00010\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\u0017X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0018\u001a\u00020\u0019X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001a\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006X"}, d2 = {"Lcom/hank/clawlive/engine/ClawRenderer;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "backgroundPaint", "Landroid/graphics/Paint;", "badgePaint", "badgeTextPaint", "Landroid/text/TextPaint;", "bubblePaint", "bubbleStrokePaint", "cachedBackgroundBitmap", "Landroid/graphics/Bitmap;", "cachedBackgroundUri", "", "characterPaint", "isAmbient", "", "lastCanvasHeight", "", "lastCanvasWidth", "layoutPrefs", "Lcom/hank/clawlive/data/local/LayoutPreferences;", "startTime", "", "stateTextPaint", "textPaint", "calculateCorners", "", "Lkotlin/Pair;", "", "width", "height", "count", "calculateDiamond", "verticalPos", "calculateEntityPositions", "entities", "Lcom/hank/clawlive/data/model/EntityStatus;", "calculateGrid2x2", "calculateHorizontal", "calculateSampleSize", "imageWidth", "imageHeight", "targetWidth", "targetHeight", "calculateVertical", "centerCropScale", "source", "draw", "", "canvas", "Landroid/graphics/Canvas;", "status", "Lcom/hank/clawlive/data/model/AgentStatus;", "drawEntityBadge", "entityId", "x", "y", "scale", "drawLobsterAtPosition", "cx", "cy", "entity", "drawLobsterEyesForEntity", "drawMessageBubble", "centerX", "anchorBottomY", "screenWidth", "drawMultiEntity", "drawSingleEntityAt", "centerY", "drawSingleEye", "radius", "pupilRadius", "lidFactor", "browAngle", "pupilPaint", "lidPaint", "getBackgroundBitmap", "getScaleFactor", "getStateEmoji", "state", "Lcom/hank/clawlive/data/model/CharacterState;", "release", "setAmbient", "ambient", "app_release"})
public final class ClawRenderer {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final com.hank.clawlive.data.local.LayoutPreferences layoutPrefs = null;
    @org.jetbrains.annotations.Nullable()
    private android.graphics.Bitmap cachedBackgroundBitmap;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String cachedBackgroundUri;
    private int lastCanvasWidth = 0;
    private int lastCanvasHeight = 0;
    @org.jetbrains.annotations.NotNull()
    private final android.graphics.Paint backgroundPaint = null;
    @org.jetbrains.annotations.NotNull()
    private final android.text.TextPaint textPaint = null;
    @org.jetbrains.annotations.NotNull()
    private final android.graphics.Paint characterPaint = null;
    @org.jetbrains.annotations.NotNull()
    private final android.graphics.Paint bubblePaint = null;
    @org.jetbrains.annotations.NotNull()
    private final android.graphics.Paint bubbleStrokePaint = null;
    @org.jetbrains.annotations.NotNull()
    private final android.graphics.Paint badgePaint = null;
    @org.jetbrains.annotations.NotNull()
    private final android.text.TextPaint badgeTextPaint = null;
    @org.jetbrains.annotations.NotNull()
    private final android.text.TextPaint stateTextPaint = null;
    private long startTime;
    private boolean isAmbient = false;
    
    public ClawRenderer(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    public final void setAmbient(boolean ambient) {
    }
    
    /**
     * Get background bitmap, loading and caching as needed.
     * Uses center-crop scaling to fill the canvas.
     */
    private final android.graphics.Bitmap getBackgroundBitmap(int width, int height) {
        return null;
    }
    
    /**
     * Calculate sample size for efficient memory usage.
     */
    private final int calculateSampleSize(int imageWidth, int imageHeight, int targetWidth, int targetHeight) {
        return 0;
    }
    
    /**
     * Center-crop scale bitmap to fill target dimensions.
     */
    private final android.graphics.Bitmap centerCropScale(android.graphics.Bitmap source, int targetWidth, int targetHeight) {
        return null;
    }
    
    /**
     * Release cached resources. Call when service is destroyed.
     */
    public final void release() {
    }
    
    /**
     * Calculate positions for entities based on count and layout preference.
     * If useCustomLayout is enabled, reads per-entity custom positions from SharedPreferences.
     */
    private final java.util.List<kotlin.Pair<java.lang.Float, java.lang.Float>> calculateEntityPositions(float width, float height, int count, java.util.List<com.hank.clawlive.data.model.EntityStatus> entities) {
        return null;
    }
    
    private final java.util.List<kotlin.Pair<java.lang.Float, java.lang.Float>> calculateGrid2x2(float width, float height, int count, float verticalPos) {
        return null;
    }
    
    private final java.util.List<kotlin.Pair<java.lang.Float, java.lang.Float>> calculateHorizontal(float width, float height, int count, float verticalPos) {
        return null;
    }
    
    private final java.util.List<kotlin.Pair<java.lang.Float, java.lang.Float>> calculateVertical(float width, float height, int count) {
        return null;
    }
    
    private final java.util.List<kotlin.Pair<java.lang.Float, java.lang.Float>> calculateDiamond(float width, float height, int count, float verticalPos) {
        return null;
    }
    
    private final java.util.List<kotlin.Pair<java.lang.Float, java.lang.Float>> calculateCorners(float width, float height, int count) {
        return null;
    }
    
    /**
     * Get scale factor based on entity count.
     * Smaller scale for more entities.
     * Base scale is 1.5x larger than original.
     */
    private final float getScaleFactor(int count) {
        return 0.0F;
    }
    
    /**
     * Draw multiple entities on the canvas.
     */
    public final void drawMultiEntity(@org.jetbrains.annotations.NotNull()
    android.graphics.Canvas canvas, @org.jetbrains.annotations.NotNull()
    java.util.List<com.hank.clawlive.data.model.EntityStatus> entities) {
    }
    
    /**
     * Draw a single entity at the specified position with scale.
     */
    private final void drawSingleEntityAt(android.graphics.Canvas canvas, com.hank.clawlive.data.model.EntityStatus entity, float centerX, float centerY, float scale, float screenWidth) {
    }
    
    /**
     * Draw entity ID badge (small circle with number).
     */
    private final void drawEntityBadge(android.graphics.Canvas canvas, int entityId, float x, float y, float scale) {
    }
    
    /**
     * Draw message bubble with semi-transparent background.
     * Positioned ABOVE the anchor point (bottom of bubble -> anchor).
     */
    private final void drawMessageBubble(android.graphics.Canvas canvas, com.hank.clawlive.data.model.EntityStatus entity, float centerX, float anchorBottomY, float scale, float screenWidth) {
    }
    
    /**
     * Get emoji for state.
     */
    private final java.lang.String getStateEmoji(com.hank.clawlive.data.model.CharacterState state) {
        return null;
    }
    
    /**
     * Draw lobster at specific position with scale.
     */
    private final void drawLobsterAtPosition(android.graphics.Canvas canvas, float cx, float cy, com.hank.clawlive.data.model.EntityStatus entity, float scale) {
    }
    
    /**
     * Draw lobster eyes (called within scaled canvas context).
     */
    private final void drawLobsterEyesForEntity(android.graphics.Canvas canvas, com.hank.clawlive.data.model.EntityStatus entity) {
    }
    
    private final void drawSingleEye(android.graphics.Canvas canvas, float cx, float cy, float radius, float pupilRadius, float lidFactor, float browAngle, android.graphics.Paint pupilPaint, android.graphics.Paint lidPaint) {
    }
    
    /**
     * Draw single entity (backward compatible).
     */
    public final void draw(@org.jetbrains.annotations.NotNull()
    android.graphics.Canvas canvas, @org.jetbrains.annotations.NotNull()
    com.hank.clawlive.data.model.AgentStatus status) {
    }
}
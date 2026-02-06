package com.hank.clawlive.engine

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.text.TextPaint
import com.hank.clawlive.data.local.EntityLayout
import com.hank.clawlive.data.local.LayoutPreferences
import com.hank.clawlive.data.model.AgentStatus
import com.hank.clawlive.data.model.CharacterState
import com.hank.clawlive.data.model.EntityStatus
import kotlin.math.sin

class ClawRenderer(private val context: Context) {

    private val layoutPrefs = LayoutPreferences.getInstance(context)

    private val textPaint = TextPaint().apply {
        color = Color.WHITE
        textSize = 50f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val characterPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // Bubble paint for message background
    private val bubblePaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // Bubble paint for message border
    private val bubbleStrokePaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.WHITE
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    // Badge paint for entity ID
    private val badgePaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val badgeTextPaint = TextPaint().apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }

    // State icon paint
    private val stateTextPaint = TextPaint().apply {
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    // Animation state
    private var startTime = System.currentTimeMillis()

    // Ambient state
    private var isAmbient = false

    fun setAmbient(ambient: Boolean) {
        this.isAmbient = ambient
    }

    // ============================================
    // MULTI-ENTITY RENDERING
    // ============================================

    /**
     * Calculate positions for entities based on count and layout preference.
     */
    private fun calculateEntityPositions(
        width: Float,
        height: Float,
        count: Int
    ): List<Pair<Float, Float>> {
        val layout = layoutPrefs.entityLayout
        val verticalPos = layoutPrefs.verticalPosition

        return when (layout) {
            EntityLayout.GRID_2X2 -> calculateGrid2x2(width, height, count, verticalPos)
            EntityLayout.HORIZONTAL -> calculateHorizontal(width, height, count, verticalPos)
            EntityLayout.VERTICAL -> calculateVertical(width, height, count)
            EntityLayout.DIAMOND -> calculateDiamond(width, height, count, verticalPos)
            EntityLayout.CORNERS -> calculateCorners(width, height, count)
        }
    }

    private fun calculateGrid2x2(width: Float, height: Float, count: Int, verticalPos: Float): List<Pair<Float, Float>> {
        val centerY = height * verticalPos
        return when (count) {
            1 -> listOf(Pair(width / 2f, centerY))
            2 -> listOf(
                Pair(width * 0.3f, centerY),
                Pair(width * 0.7f, centerY)
            )
            3 -> listOf(
                Pair(width / 2f, centerY - height * 0.15f),
                Pair(width * 0.3f, centerY + height * 0.15f),
                Pair(width * 0.7f, centerY + height * 0.15f)
            )
            else -> listOf(
                Pair(width * 0.3f, centerY - height * 0.15f),
                Pair(width * 0.7f, centerY - height * 0.15f),
                Pair(width * 0.3f, centerY + height * 0.15f),
                Pair(width * 0.7f, centerY + height * 0.15f)
            )
        }
    }

    private fun calculateHorizontal(width: Float, height: Float, count: Int, verticalPos: Float): List<Pair<Float, Float>> {
        val centerY = height * verticalPos
        val spacing = width / (count + 1)
        return (1..count).map { i ->
            Pair(spacing * i, centerY)
        }
    }

    private fun calculateVertical(width: Float, height: Float, count: Int): List<Pair<Float, Float>> {
        val centerX = width / 2f
        val spacing = height / (count + 1)
        return (1..count).map { i ->
            Pair(centerX, spacing * i)
        }
    }

    private fun calculateDiamond(width: Float, height: Float, count: Int, verticalPos: Float): List<Pair<Float, Float>> {
        val centerX = width / 2f
        val centerY = height * verticalPos
        val offsetX = width * 0.25f
        val offsetY = height * 0.15f

        return when (count) {
            1 -> listOf(Pair(centerX, centerY))
            2 -> listOf(
                Pair(centerX - offsetX, centerY),
                Pair(centerX + offsetX, centerY)
            )
            3 -> listOf(
                Pair(centerX, centerY - offsetY),
                Pair(centerX - offsetX, centerY + offsetY),
                Pair(centerX + offsetX, centerY + offsetY)
            )
            else -> listOf(
                Pair(centerX, centerY - offsetY),        // Top
                Pair(centerX - offsetX, centerY),       // Left
                Pair(centerX + offsetX, centerY),       // Right
                Pair(centerX, centerY + offsetY)        // Bottom
            )
        }
    }

    private fun calculateCorners(width: Float, height: Float, count: Int): List<Pair<Float, Float>> {
        val marginX = width * 0.2f
        val marginY = height * 0.25f

        return when (count) {
            1 -> listOf(Pair(width / 2f, height / 2f))
            2 -> listOf(
                Pair(marginX, marginY),
                Pair(width - marginX, height - marginY)
            )
            3 -> listOf(
                Pair(marginX, marginY),
                Pair(width - marginX, marginY),
                Pair(width / 2f, height - marginY)
            )
            else -> listOf(
                Pair(marginX, marginY),
                Pair(width - marginX, marginY),
                Pair(marginX, height - marginY),
                Pair(width - marginX, height - marginY)
            )
        }
    }

    /**
     * Get scale factor based on entity count.
     * Smaller scale for more entities.
     * Base scale is 1.5x larger than original.
     */
    private fun getScaleFactor(count: Int): Float = when (count) {
        1 -> 1.5f      // 1.0 * 1.5
        2 -> 0.975f    // 0.65 * 1.5
        3 -> 0.825f    // 0.55 * 1.5
        else -> 0.75f  // 0.5 * 1.5
    }

    /**
     * Draw multiple entities on the canvas.
     */
    fun drawMultiEntity(canvas: Canvas, entities: List<EntityStatus>) {
        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()

        // Background
        canvas.drawColor(Color.BLACK)

        if (entities.isEmpty()) {
            // Draw "No entities" message with instructions
            textPaint.textSize = 36f
            textPaint.color = Color.WHITE
            canvas.drawText("No entities connected", width / 2f, height / 2f - 40f, textPaint)
            textPaint.textSize = 24f
            textPaint.color = Color.GRAY
            canvas.drawText("Open Claw Live app to bind", width / 2f, height / 2f + 20f, textPaint)
            textPaint.textSize = 20f
            canvas.drawText("entities with OpenClaw bot", width / 2f, height / 2f + 60f, textPaint)
            return
        }

        val positions = calculateEntityPositions(width, height, entities.size)
        val scale = getScaleFactor(entities.size)

        entities.forEachIndexed { index, entity ->
            if (index < positions.size) {
                val (cx, cy) = positions[index]
                drawSingleEntityAt(canvas, entity, cx, cy, scale, width)
            }
        }
    }

    /**
     * Draw a single entity at the specified position with scale.
     */
    private fun drawSingleEntityAt(
        canvas: Canvas,
        entity: EntityStatus,
        centerX: Float,
        centerY: Float,
        scale: Float,
        screenWidth: Float
    ) {
        // Calculate Animation (Bobbing)
        val time = System.currentTimeMillis() - startTime
        val bobOffset = if (entity.state == CharacterState.SLEEPING) {
            0f
        } else {
            val speed = if (entity.state == CharacterState.BUSY) 0.01f else 0.003f
            (sin(time * speed) * 30 * scale).toFloat()
        }

        val charY = centerY + bobOffset
        val radius = 150f * scale

        // Draw entity based on type (LOBSTER only now)
        // The lobster SVG drawing creates its own body shape, no need for base circle
        drawLobsterAtPosition(canvas, centerX, charY, entity, scale)

        // Draw message bubble (ABOVE the entity)
        // Anchor point is top of the character + margin
        drawMessageBubble(canvas, entity, centerX, charY - radius - (20f * scale), scale, screenWidth)

        // Draw Name and Status Group BELOW the entity
        if (!isAmbient) {
            val stateEmoji = getStateEmoji(entity.state)
            val statusText = "$stateEmoji ${entity.state}"

            // Configure paints
            stateTextPaint.textSize = (28f * scale).coerceAtLeast(18f)
            stateTextPaint.color = Color.WHITE

            // Base Y position (below entity)
            val baseY = charY + radius + (40f * scale)
            val lineHeight = stateTextPaint.textSize * 1.3f

            // Draw name if exists (at baseY)
            entity.name?.let { name ->
                stateTextPaint.textAlign = Paint.Align.CENTER
                canvas.drawText(name, centerX, baseY, stateTextPaint)
            }

            // Status bar Y: shifted down if name exists
            val statusY = if (entity.name != null) baseY + lineHeight else baseY

            // Measure dimensions for status bar
            val textWidth = stateTextPaint.measureText(statusText)
            val badgeRadius = 12f * scale // Smaller badge for this location
            val badgeDiameter = badgeRadius * 2
            val spacing = 16f * scale

            // Calculate total width of the group (Badge + Spacing + Text)
            val totalWidth = badgeDiameter + spacing + textWidth

            // Calculate starting X to center the whole group
            val groupStartX = centerX - (totalWidth / 2)
            val badgeCenterY = statusY - (stateTextPaint.textSize / 3) // Align roughly with text middle

            // Draw Badge
            drawEntityBadge(canvas, entity.entityId, groupStartX + badgeRadius, badgeCenterY, scale * 0.5f) // Pass smaller scale because function uses it for radius too

            // Draw Text (Left aligned from after the badge)
            stateTextPaint.textAlign = Paint.Align.LEFT
            canvas.drawText(statusText, groupStartX + badgeDiameter + spacing, statusY, stateTextPaint)
            stateTextPaint.textAlign = Paint.Align.CENTER // Restore default
        }

        // Draw "Zzz" if sleeping (Overlay on body/head, maybe slightly adjusted)
        if (entity.state == CharacterState.SLEEPING && !isAmbient) {
            textPaint.textSize = 60f * scale
            // Moving Zzz slightly up so it doesn't overlap too much with the center face features
            canvas.drawText("Zzz...", centerX + radius * 0.7f, charY - radius * 0.8f, textPaint)
        }
    }

    /**
     * Draw entity ID badge (small circle with number).
     */
    private fun drawEntityBadge(
        canvas: Canvas,
        entityId: Int,
        x: Float,
        y: Float,
        scale: Float
    ) {
        if (isAmbient) return

        val badgeRadius = 24f * scale

        // Badge background color based on entity ID
        badgePaint.color = when (entityId) {
            0 -> Color.parseColor("#4CAF50") // Green
            1 -> Color.parseColor("#2196F3") // Blue
            2 -> Color.parseColor("#FF9800") // Orange
            3 -> Color.parseColor("#9C27B0") // Purple
            else -> Color.GRAY
        }

        // Draw badge circle
        canvas.drawCircle(x, y, badgeRadius, badgePaint)

        // Draw entity number
        badgeTextPaint.textSize = 28f * scale
        canvas.drawText("#$entityId", x, y + (10f * scale), badgeTextPaint)
    }

    /**
     * Draw message bubble with semi-transparent background.
     */
    /**
     * Draw message bubble with semi-transparent background.
     * Positioned ABOVE the anchor point (bottom of bubble -> anchor).
     */
    /**
     * Draw message bubble with semi-transparent background.
     * Positioned ABOVE the anchor point (bottom of bubble -> anchor).
     */
    /**
     * Draw message bubble with semi-transparent background.
     * Positioned ABOVE the anchor point (bottom of bubble -> anchor).
     */
    private fun drawMessageBubble(
        canvas: Canvas,
        entity: EntityStatus,
        centerX: Float,
        anchorBottomY: Float,
        scale: Float,
        screenWidth: Float
    ) {
        val message = entity.message ?: ""
        if (message.isEmpty() && isAmbient) return

        // 1. Setup Text Paint
        textPaint.textSize = (32f * scale).coerceAtLeast(16f)
        textPaint.color = Color.WHITE
        // FORCE LEFT ALIGNMENT for bubble text to prevent overflow
        val originalAlign = textPaint.textAlign
        textPaint.textAlign = Paint.Align.LEFT

        val displayText = if (isAmbient) {
            getStateEmoji(entity.state)
        } else {
            message
        }

        // 2. Define Layout Constraints
        val padH = 16f * scale
        val padV = 16f * scale
        val screenHeight = canvas.height.toFloat()
        val screenMargin = 40f * scale // Safety margin from top/bottom
        
        // Calculate max available height for text
        // (Screen Height - Margins - Padding)
        val maxAvailableHeight = screenHeight - (screenMargin * 2) - (padV * 2)
        
        val maxWidth = (screenWidth * 0.8f).coerceIn(200f, 800f)
        val maxTextWidth = (maxWidth - padH * 2).toInt().coerceAtLeast(1)
        
        // Calculate Max Lines to fit in screen
        val lineHeight = textPaint.fontSpacing
        val maxLines = (maxAvailableHeight / lineHeight).toInt().coerceAtLeast(1)

        // 3. Create StaticLayout
        val layoutBuilder = android.text.StaticLayout.Builder.obtain(
            displayText, 0, displayText.length, textPaint, maxTextWidth
        )
            .setAlignment(android.text.Layout.Alignment.ALIGN_NORMAL) 
            .setLineSpacing(4f, 1.0f)
            .setIncludePad(true)
            .setMaxLines(maxLines)
            .setEllipsize(android.text.TextUtils.TruncateAt.END)
        
        val layout = layoutBuilder.build()

        // 4. Calculate Dimensions
        var widestLineWidth = 0f
        for (i in 0 until layout.lineCount) {
             val lineWidth = layout.getLineWidth(i)
             if (lineWidth > widestLineWidth) {
                 widestLineWidth = lineWidth
             }
        }
        
        val contentWidth = widestLineWidth
        // Ensure minimum width for the tail connection
        val minContentWidth = 40f * scale 
        val finalContentWidth = contentWidth.coerceAtLeast(minContentWidth)
        
        val bubbleWidth = finalContentWidth + padH * 2
        val bubbleHeight = layout.height.toFloat() + padV * 2

        // 5. Calculate Positioning (Smart Constraints)
        val idealTop = anchorBottomY - bubbleHeight
        
        // If idealTop is above the screen margin, shift it down to the margin.
        // This might cause it to overlap the character, but text legibility is priority.
        val bubbleTop = if (idealTop < screenMargin) {
            screenMargin
        } else {
            idealTop
        }
        
        val bubbleBottom = bubbleTop + bubbleHeight
        val bubbleLeft = centerX - bubbleWidth / 2
        val bubbleRight = centerX + bubbleWidth / 2
        
        // Check if shifted significantly (tail would be disconnected/weird)
        // If the bubble bottom is significantly below the anchor point, we should hide the tail?
        // Actually, if bubbleTop != idealTop, it means we shifted.
        val isShifted = bubbleTop > idealTop + 1f // allowance for float error

        // 6. Draw Bubble Shape (Unified Path)
        val cornerRadius = 24f * scale
        
        // Tail geometry
        val tailWidth = 20f * scale
        val tailHeight = 20f * scale
        val tailTipX = centerX
        val tailTipY = bubbleBottom + tailHeight
        val tailBaseLeft = centerX - (tailWidth / 2) + (5f * scale)
        val tailBaseRight = centerX + (tailWidth / 2)
        
        val path = android.graphics.Path()
        
        if (!isAmbient && !isShifted) {
             // WITH TAIL
            path.moveTo(tailBaseRight, bubbleBottom)
            path.lineTo(bubbleRight - cornerRadius, bubbleBottom)
            path.arcTo(RectF(bubbleRight - cornerRadius * 2, bubbleBottom - cornerRadius * 2, bubbleRight, bubbleBottom), 90f, -90f)
            path.lineTo(bubbleRight, bubbleTop + cornerRadius)
            path.arcTo(RectF(bubbleRight - cornerRadius * 2, bubbleTop, bubbleRight, bubbleTop + cornerRadius * 2), 0f, -90f)
            path.lineTo(bubbleLeft + cornerRadius, bubbleTop)
            path.arcTo(RectF(bubbleLeft, bubbleTop, bubbleLeft + cornerRadius * 2, bubbleTop + cornerRadius * 2), 270f, -90f)
            path.lineTo(bubbleLeft, bubbleBottom - cornerRadius)
            path.arcTo(RectF(bubbleLeft, bubbleBottom - cornerRadius * 2, bubbleLeft + cornerRadius * 2, bubbleBottom), 180f, -90f)
            path.lineTo(tailBaseLeft, bubbleBottom)
            path.quadTo(centerX - (tailWidth * 0.2f), bubbleBottom + (tailHeight * 0.6f), tailTipX, tailTipY)
            path.quadTo(centerX + (tailWidth * 0.4f), bubbleBottom + (tailHeight * 0.3f), tailBaseRight, bubbleBottom)
        } else {
            // NO TAIL (Rounded Rect only) - used when shifted or ambient
            path.addRoundRect(RectF(bubbleLeft, bubbleTop, bubbleRight, bubbleBottom), cornerRadius, cornerRadius, android.graphics.Path.Direction.CW)
        }
        
        path.close()

        // Set Paint Colors
        val bubblesColor = when (entity.state) {
            CharacterState.SLEEPING -> Color.argb(230, 50, 50, 80)
            CharacterState.EXCITED -> Color.argb(230, 255, 100, 50)
            CharacterState.BUSY -> Color.argb(230, 50, 100, 150)
            CharacterState.EATING -> Color.argb(230, 80, 150, 50)
            else -> Color.argb(230, 40, 40, 40)
        }
        bubblePaint.color = bubblesColor
        
        bubbleStrokePaint.strokeWidth = 4f * scale
        bubbleStrokePaint.color = Color.WHITE

        // Draw Fill
        canvas.drawPath(path, bubblePaint)
        
        // Draw Border
        canvas.drawPath(path, bubbleStrokePaint)

        // 7. Draw Text
        canvas.save()
        canvas.translate(bubbleLeft + padH, bubbleTop + padV)
        layout.draw(canvas)
        canvas.restore()
        
        // Restore original alignment (CENTER)
        textPaint.textAlign = originalAlign
    }

    /**
     * Get emoji for state.
     */
    private fun getStateEmoji(state: CharacterState): String = when (state) {
        CharacterState.IDLE -> "😐"
        CharacterState.SLEEPING -> "😴"
        CharacterState.EXCITED -> "🎉"
        CharacterState.BUSY -> "💼"
        CharacterState.EATING -> "🍽️"
    }

    /**
     * Draw lobster at specific position with scale.
     */
    private fun drawLobsterAtPosition(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        entity: EntityStatus,
        scale: Float
    ) {
        // SVG scale (original is 4x, now multiply by entity scale)
        val svgScale = 4f * scale

        canvas.save()
        canvas.translate(cx - (60 * svgScale), cy - (60 * svgScale))
        canvas.scale(svgScale, svgScale)

        // Colors
        // Dynamic color
        val charString = entity.character.toUpperCase(java.util.Locale.ROOT)
        // Check for overrides in parts
        val customColor = entity.parts?.get("COLOR")?.toInt()
        val metallic = entity.parts?.get("METALLIC") ?: 0f
        val gloss = entity.parts?.get("GLOSS") ?: 0f

        val (coralBright, coralDark) = if (customColor != null) {
            // Ensure alpha channel is set (in case only RGB was provided without alpha)
            // If alpha is 0 (transparent), force it to 0xFF (fully opaque)
            val base = if (android.graphics.Color.alpha(customColor) == 0) {
                customColor or 0xFF000000.toInt()
            } else {
                customColor
            }
            // If metallic, dark color is darker (higher contrast)
            // If gloss, bright color is lighter
            val darkFactor = if (metallic > 0.5f) 0.4f else 0.8f // 0.4 = 60% darker
            val r = android.graphics.Color.red(base)
            val g = android.graphics.Color.green(base)
            val b = android.graphics.Color.blue(base)
            
            val dark = android.graphics.Color.rgb(
                (r * darkFactor).toInt(),
                (g * darkFactor).toInt(),
                (b * darkFactor).toInt()
            )
            Pair(base, dark)
        } else {
            // Fallback to name-based logic
            val isGolden = charString.contains("GOLDEN")
            val isDiamond = charString.contains("DIAMOND")
            
            val bright = when {
                isGolden -> Color.parseColor("#FFD700")
                isDiamond -> Color.CYAN
                else -> Color.parseColor("#FF7F50")
            }
            
            val dark = when {
                isGolden -> Color.parseColor("#DAA520")
                isDiamond -> Color.parseColor("#008B8B")
                else -> Color.parseColor("#CD5B45")
            }
            Pair(bright, dark)
        }

        val bodyPaint = Paint().apply {
            style = Paint.Style.FILL
            color = coralBright
            isAntiAlias = true
            // Note: LinearGradient with canvas transforms needs setLocalMatrix
            // For now, using solid color for reliability
        }

        val strokePaint = Paint().apply {
            style = Paint.Style.STROKE
            color = coralBright
            strokeWidth = 2f
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }

        // Body
        val bodyPath = android.graphics.Path().apply {
            moveTo(60f, 10f)
            cubicTo(30f, 10f, 15f, 35f, 15f, 55f)
            cubicTo(15f, 75f, 30f, 95f, 45f, 100f)
            lineTo(45f, 110f)
            lineTo(55f, 110f)
            lineTo(55f, 100f)
            cubicTo(55f, 100f, 60f, 102f, 65f, 100f)
            lineTo(65f, 110f)
            lineTo(75f, 110f)
            lineTo(75f, 100f)
            cubicTo(90f, 95f, 105f, 75f, 105f, 55f)
            cubicTo(105f, 35f, 90f, 10f, 60f, 10f)
            close()
        }
        canvas.drawPath(bodyPath, bodyPaint)

        // Left Claw
        canvas.save()
        val leftRotation = entity.parts?.get("CLAW_LEFT") ?: 0f
        if (leftRotation != 0f) {
            canvas.rotate(leftRotation, 20f, 55f)
        }
        val leftClawPath = android.graphics.Path().apply {
            moveTo(20f, 45f)
            cubicTo(5f, 40f, 0f, 50f, 5f, 60f)
            cubicTo(10f, 70f, 20f, 65f, 25f, 55f)
            cubicTo(28f, 48f, 25f, 45f, 20f, 45f)
            close()
        }
        canvas.drawPath(leftClawPath, bodyPaint)
        canvas.restore()

        // Right Claw
        canvas.save()
        val rightRotation = entity.parts?.get("CLAW_RIGHT") ?: 0f
        if (rightRotation != 0f) {
            canvas.rotate(rightRotation, 100f, 55f) // Adjusted pivot
        }
        val rightClawPath = android.graphics.Path().apply {
            moveTo(100f, 45f)
            cubicTo(115f, 40f, 120f, 50f, 115f, 60f)
            cubicTo(110f, 70f, 100f, 65f, 95f, 55f)
            cubicTo(92f, 48f, 95f, 45f, 100f, 45f)
            close()
        }
        canvas.drawPath(rightClawPath, bodyPaint)
        canvas.restore()

        // Antenna
        val antennaL = android.graphics.Path().apply {
            moveTo(45f, 15f)
            quadTo(35f, 5f, 30f, 8f)
        }
        canvas.drawPath(antennaL, strokePaint)

        val antennaR = android.graphics.Path().apply {
            moveTo(75f, 15f)
            quadTo(85f, 5f, 90f, 8f)
        }
        canvas.drawPath(antennaR, strokePaint)

        // Eyes
        val eyePaint = Paint().apply {
            color = Color.parseColor("#1a1a2e") // var(--bg-deep)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val eyeGlowPaint = Paint().apply {
            color = Color.CYAN // var(--cyan-bright)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        // Left Eye
        canvas.drawCircle(45f, 35f, 6f, eyePaint)
        canvas.drawCircle(46f, 34f, 2f, eyeGlowPaint)

        // Right Eye
        canvas.drawCircle(75f, 35f, 6f, eyePaint)
        canvas.drawCircle(76f, 34f, 2f, eyeGlowPaint)

        canvas.restore()
    }

    /**
     * Draw lobster eyes (called within scaled canvas context).
     */
    private fun drawLobsterEyesForEntity(canvas: Canvas, entity: EntityStatus) {
        val eyeRadius = 6f
        val pupilRadius = 2f
        val leftEyeX = 45f
        val rightEyeX = 75f
        val eyeY = 35f

        val pupilPaint = Paint().apply { style = Paint.Style.FILL; color = Color.BLACK; isAntiAlias = true }
        val lidPaint = Paint().apply { style = Paint.Style.FILL; color = Color.parseColor("#FF7F50"); isAntiAlias = true }

        val defaultLid = if (entity.state == CharacterState.SLEEPING) 1.0f else 0f
        val lidFactor = entity.parts?.get("EYE_LID") ?: defaultLid
        val browAngle = entity.parts?.get("EYE_ANGLE") ?: 0f

        drawSingleEye(canvas, leftEyeX, eyeY, eyeRadius, pupilRadius, lidFactor, browAngle, pupilPaint, lidPaint)
        drawSingleEye(canvas, rightEyeX, eyeY, eyeRadius, pupilRadius, lidFactor, -browAngle, pupilPaint, lidPaint)
    }

    private fun drawSingleEye(
        canvas: Canvas, cx: Float, cy: Float, radius: Float, pupilRadius: Float,
        lidFactor: Float, browAngle: Float,
        pupilPaint: Paint, lidPaint: Paint
    ) {
        canvas.save()

        val eyePath = android.graphics.Path()
        eyePath.addCircle(cx, cy, radius, android.graphics.Path.Direction.CW)
        canvas.clipPath(eyePath)

        canvas.drawColor(Color.WHITE)
        canvas.drawCircle(cx, cy, pupilRadius * 1.5f, pupilPaint)

        canvas.restore()

        if (lidFactor > 0.05f || browAngle != 0f) {
            canvas.save()
            canvas.rotate(browAngle, cx, cy)
            canvas.clipPath(eyePath)

            val lidTop = cy - radius - 5f
            val coverage = 2 * radius * lidFactor + 2f
            val lidBottom = (cy - radius) + coverage

            canvas.drawRect(cx - radius - 5f, lidTop, cx + radius + 5f, lidBottom, lidPaint)
            canvas.restore()
        }
    }

    // ============================================
    // BACKWARD COMPATIBLE SINGLE ENTITY
    // ============================================

    /**
     * Draw single entity (backward compatible).
     */
    fun draw(canvas: Canvas, status: AgentStatus) {
        // Convert to EntityStatus and use multi-entity renderer
        val entityStatus = EntityStatus.fromAgentStatus(status, 0)
        drawMultiEntity(canvas, listOf(entityStatus))
    }
}

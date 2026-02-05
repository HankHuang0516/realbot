package com.hank.clawlive.engine

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import com.hank.clawlive.data.model.AgentStatus
import com.hank.clawlive.data.model.CharacterState
import com.hank.clawlive.data.model.CharacterType
import com.hank.clawlive.data.model.EntityStatus
import kotlin.math.sin

class ClawRenderer(private val context: Context) {

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
     * Calculate positions for entities based on count.
     * Layout:
     * - 1: center
     * - 2: left-center, right-center
     * - 3: top-center, bottom-left, bottom-right
     * - 4: 2x2 grid
     */
    private fun calculateEntityPositions(
        width: Float,
        height: Float,
        count: Int
    ): List<Pair<Float, Float>> {
        return when (count) {
            1 -> listOf(
                Pair(width / 2f, height / 2f)
            )
            2 -> listOf(
                Pair(width * 0.3f, height / 2f),
                Pair(width * 0.7f, height / 2f)
            )
            3 -> listOf(
                Pair(width / 2f, height * 0.35f),
                Pair(width * 0.3f, height * 0.7f),
                Pair(width * 0.7f, height * 0.7f)
            )
            else -> listOf( // 4 or more
                Pair(width * 0.3f, height * 0.35f),
                Pair(width * 0.7f, height * 0.35f),
                Pair(width * 0.3f, height * 0.7f),
                Pair(width * 0.7f, height * 0.7f)
            )
        }
    }

    /**
     * Get scale factor based on entity count.
     * Smaller scale for more entities.
     */
    private fun getScaleFactor(count: Int): Float = when (count) {
        1 -> 1.0f
        2 -> 0.65f
        3 -> 0.55f
        else -> 0.5f
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
            // Draw "No entities" message
            textPaint.textSize = 40f
            canvas.drawText("No entities active", width / 2f, height / 2f, textPaint)
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

        // Dynamic color
        characterPaint.color = when (entity.character) {
            CharacterType.LOBSTER -> if (entity.state == CharacterState.SLEEPING)
                Color.parseColor("#8B0000") else Color.RED
            CharacterType.PIG -> if (entity.state == CharacterState.SLEEPING)
                Color.parseColor("#C71585") else Color.MAGENTA
        }

        // Base Body Shape
        canvas.drawCircle(centerX, charY, radius, characterPaint)

        // Form-specific details
        when (entity.character) {
            CharacterType.LOBSTER -> {
                drawLobsterAtPosition(canvas, centerX, charY, entity, scale)
            }
            CharacterType.PIG -> {
                drawPigAtPosition(canvas, centerX, charY, entity, scale)
            }
        }

        // Draw Status Text (smaller for multi-entity)
        drawEntityMessage(canvas, entity, centerX, charY + radius + (40f * scale), scale, screenWidth)

        // Draw "Zzz" if sleeping
        if (entity.state == CharacterState.SLEEPING && !isAmbient) {
            textPaint.textSize = 60f * scale
            canvas.drawText("Zzz...", centerX + radius * 0.7f, charY - radius * 0.5f, textPaint)
        }
    }

    /**
     * Draw entity message text below the character.
     */
    private fun drawEntityMessage(
        canvas: Canvas,
        entity: EntityStatus,
        centerX: Float,
        textY: Float,
        scale: Float,
        screenWidth: Float
    ) {
        val message = if (isAmbient) {
            "${entity.state}"
        } else {
            "${entity.message}\n(${entity.state})"
        }

        textPaint.color = if (isAmbient) Color.GRAY else Color.WHITE
        textPaint.textSize = (40f * scale).coerceAtLeast(24f)

        // Text width based on scale
        val textWidth = (screenWidth * 0.35f * scale).toInt().coerceAtLeast(150)

        val layout = android.text.StaticLayout.Builder.obtain(
            message, 0, message.length, textPaint, textWidth
        ).setAlignment(android.text.Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1.0f)
            .setIncludePad(false)
            .build()

        canvas.save()
        canvas.translate(centerX - (textWidth / 2f), textY)
        layout.draw(canvas)
        canvas.restore()
    }

    /**
     * Draw pig at specific position with scale.
     */
    private fun drawPigAtPosition(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        entity: EntityStatus,
        scale: Float
    ) {
        // Snout
        val snoutPaint = Paint(characterPaint)
        snoutPaint.color = Color.parseColor("#FFC0CB")
        canvas.drawOval(
            cx - 50f * scale, cy + 20f * scale,
            cx + 50f * scale, cy + 80f * scale,
            snoutPaint
        )

        // Nostrils
        snoutPaint.color = Color.BLACK
        canvas.drawCircle(cx - 20f * scale, cy + 50f * scale, 10f * scale, snoutPaint)
        canvas.drawCircle(cx + 20f * scale, cy + 50f * scale, 10f * scale, snoutPaint)

        // Ears
        characterPaint.color = Color.MAGENTA
        canvas.drawCircle(cx - 100f * scale, cy - 100f * scale, 50f * scale, characterPaint)
        canvas.drawCircle(cx + 100f * scale, cy - 100f * scale, 50f * scale, characterPaint)

        // Eyes
        if (!isAmbient) {
            val eyeRadius = 20f * scale
            val eyeOffsetX = 50f * scale
            val eyeOffsetY = -30f * scale
            characterPaint.color = Color.WHITE
            canvas.drawCircle(cx - eyeOffsetX, cy + eyeOffsetY, eyeRadius, characterPaint)
            canvas.drawCircle(cx + eyeOffsetX, cy + eyeOffsetY, eyeRadius, characterPaint)
            characterPaint.color = Color.BLACK
            val pupilRadius = if (entity.state == CharacterState.EXCITED) 12f * scale else 8f * scale
            canvas.drawCircle(cx - eyeOffsetX, cy + eyeOffsetY, pupilRadius, characterPaint)
            canvas.drawCircle(cx + eyeOffsetX, cy + eyeOffsetY, pupilRadius, characterPaint)
        }
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
        val coralBright = Color.parseColor("#FF7F50")
        val coralDark = Color.parseColor("#CD5B45")

        val bodyPaint = Paint().apply {
            style = Paint.Style.FILL
            color = coralBright
            isAntiAlias = true
            shader = android.graphics.LinearGradient(
                0f, 0f, 120f, 120f,
                coralBright, coralDark,
                android.graphics.Shader.TileMode.CLAMP
            )
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

        // Left Claw with rotation
        canvas.save()
        val leftRotation = entity.parts?.get("CLAW_LEFT") ?: 0f
        if (leftRotation != 0f) {
            canvas.rotate(leftRotation, 25f, 55f)
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

        // Right Claw with rotation
        canvas.save()
        val rightRotation = entity.parts?.get("CLAW_RIGHT") ?: 0f
        if (rightRotation != 0f) {
            canvas.rotate(rightRotation, 95f, 55f)
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
        drawLobsterEyesForEntity(canvas, entity)

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

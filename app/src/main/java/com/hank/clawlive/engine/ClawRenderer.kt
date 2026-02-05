package com.hank.clawlive.engine

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.hank.clawlive.data.model.AgentStatus
import com.hank.clawlive.data.model.CharacterState
import com.hank.clawlive.data.model.CharacterType
import kotlin.math.sin

class ClawRenderer(private val context: Context) {

    private val textPaint = Paint().apply {
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

    fun draw(canvas: Canvas, status: AgentStatus) {
        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()
        val centerX = width / 2f
        val centerY = height / 2f

        // 1. Draw Background
        // Use simpler/darker background in ambient mode
        if (isAmbient) {
            canvas.drawColor(Color.BLACK) 
        } else {
            // slightly lighter black for active mode or maybe just black is fine.
            canvas.drawColor(Color.BLACK)
        }

        // 2. Calculate Animation (Bobbing)
        val time = System.currentTimeMillis() - startTime
        val bobOffset = if (status.state == CharacterState.SLEEPING) {
            0f
        } else {
            val speed = if (status.state == CharacterState.BUSY) 0.01f else 0.003f
            (sin(time * speed) * 30).toFloat()
        }

        // 3. Draw Character (Distinct Forms)
        val charY = centerY + bobOffset
        val radius = 150f
        
        // Dynamic color
        characterPaint.color = when (status.character) {
            CharacterType.LOBSTER -> if (status.state == CharacterState.SLEEPING) Color.parseColor("#8B0000") else Color.RED
            CharacterType.PIG -> if (status.state == CharacterState.SLEEPING) Color.parseColor("#C71585") else Color.MAGENTA
        }

        // Base Body Shape
        canvas.drawCircle(centerX, charY, radius, characterPaint)
        
        // Form-specific details
        when (status.character) {
            CharacterType.LOBSTER -> {
                drawLobsterSVG(canvas, centerX, charY)
            }
            CharacterType.PIG -> {
                // ... (Keep existing Pig logic for contrast) ...
                // Draw Snout (Darker Pink Oval)
                // ...
                val snoutPaint = Paint(characterPaint)
                snoutPaint.color = Color.parseColor("#FFC0CB") // Light pink snout
                canvas.drawOval(
                    centerX - 50f, charY + 20f,
                    centerX + 50f, charY + 80f,
                    snoutPaint
                )
                // Nostrils
                snoutPaint.color = Color.BLACK
                canvas.drawCircle(centerX - 20f, charY + 50f, 10f, snoutPaint)
                canvas.drawCircle(centerX + 20f, charY + 50f, 10f, snoutPaint)
                
                // Ears
                characterPaint.color = Color.MAGENTA
                canvas.drawCircle(centerX - 100f, charY - 100f, 50f, characterPaint)
                canvas.drawCircle(centerX + 100f, charY - 100f, 50f, characterPaint)
            }
        }
        
        // 3.1 Draw Eyes (Only for Pig now, Lobster has embedded eyes in SVG)
        if (!isAmbient && status.character == CharacterType.PIG) { 
            // ... (Keep existing Eye logic for Pig) ...
             val eyeRadius = 20f
             val eyeOffsetX = 50f
             val eyeOffsetY = -30f
             characterPaint.color = Color.WHITE
             canvas.drawCircle(centerX - eyeOffsetX, charY + eyeOffsetY, eyeRadius, characterPaint)
             canvas.drawCircle(centerX + eyeOffsetX, charY + eyeOffsetY, eyeRadius, characterPaint)
             characterPaint.color = Color.BLACK
             val pupilRadius = 8f
             // Dilate pupils if EXCITED
             val currentPupilRadius = if (status.state == CharacterState.EXCITED) 12f else pupilRadius
             canvas.drawCircle(centerX - eyeOffsetX, charY + eyeOffsetY, currentPupilRadius, characterPaint)
             canvas.drawCircle(centerX + eyeOffsetX, charY + eyeOffsetY, currentPupilRadius, characterPaint)
        }

        // 4. Draw Status Text
        val message = if (isAmbient) "${status.state}" else "${status.message}\n(${status.state})"
        val lines = message.split("\n")
        var textY = charY + radius + 80f
        
        textPaint.color = if (isAmbient) Color.GRAY else Color.WHITE
        
        for (line in lines) {
            canvas.drawText(line, centerX, textY, textPaint)
            textPaint.textSize.let { textY += it + 10f }
        }

        // 5. Draw "Zzz" if sleeping (not in ambient)
        if (status.state == CharacterState.SLEEPING && !isAmbient) {
            textPaint.textSize = 80f
            canvas.drawText("Zzz...", centerX + radius, charY - radius, textPaint)
            textPaint.textSize = 50f // Reset
        }
    }

    private fun drawLobsterSVG(canvas: Canvas, cx: Float, cy: Float) {
        // Source SVG viewBox is 0 0 120 120.
        // We want to scale it up to match our ~300px radius size.
        // Let's scale by 4x -> 480px width.
        val scale = 4f
        
        canvas.save()
        // Center the 120x120 SVG at (cx, cy)
        // 120 * scale / 2 = 60 * 4 = 240 offset
        canvas.translate(cx - (60 * scale), cy - (60 * scale))
        canvas.scale(scale, scale)

        // Colors
        val coralBright = Color.parseColor("#FF7F50")
        val coralDark = Color.parseColor("#CD5B45")
        val bgDeep = Color.parseColor("#1A1A2E") // Dark blueish
        val cyanBright = Color.parseColor("#00FFFF")

        // Gradient for Body (Simple solid fallback or simulated gradient)
        // For simplicity in Canvas, we'll use solid coralBright first or a Shader if needed.
        val bodyPaint = Paint().apply {
            style = Paint.Style.FILL
            color = coralBright
            isAntiAlias = true
            // If we want the gradient:
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

        val eyePaint = Paint().apply { style = Paint.Style.FILL; color = bgDeep; isAntiAlias = true }
        val eyeGlowPaint = Paint().apply { style = Paint.Style.FILL; color = cyanBright; isAntiAlias = true }

        // 1. Body
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

        // 2. Left Claw
        val leftClawPath = android.graphics.Path().apply {
            moveTo(20f, 45f)
            cubicTo(5f, 40f, 0f, 50f, 5f, 60f)
            cubicTo(10f, 70f, 20f, 65f, 25f, 55f)
            cubicTo(28f, 48f, 25f, 45f, 20f, 45f)
            close()
        }
        canvas.drawPath(leftClawPath, bodyPaint)

        // 3. Right Claw
        val rightClawPath = android.graphics.Path().apply {
            moveTo(100f, 45f)
            cubicTo(115f, 40f, 120f, 50f, 115f, 60f)
            cubicTo(110f, 70f, 100f, 65f, 95f, 55f)
            cubicTo(92f, 48f, 95f, 45f, 100f, 45f)
            close()
        }
        canvas.drawPath(rightClawPath, bodyPaint)

        // 4. Antenna (Stroke)
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

        // 5. Eyes
        canvas.drawCircle(45f, 35f, 6f, eyePaint)
        canvas.drawCircle(75f, 35f, 6f, eyePaint)

        // 6. Eye Glow
        canvas.drawCircle(46f, 34f, 2f, eyeGlowPaint)
        canvas.drawCircle(76f, 34f, 2f, eyeGlowPaint)

        canvas.restore()
    }
}

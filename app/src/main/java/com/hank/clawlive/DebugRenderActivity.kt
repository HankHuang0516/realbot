package com.hank.clawlive

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.hank.clawlive.data.model.CharacterState
import com.hank.clawlive.data.model.EntityStatus
import com.hank.clawlive.engine.ClawRenderer

/**
 * Debug Activity to verify lobster rendering locally.
 * This renders the lobster to a static bitmap for inspection.
 */
class DebugRenderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scrollView = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        // Title
        layout.addView(TextView(this).apply {
            text = "Debug Lobster Rendering"
            textSize = 24f
            setTextColor(Color.WHITE)
        })

        layout.addView(TextView(this).apply {
            text = "Below are test renders of the lobster at different scales:"
            textSize = 14f
            setTextColor(Color.GRAY)
            setPadding(0, 16, 0, 32)
        })

        // Test 1: Full renderer test (uses ClawRenderer)
        addTestSection(layout, "Full Renderer (ClawRenderer)", 800, 600) { canvas ->
            val renderer = ClawRenderer(this)
            val entity = EntityStatus(
                entityId = 0,
                character = "LOBSTER",
                state = CharacterState.IDLE,
                message = "Test message for debugging"
            )
            renderer.drawMultiEntity(canvas, listOf(entity))
        }

        // Test 2: Direct path drawing (isolated test)
        addTestSection(layout, "Direct Body Path Only", 400, 400) { canvas ->
            canvas.drawColor(Color.BLACK)
            drawDirectLobsterBody(canvas, 200f, 200f, 2f)
        }

        // Test 3: Simple shapes to verify canvas
        addTestSection(layout, "Simple Shapes (Canvas Test)", 400, 200) { canvas ->
            canvas.drawColor(Color.BLACK)

            val paint = Paint().apply {
                color = Color.parseColor("#FF7F50") // Coral
                style = Paint.Style.FILL
                isAntiAlias = true
            }

            // Draw simple circle
            canvas.drawCircle(100f, 100f, 50f, paint)

            // Draw simple rect
            canvas.drawRect(200f, 50f, 300f, 150f, paint)

            // Draw with path
            val path = android.graphics.Path().apply {
                moveTo(350f, 50f)
                lineTo(400f, 150f)
                lineTo(300f, 150f)
                close()
            }
            canvas.drawPath(path, paint)
        }

        // Test 4: Body path coordinates visualization
        addTestSection(layout, "Body Path Coordinates (at origin)", 500, 500) { canvas ->
            canvas.drawColor(Color.DKGRAY)

            // Draw coordinate grid
            val gridPaint = Paint().apply {
                color = Color.parseColor("#333333")
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }
            for (i in 0..500 step 20) {
                canvas.drawLine(i.toFloat(), 0f, i.toFloat(), 500f, gridPaint)
                canvas.drawLine(0f, i.toFloat(), 500f, i.toFloat(), gridPaint)
            }

            // Draw origin marker
            val originPaint = Paint().apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }
            canvas.drawCircle(0f, 0f, 5f, originPaint)

            // Draw lobster body path at 1:1 scale with offset
            canvas.save()
            canvas.translate(150f, 100f) // Offset to see the full shape

            val bodyPaint = Paint().apply {
                color = Color.parseColor("#FF7F50")
                style = Paint.Style.FILL
                isAntiAlias = true
            }

            // Original SVG path coordinates (from drawLobsterAtPosition)
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

            // Draw claws
            val leftClawPath = android.graphics.Path().apply {
                moveTo(20f, 45f)
                cubicTo(5f, 40f, 0f, 50f, 5f, 60f)
                cubicTo(10f, 70f, 20f, 65f, 25f, 55f)
                cubicTo(28f, 48f, 25f, 45f, 20f, 45f)
                close()
            }
            canvas.drawPath(leftClawPath, bodyPaint)

            val rightClawPath = android.graphics.Path().apply {
                moveTo(100f, 45f)
                cubicTo(115f, 40f, 120f, 50f, 115f, 60f)
                cubicTo(110f, 70f, 100f, 65f, 95f, 55f)
                cubicTo(92f, 48f, 95f, 45f, 100f, 45f)
                close()
            }
            canvas.drawPath(rightClawPath, bodyPaint)

            // Draw eyes
            val eyePaint = Paint().apply {
                color = Color.parseColor("#1a1a2e")
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            val eyeGlowPaint = Paint().apply {
                color = Color.CYAN
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawCircle(45f, 35f, 6f, eyePaint)
            canvas.drawCircle(46f, 34f, 2f, eyeGlowPaint)
            canvas.drawCircle(75f, 35f, 6f, eyePaint)
            canvas.drawCircle(76f, 34f, 2f, eyeGlowPaint)

            canvas.restore()

            // Add label
            val textPaint = Paint().apply {
                color = Color.WHITE
                textSize = 16f
            }
            canvas.drawText("Body spans: x=15-105, y=10-110", 150f, 480f, textPaint)
        }

        // Test 5: Multiple entities
        addTestSection(layout, "Multiple Entities (4)", 800, 800) { canvas ->
            val renderer = ClawRenderer(this)
            val entities = listOf(
                EntityStatus(entityId = 0, name = "測試龍蝦", character = "LOBSTER", state = CharacterState.IDLE, message = "Entity 0"),
                EntityStatus(entityId = 1, character = "LOBSTER", state = CharacterState.BUSY, message = "Entity 1"),
                EntityStatus(entityId = 2, name = "阿財", character = "LOBSTER", state = CharacterState.EATING, message = "Entity 2"),
                EntityStatus(entityId = 3, character = "LOBSTER", state = CharacterState.EXCITED, message = "Entity 3")
            )
            renderer.drawMultiEntity(canvas, entities)
        }

        scrollView.addView(layout)
        scrollView.setBackgroundColor(Color.BLACK)
        setContentView(scrollView)
    }

    private fun addTestSection(
        layout: LinearLayout,
        title: String,
        width: Int,
        height: Int,
        drawFunc: (Canvas) -> Unit
    ) {
        // Section title
        layout.addView(TextView(this).apply {
            text = title
            textSize = 18f
            setTextColor(Color.WHITE)
            setPadding(0, 32, 0, 8)
        })

        // Create bitmap and draw
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        try {
            drawFunc(canvas)
        } catch (e: Exception) {
            // Draw error message on canvas
            canvas.drawColor(Color.RED)
            val paint = Paint().apply {
                color = Color.WHITE
                textSize = 24f
            }
            canvas.drawText("Error: ${e.message}", 20f, height / 2f, paint)
        }

        // Display bitmap
        layout.addView(ImageView(this).apply {
            setImageBitmap(bitmap)
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true
        })

        // Dimensions info
        layout.addView(TextView(this).apply {
            text = "Size: ${width}x${height}"
            textSize = 12f
            setTextColor(Color.GRAY)
        })
    }

    /**
     * Draw lobster body directly (for isolated testing)
     */
    private fun drawDirectLobsterBody(canvas: Canvas, cx: Float, cy: Float, scale: Float) {
        val svgScale = 4f * scale

        canvas.save()
        canvas.translate(cx - (60 * svgScale), cy - (60 * svgScale))
        canvas.scale(svgScale, svgScale)

        val coralBright = Color.parseColor("#FF7F50")

        val bodyPaint = Paint().apply {
            style = Paint.Style.FILL
            color = coralBright
            isAntiAlias = true
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
        val leftClawPath = android.graphics.Path().apply {
            moveTo(20f, 45f)
            cubicTo(5f, 40f, 0f, 50f, 5f, 60f)
            cubicTo(10f, 70f, 20f, 65f, 25f, 55f)
            cubicTo(28f, 48f, 25f, 45f, 20f, 45f)
            close()
        }
        canvas.drawPath(leftClawPath, bodyPaint)

        // Right Claw
        val rightClawPath = android.graphics.Path().apply {
            moveTo(100f, 45f)
            cubicTo(115f, 40f, 120f, 50f, 115f, 60f)
            cubicTo(110f, 70f, 100f, 65f, 95f, 55f)
            cubicTo(92f, 48f, 95f, 45f, 100f, 45f)
            close()
        }
        canvas.drawPath(rightClawPath, bodyPaint)

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
            color = Color.parseColor("#1a1a2e")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val eyeGlowPaint = Paint().apply {
            color = Color.CYAN
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        canvas.drawCircle(45f, 35f, 6f, eyePaint)
        canvas.drawCircle(46f, 34f, 2f, eyeGlowPaint)
        canvas.drawCircle(75f, 35f, 6f, eyePaint)
        canvas.drawCircle(76f, 34f, 2f, eyeGlowPaint)

        canvas.restore()
    }
}

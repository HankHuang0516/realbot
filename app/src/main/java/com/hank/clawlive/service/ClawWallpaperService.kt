package com.hank.clawlive.service

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import timber.log.Timber
import com.hank.clawlive.engine.ClawRenderer
import com.hank.clawlive.data.model.AgentStatus
import com.hank.clawlive.data.model.CharacterState
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*

class ClawWallpaperService : WallpaperService() {

    override fun onCreate() {
        super.onCreate()
        if (Timber.treeCount == 0) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.d("ClawWallpaperService Created")
    }

    override fun onCreateEngine(): Engine {
        Timber.d("Creating ClawEngine")
        return ClawEngine()
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("ClawWallpaperService Destroyed")
    }

    // Inner class implementation
    inner class ClawEngine : WallpaperService.Engine() {

        private val handler = Handler(Looper.getMainLooper())
        private var visible = false
        private val renderer = ClawRenderer(this@ClawWallpaperService)
        private val repository = com.hank.clawlive.data.repository.StateRepository(
            com.hank.clawlive.data.remote.NetworkModule.api,
            this@ClawWallpaperService
        )
        
        // Coroutine Scope for this engine instance
        private val engineScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main + kotlinx.coroutines.SupervisorJob())

        private var currentStatus = AgentStatus(
            state = CharacterState.IDLE,
            message = "Connecting..."
        )

        private val drawRunnable = Runnable { draw() }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            Timber.d("ClawEngine onCreate")
            setTouchEventsEnabled(true)
            
            // Start observing data
            observeStatus()
        }
        


        private fun observeStatus() {
            engineScope.launch {
                repository.getStatusFlow(intervalMs = 5000) // Fast 5s updates for testing
                    .collect { newStatus ->
                        Timber.d("New Status Received: $newStatus")
                        currentStatus = newStatus
                        if (visible) {
                            draw()
                        }
                    }
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            Timber.d("onVisibilityChanged: $visible")
            if (visible) {
                draw()
            } else {
                handler.removeCallbacks(drawRunnable)
            }
        }
        
        override fun onTouchEvent(event: android.view.MotionEvent?) {
             if (event?.action == android.view.MotionEvent.ACTION_UP) {
                 // Trigger Wake Up logic
                 currentStatus = currentStatus.copy(message = "Waking up...", state = CharacterState.EXCITED)
                 if (visible) draw()
                 
                 engineScope.launch {
                     try {
                         repository.wakeUp()
                         // After wake up, force a refresh or show success
                         currentStatus = currentStatus.copy(message = "I'm Awake! (Webhook Sent)")
                         if (visible) draw()
                         // Revert to normal after 2s
                         kotlinx.coroutines.delay(2000)
                         // The flow will likely override this anyway, but good for feedback
                     } catch (e: Exception) {
                         Timber.e(e, "Wake up failed")
                     }
                 }
             }
             super.onTouchEvent(event)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder)
            visible = false
            handler.removeCallbacks(drawRunnable)
            engineScope.cancel() // Clean up coroutines
            Timber.d("onSurfaceDestroyed")
        }

        private fun draw() {
            val holder = surfaceHolder
            var canvas: Canvas? = null

            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    renderer.draw(canvas, currentStatus)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error during drawing")
            } finally {
                if (canvas != null) {
                    try {
                        holder.unlockCanvasAndPost(canvas)
                    } catch (e: Exception) {
                        Timber.e(e, "Error unlocking canvas")
                    }
                }
            }

            // Schedule next frame
            handler.removeCallbacks(drawRunnable)
            if (visible) {
                handler.postDelayed(drawRunnable, 33)
            }
        }
    }
}

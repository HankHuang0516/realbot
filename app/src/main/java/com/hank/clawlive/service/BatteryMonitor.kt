package com.hank.clawlive.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Monitors device battery level changes and reports to backend.
 * Uses ACTION_BATTERY_CHANGED broadcast to get real-time battery updates.
 */
class BatteryMonitor(
    private val context: Context,
    private val onBatteryChanged: suspend (Int) -> Unit
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var lastReportedLevel: Int = -1
    private var isRegistered = false

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
                
                if (level >= 0 && scale > 0) {
                    val percentage = (level * 100) / scale
                    
                    // Only report if battery level changed (avoid spamming API)
                    if (percentage != lastReportedLevel) {
                        lastReportedLevel = percentage
                        Timber.d("Battery level changed: $percentage%")
                        
                        scope.launch {
                            try {
                                onBatteryChanged(percentage)
                            } catch (e: Exception) {
                                Timber.e(e, "Failed to report battery level")
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Start monitoring battery changes.
     * Returns the current battery level immediately.
     */
    fun start(): Int {
        if (!isRegistered) {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            // Use sticky broadcast to get current battery status immediately
            val batteryStatus = context.registerReceiver(batteryReceiver, filter)
            isRegistered = true
            
            // Extract current level from sticky broadcast
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
            
            if (level >= 0 && scale > 0) {
                lastReportedLevel = (level * 100) / scale
                Timber.d("Battery monitoring started. Current level: $lastReportedLevel%")
                return lastReportedLevel
            }
        }
        return lastReportedLevel
    }

    /**
     * Stop monitoring battery changes.
     */
    fun stop() {
        if (isRegistered) {
            try {
                context.unregisterReceiver(batteryReceiver)
                isRegistered = false
                Timber.d("Battery monitoring stopped")
            } catch (e: Exception) {
                Timber.e(e, "Error unregistering battery receiver")
            }
        }
    }

    /**
     * Get current battery level without registering for updates.
     */
    fun getCurrentLevel(): Int {
        val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        
        return if (level >= 0 && scale > 0) {
            (level * 100) / scale
        } else {
            lastReportedLevel
        }
    }
}

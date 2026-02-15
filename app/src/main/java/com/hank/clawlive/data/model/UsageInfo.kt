package com.hank.clawlive.data.model

/**
 * AI Resource Usage Information
 * Used for displaying usage status in the app's status bar
 * 
 * JSON Structure from Backend:
 * {
 *   "percentage": 85,           // 0-100 remaining percentage
 *   "label": "85% 剩餘",       // Text to display
 *   "status": "NORMAL",        // NORMAL, WARNING, CRITICAL
 *   "resetTime": "2026-02-11T14:00:00Z",  // Optional: next reset time
 *   "description": "本小時剩餘 1.2k tokens"  // Optional: detailed description
 * }
 */
data class UsageInfo(
    val percentage: Int,
    val label: String,
    val status: UsageStatus = UsageStatus.NORMAL,
    val resetTime: String? = null,
    val description: String? = null
)

/**
 * Usage status levels for color coding
 */
enum class UsageStatus {
    NORMAL,    // Green - Normal usage
    WARNING,   // Yellow - Approaching limit
    CRITICAL   // Red - Near limit
}

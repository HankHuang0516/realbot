package com.hank.clawlive.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hank.clawlive.ChatActivity
import com.hank.clawlive.FeedbackHistoryActivity
import com.hank.clawlive.MainActivity
import com.hank.clawlive.R
import com.hank.clawlive.data.local.DeviceManager
import com.hank.clawlive.data.remote.NetworkModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class ClawFcmService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_CHAT = "eclaw_chat"
        const val CHANNEL_SYSTEM = "eclaw_system"
        const val CHANNEL_FEEDBACK = "eclaw_feedback"

        fun createChannels(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val nm = context.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(NotificationChannel(
                CHANNEL_CHAT, "Chat Messages", NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Messages from your entities" })
            nm.createNotificationChannel(NotificationChannel(
                CHANNEL_SYSTEM, "System", NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "System notifications" })
            nm.createNotificationChannel(NotificationChannel(
                CHANNEL_FEEDBACK, "Feedback Updates", NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Feedback status updates" })
        }
    }

    override fun onNewToken(token: String) {
        Timber.d("[FCM] New token: ${token.take(20)}...")
        val dm = DeviceManager.getInstance(this)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                NetworkModule.api.registerFcmToken(
                    mapOf(
                        "deviceId" to dm.deviceId,
                        "deviceSecret" to dm.deviceSecret,
                        "fcmToken" to token
                    )
                )
            } catch (e: Exception) {
                Timber.e(e, "[FCM] Failed to register token")
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val title = data["title"] ?: message.notification?.title ?: "E-Claw"
        val body = data["body"] ?: message.notification?.body ?: ""
        val category = data["category"] ?: "system"

        val channelId = when (category) {
            "bot_reply", "broadcast", "speak_to" -> CHANNEL_CHAT
            "feedback_reply", "feedback_resolved" -> CHANNEL_FEEDBACK
            else -> CHANNEL_SYSTEM
        }

        val targetIntent = when (category) {
            "bot_reply", "broadcast", "speak_to", "scheduled" ->
                Intent(this, ChatActivity::class.java)
            "feedback_reply", "feedback_resolved" ->
                Intent(this, FeedbackHistoryActivity::class.java)
            else ->
                Intent(this, MainActivity::class.java)
        }.apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP }

        val pi = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), targetIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val nm = getSystemService(NotificationManager::class.java)
        val notif = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        nm.notify(System.currentTimeMillis().toInt(), notif)
    }
}

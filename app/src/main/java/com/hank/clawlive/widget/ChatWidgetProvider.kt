package com.hank.clawlive.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.hank.clawlive.ChatActivity
import com.hank.clawlive.R
import com.hank.clawlive.data.local.ChatPreferences

/**
 * Simple 1x1 resizable chat widget
 * Clicking opens ChatActivity as a floating dialog
 */
class ChatWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val chatPrefs = ChatPreferences.getInstance(context)

        appWidgetIds.forEach { appWidgetId ->
            updateAppWidget(context, appWidgetManager, appWidgetId, chatPrefs)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        chatPrefs: ChatPreferences
    ) {
        // Create intent to launch ChatActivity as floating dialog
        val intent = Intent(context, ChatActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val views = RemoteViews(context.packageName, R.layout.widget_claw_chat)

        // Set dynamic text from last message
        val displayText = chatPrefs.getWidgetDisplayText()
        views.setTextViewText(R.id.widget_text, displayText)

        val textColor = if (chatPrefs.lastMessage.isNullOrEmpty()) {
            0xFFAAAAAA.toInt()
        } else {
            0xFFFFFFFF.toInt()
        }
        views.setTextColor(R.id.widget_text, textColor)

        // On Click -> Launch ChatActivity floating dialog
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
        views.setOnClickPendingIntent(R.id.widget_send_btn, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        fun updateWidgets(context: Context) {
            val intent = Intent(context, ChatWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }

            val widgetManager = AppWidgetManager.getInstance(context)
            val widgetIds = widgetManager.getAppWidgetIds(
                ComponentName(context, ChatWidgetProvider::class.java)
            )

            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
            context.sendBroadcast(intent)
        }
    }
}

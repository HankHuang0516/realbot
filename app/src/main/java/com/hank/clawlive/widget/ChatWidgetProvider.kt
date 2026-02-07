package com.hank.clawlive.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.hank.clawlive.MessageActivity
import com.hank.clawlive.R
import com.hank.clawlive.data.local.ChatPreferences

class ChatWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val chatPrefs = ChatPreferences.getInstance(context)

        // Perform this loop procedure for each App Widget that belongs to this provider
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
        // Create an Intent to launch the MessageActivity (Dialog)
        val intent = Intent(context, MessageActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Construct the RemoteViews object
        val views = RemoteViews(context.packageName, R.layout.widget_claw_chat)

        // Set dynamic text from last message
        val displayText = chatPrefs.getWidgetDisplayText()
        views.setTextViewText(R.id.widget_text, displayText)

        // Style text differently if it's a real message vs placeholder
        val textColor = if (chatPrefs.lastMessage.isNullOrEmpty()) {
            0xFFAAAAAA.toInt()  // Gray for placeholder
        } else {
            0xFFFFFFFF.toInt()  // White for actual message
        }
        views.setTextColor(R.id.widget_text, textColor)

        // On Click -> Launch Activity (both root and send button)
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
        views.setOnClickPendingIntent(R.id.widget_send_btn, pendingIntent)

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        /**
         * Static helper to update widgets from anywhere in the app
         */
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

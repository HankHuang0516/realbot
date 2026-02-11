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
import timber.log.Timber

/**
 * Chat Widget Provider with ListView showing recent chat history
 */
class ChatWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Perform this loop procedure for each App Widget that belongs to this provider
        appWidgetIds.forEach { appWidgetId ->
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        Timber.d("Updating widget $appWidgetId")

        // Construct the RemoteViews object
        val views = RemoteViews(context.packageName, R.layout.widget_claw_chat_history)

        // Set up the RemoteViewsService for the ListView
        val serviceIntent = Intent(context, ChatWidgetService::class.java)
        views.setRemoteAdapter(R.id.widget_message_list, serviceIntent)

        // Set up the intent that will open ChatActivity when widget is clicked
        val chatIntent = Intent(context, ChatActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val chatPendingIntent = PendingIntent.getActivity(
            context,
            0,
            chatIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set click listeners
        views.setOnClickPendingIntent(R.id.widget_input_section, chatPendingIntent)
        views.setOnClickPendingIntent(R.id.widget_send_btn, chatPendingIntent)
        views.setOnClickPendingIntent(R.id.widget_open_chat, chatPendingIntent)

        // Set the empty view for the ListView
        views.setEmptyView(R.id.widget_message_list, R.id.widget_empty_state)

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)

        // Notify the widget that data has changed
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_message_list)
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
        Timber.d("Widget enabled")
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
        Timber.d("Widget disabled")
    }

    companion object {
        /**
         * Static helper to update widgets from anywhere in the app
         */
        fun updateWidgets(context: Context) {
            Timber.d("Updating all widgets")
            val intent = Intent(context, ChatWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }

            val widgetManager = AppWidgetManager.getInstance(context)
            val widgetIds = widgetManager.getAppWidgetIds(
                ComponentName(context, ChatWidgetProvider::class.java)
            )

            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
            context.sendBroadcast(intent)

            // Also notify data set changed for ListView
            widgetIds.forEach { widgetId ->
                widgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.widget_message_list)
            }
        }
    }
}

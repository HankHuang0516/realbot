package com.hank.clawlive.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.hank.clawlive.R
import com.hank.clawlive.data.local.database.ChatMessage
import com.hank.clawlive.data.local.database.MessageType
import com.hank.clawlive.data.repository.ChatRepository
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * RemoteViewsService for Chat Widget ListView
 * Provides recent chat messages to widget
 */
class ChatWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return ChatWidgetViewFactory(this.applicationContext)
    }
}

class ChatWidgetViewFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {

    private val chatRepository: ChatRepository by lazy {
        ChatRepository.getInstance(context)
    }

    private var messages: List<ChatMessage> = emptyList()

    override fun onCreate() {
        // Initial load
        loadMessages()
    }

    override fun onDataSetChanged() {
        // Called when notifyAppWidgetViewDataChanged is triggered
        // This is called on the main thread, so we use runBlocking
        loadMessages()
    }

    private fun loadMessages() {
        try {
            // Load last 5 messages (synchronously for widget)
            // runBlocking is acceptable here because widget updates are infrequent
            messages = runBlocking {
                chatRepository.getRecentMessagesSync(limit = 5)
            }
            Timber.d("Widget loaded ${messages.size} messages")
        } catch (e: Exception) {
            Timber.e(e, "Failed to load messages for widget")
            messages = emptyList()
        }
    }

    override fun getViewAt(position: Int): RemoteViews {
        if (position >= messages.size) {
            return getEmptyView()
        }

        val message = messages[position]
        val views = RemoteViews(context.packageName, R.layout.item_widget_message)

        // Set icon based on message type and entity
        val icon = getMessageIcon(message)
        views.setTextViewText(R.id.messageIcon, icon)

        // Set message text
        val displayText = formatMessageText(message)
        views.setTextViewText(R.id.messageText, displayText)

        // Set timestamp
        val timeText = formatTime(message.timestamp)
        views.setTextViewText(R.id.messageTime, timeText)

        return views
    }

    private fun getMessageIcon(message: ChatMessage): String {
        return when {
            message.isFromUser -> "📱" // User message icon
            message.fromEntityCharacter == "LOBSTER" -> "🦞"
            message.fromEntityCharacter == "PIG" -> "🐷"
            else -> "💬" // Default message icon
        }
    }

    private fun formatMessageText(message: ChatMessage): String {
        return when {
            message.isFromUser -> {
                // User message: show target info if broadcast
                val targets = message.getTargetEntityIdList()
                if (targets.size > 1) {
                    "You → All: ${message.text}"
                } else {
                    "You: ${message.text}"
                }
            }
            message.messageType == MessageType.ENTITY_TO_ENTITY -> {
                // Entity to entity message
                val sourceName = message.fromEntityName ?: "Entity ${message.fromEntityId}"
                "$sourceName: ${message.text}"
            }
            else -> {
                // Entity response
                val entityName = message.fromEntityName ?: "Entity ${message.fromEntityId}"
                "$entityName: ${message.text}"
            }
        }
    }

    private fun formatTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60_000 -> "now" // Less than 1 minute
            diff < 3600_000 -> "${diff / 60_000}m" // Less than 1 hour, show minutes
            diff < 86400_000 -> { // Less than 24 hours, show time
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
            }
            else -> { // More than 24 hours, show date
                SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date(timestamp))
            }
        }
    }

    private fun getEmptyView(): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.item_widget_message)
        views.setTextViewText(R.id.messageIcon, "💬")
        views.setTextViewText(R.id.messageText, "No messages")
        views.setTextViewText(R.id.messageTime, "")
        return views
    }

    override fun getCount(): Int = messages.size

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true

    override fun onDestroy() {
        messages = emptyList()
    }
}

package com.hank.clawlive.ui.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hank.clawlive.R
import com.hank.clawlive.data.local.EntityAvatarManager
import com.hank.clawlive.data.local.database.ChatMessage
import com.hank.clawlive.data.local.database.MessageType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter for chat messages with dual ViewHolder types (sent/received)
 */
class ChatAdapter : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(ChatDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_SENT = 0
        private const val VIEW_TYPE_RECEIVED = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isFromUser) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SENT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_sent, parent, false)
                SentMessageViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_received, parent, false)
                ReceivedMessageViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is SentMessageViewHolder -> holder.bind(message)
            is ReceivedMessageViewHolder -> holder.bind(message)
        }
    }

    /**
     * ViewHolder for sent messages (right-aligned, green bubble)
     */
    class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvTargets: TextView = itemView.findViewById(R.id.tvTargets)

        fun bind(message: ChatMessage) {
            tvMessage.text = message.text
            tvTime.text = formatTime(message.timestamp)

            // Long-press to copy message text
            itemView.setOnLongClickListener {
                copyToClipboard(it, message.text)
                true
            }

            // Show target entities for broadcast messages
            if (message.messageType == MessageType.USER_BROADCAST) {
                val targets = message.getTargetEntityIdList()
                if (targets.size > 1) {
                    tvTargets.text = itemView.context.getString(
                        R.string.to_entities,
                        targets.joinToString(", ") { "Entity $it" }
                    )
                    tvTargets.visibility = View.VISIBLE
                } else {
                    tvTargets.visibility = View.GONE
                }
            } else {
                tvTargets.visibility = View.GONE
            }
        }
    }

    /**
     * ViewHolder for received messages (left-aligned, gray bubble)
     */
    class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAvatar: TextView = itemView.findViewById(R.id.tvAvatar)
        private val tvEntityName: TextView = itemView.findViewById(R.id.tvEntityName)
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val tvReadStatus: TextView = itemView.findViewById(R.id.tvReadStatus)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvReadReceipt: TextView = itemView.findViewById(R.id.tvReadReceipt)

        fun bind(message: ChatMessage) {
            tvMessage.text = message.text
            tvTime.text = formatTime(message.timestamp)

            // Long-press to copy message text
            itemView.setOnLongClickListener {
                copyToClipboard(it, message.text)
                true
            }

            // Entity name and avatar (synced from user's chosen avatar)
            val entityId = message.fromEntityId ?: 0
            val entityName = message.fromEntityName

            // Set avatar from EntityAvatarManager (synced across all screens)
            val avatarManager = EntityAvatarManager.getInstance(itemView.context)
            tvAvatar.text = avatarManager.getAvatar(entityId)

            // Set entity name
            tvEntityName.text = if (entityName.isNullOrEmpty()) {
                "Entity $entityId"
            } else {
                "$entityName (#$entityId)"
            }

<<<<<<< HEAD
            // Show "已讀" status if message was read
            // For now, show "Entity X 已讀" under the message
            // This can be enhanced with actual read status tracking later
            val readStatusText = "Entity $entityId 已讀"
            tvReadStatus.text = readStatusText
            tvReadStatus.visibility = View.VISIBLE
=======
            // Show read receipt: "Entity X 已讀"
            // Check if message contains "已讀" pattern or is from entity response
            val isReadReceipt = message.text.contains("已讀") || 
                              message.messageType == MessageType.ENTITY_RESPONSE
            if (isReadReceipt && message.fromEntityId != null) {
                tvReadReceipt.text = itemView.context.getString(
                    R.string.entity_read_receipt,
                    message.fromEntityId
                )
                tvReadReceipt.visibility = View.VISIBLE
            } else {
                tvReadReceipt.visibility = View.GONE
            }
>>>>>>> main
        }
    }

    /**
     * DiffUtil callback for efficient list updates
     */
    class ChatDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }
    }
}

/**
 * Format timestamp to readable time string
 */
private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

/**
 * Copy text to clipboard and show toast
 */
private fun copyToClipboard(view: View, text: String) {
    val context = view.context
    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("chat_message", text))
    Toast.makeText(context, context.getString(R.string.message_copied), Toast.LENGTH_SHORT).show()
}

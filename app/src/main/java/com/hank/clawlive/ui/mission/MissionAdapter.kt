package com.hank.clawlive.ui.mission

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hank.clawlive.R
import com.hank.clawlive.data.model.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Mission Adapter for TODO/Mission/Done Lists
 */
class MissionAdapter(
    private val onItemAction: (MissionItem, MissionAction) -> Unit
) : ListAdapter<MissionItem, MissionAdapter.MissionViewHolder>(MissionDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MissionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mission, parent, false)
        return MissionViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: MissionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class MissionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvPriority: TextView = itemView.findViewById(R.id.tvPriority)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvBot: TextView = itemView.findViewById(R.id.tvBot)
        private val tvEta: TextView = itemView.findViewById(R.id.tvEta)
        private val tvUpdatedAt: TextView = itemView.findViewById(R.id.tvUpdatedAt)
        
        fun bind(item: MissionItem) {
            tvTitle.text = item.title
            tvPriority.text = item.priority.label
            tvStatus.text = item.status.label
            tvBot.text = item.assignedBot ?: "-"
            tvEta.text = item.eta?.let { formatDate(it) } ?: "-"
            tvUpdatedAt.text = formatDate(item.updatedAt)
            
            // 按優先權設定顏色
            val priorityColor = when (item.priority) {
                Priority.CRITICAL -> R.color.priority_critical
                Priority.HIGH -> R.color.priority_high
                Priority.MEDIUM -> R.color.priority_medium
                Priority.LOW -> R.color.priority_low
            }
            tvPriority.setTextColor(itemView.context.getColor(priorityColor))
            
            // 點擊編輯
            itemView.setOnClickListener {
                onItemAction(item, MissionAction.EDIT)
            }
            
            // 長按選單
            itemView.setOnLongClickListener {
                showActionMenu(item)
                true
            }
        }
        
        private fun showActionMenu(item: MissionItem) {
            // TODO: 顯示 PopupMenu (移動、刪除等)
            when (item.status) {
                MissionStatus.PENDING -> {
                    onItemAction(item, MissionAction.MOVE_TO_MISSION)
                }
                MissionStatus.IN_PROGRESS -> {
                    onItemAction(item, MissionAction.MOVE_TO_DONE)
                }
                else -> {}
            }
        }
        
        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }
    
    class MissionDiffCallback : DiffUtil.ItemCallback<MissionItem>() {
        override fun areItemsTheSame(oldItem: MissionItem, newItem: MissionItem): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: MissionItem, newItem: MissionItem): Boolean {
            return oldItem == newItem
        }
    }
}

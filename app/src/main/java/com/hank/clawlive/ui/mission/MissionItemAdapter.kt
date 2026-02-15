package com.hank.clawlive.ui.mission

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hank.clawlive.R
import com.hank.clawlive.data.model.MissionItem
import com.hank.clawlive.data.model.MissionStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ListMode { TODO, MISSION, DONE }

class MissionItemAdapter(
    private val mode: ListMode,
    private val onItemClick: (MissionItem) -> Unit,
    private val onItemLongClick: (MissionItem) -> Unit
) : ListAdapter<MissionItem, MissionItemAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mission, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPriority: TextView = itemView.findViewById(R.id.tvPriority)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        private val detailsRow: LinearLayout = itemView.findViewById(R.id.detailsRow)
        private val tvBot: TextView = itemView.findViewById(R.id.tvBot)
        private val tvEta: TextView = itemView.findViewById(R.id.tvEta)
        private val tvCompletedAt: TextView = itemView.findViewById(R.id.tvCompletedAt)

        fun bind(item: MissionItem) {
            tvPriority.text = item.priority.label.take(2) // emoji only
            tvTitle.text = item.title
            tvStatus.text = item.status.label

            // Description
            if (item.description.isNotBlank()) {
                tvDescription.text = item.description
                tvDescription.visibility = View.VISIBLE
            } else {
                tvDescription.visibility = View.GONE
            }

            when (mode) {
                ListMode.TODO -> {
                    detailsRow.visibility = View.GONE
                    tvCompletedAt.visibility = View.GONE
                }
                ListMode.MISSION -> {
                    detailsRow.visibility = View.VISIBLE
                    tvBot.text = if (item.assignedBot != null) "🤖 ${item.assignedBot}" else "🤖 -"
                    tvEta.text = if (item.eta != null) "⏰ ${formatDateTime(item.eta)}" else "⏰ -"
                    tvCompletedAt.visibility = View.GONE
                }
                ListMode.DONE -> {
                    detailsRow.visibility = View.GONE
                    if (item.completedAt != null) {
                        tvCompletedAt.text = "✓ ${formatDateTime(item.completedAt)}"
                        tvCompletedAt.visibility = View.VISIBLE
                    } else {
                        tvCompletedAt.visibility = View.GONE
                    }
                }
            }

            itemView.setOnClickListener { onItemClick(item) }
            itemView.setOnLongClickListener {
                onItemLongClick(item)
                true
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<MissionItem>() {
        override fun areItemsTheSame(oldItem: MissionItem, newItem: MissionItem) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: MissionItem, newItem: MissionItem) =
            oldItem == newItem
    }
}

private val dateTimeFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())

private fun formatDateTime(timestamp: Long): String {
    return dateTimeFormat.format(Date(timestamp))
}

package com.hank.clawlive.ui.mission

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hank.clawlive.R
import com.hank.clawlive.data.model.MissionNote

class MissionNoteAdapter(
    private val onNoteClick: (MissionNote) -> Unit,
    private val onNoteLongClick: (MissionNote) -> Unit
) : ListAdapter<MissionNote, MissionNoteAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mission_note, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvContent: TextView = itemView.findViewById(R.id.tvContent)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)

        fun bind(note: MissionNote) {
            tvTitle.text = note.title
            tvContent.text = note.content
            tvCategory.text = note.category

            itemView.setOnClickListener { onNoteClick(note) }
            itemView.setOnLongClickListener {
                onNoteLongClick(note)
                true
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<MissionNote>() {
        override fun areItemsTheSame(oldItem: MissionNote, newItem: MissionNote) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: MissionNote, newItem: MissionNote) =
            oldItem == newItem
    }
}

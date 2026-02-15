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
import java.text.SimpleDateFormat
import java.util.*

/**
 * Note Adapter for Notes List
 * 
 * 功能：
 * - 顯示筆記標題、內容、分類
 * - 顯示建立時間
 * - 用戶可編輯，Bots 唯讀
 */
class NoteAdapter(
    private val onNoteAction: (MissionNote, NoteAction) -> Unit
) : ListAdapter<MissionNote, NoteAdapter.NoteViewHolder>(NoteDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvNoteTitle)
        private val tvContent: TextView = itemView.findViewById(R.id.tvNoteContent)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvNoteCategory)
        private val tvCreatedAt: TextView = itemView.findViewById(R.id.tvNoteCreatedAt)
        
        fun bind(note: MissionNote) {
            tvTitle.text = note.title
            tvContent.text = note.content
            tvCategory.text = note.category.uppercase()
            tvCreatedAt.text = formatDate(note.createdAt)
            
            // 顯示分類標籤顏色
            val categoryColor = getCategoryColor(note.category)
            tvCategory.setTextColor(itemView.context.getColor(categoryColor))
            
            // 點擊查看/編輯
            itemView.setOnClickListener {
                onNoteAction(note, NoteAction.EDIT)
            }
            
            // 長按複製內容
            itemView.setOnLongClickListener {
                copyNoteContent(note)
                true
            }
        }
        
        private fun getCategoryColor(category: String): Int {
            return when (category.lowercase()) {
                "important" -> R.color.category_important
                "todo" -> R.color.category_todo
                "idea" -> R.color.category_idea
                "bug" -> R.color.category_bug
                "general" -> R.color.category_general
                else -> R.color.category_general
            }
        }
        
        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
        
        private fun copyNoteContent(note: MissionNote) {
            val clipboard = itemView.context.getSystemService(android.content.ClipboardManager::class.java)
            val clip = android.content.ClipData.newPlainText("Note Content", note.content)
            clipboard.setPrimaryClip(clip)
            // TODO: 顯示 Toast 提示已複製
        }
    }
    
    class NoteDiffCallback : DiffUtil.ItemCallback<MissionNote>() {
        override fun areItemsTheSame(oldItem: MissionNote, newItem: MissionNote): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: MissionNote, newItem: MissionNote): Boolean {
            return oldItem == newItem
        }
    }
}

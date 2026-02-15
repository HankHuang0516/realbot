package com.hank.clawlive.ui.mission

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ToggleButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hank.clawlive.R
import com.hank.clawlive.data.model.MissionRule
import com.hank.clawlive.data.model.RuleType
import java.text.SimpleDateFormat
import java.util.*

/**
 * Rule Adapter for Rules List
 * 
 * 功能：
 * - 顯示規則名稱、描述、類型
 * - 顯示開關狀態 (isEnabled)
 * - 支援快速啟用/停用規則
 * - Bots 遵守的 workflow 規則
 */
class RuleAdapter(
    private val onRuleAction: (MissionRule, RuleAction) -> Unit
) : ListAdapter<MissionRule, RuleAdapter.RuleViewHolder>(RuleDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RuleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rule, parent, false)
        return RuleViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: RuleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class RuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvRuleName)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvRuleDescription)
        private val tvType: TextView = itemView.findViewById(R.id.tvRuleType)
        private val tvPriority: TextView = itemView.findViewById(R.id.tvRulePriority)
        private val tvUpdatedAt: TextView = itemView.findViewById(R.id.tvRuleUpdatedAt)
        private val toggleEnabled: ToggleButton = itemView.findViewById(R.id.toggleRuleEnabled)
        
        fun bind(rule: MissionRule) {
            tvName.text = rule.name
            tvDescription.text = rule.description
            tvType.text = rule.ruleType.label
            tvPriority.text = "優先權: ${rule.priority}"
            tvUpdatedAt.text = formatDate(rule.updatedAt)
            
            // 設定開關狀態
            toggleEnabled.isChecked = rule.isEnabled
            
            // 設定類型標籤顏色
            val typeColor = getRuleTypeColor(rule.ruleType)
            tvType.setTextColor(itemView.context.getColor(typeColor))
            
            // 設定開關狀態顏色
            val stateColor = if (rule.isEnabled) {
                R.color.rule_enabled
            } else {
                R.color.rule_disabled
            }
            toggleEnabled.setTextColor(itemView.context.getColor(stateColor))
            
            // 開關切換
            toggleEnabled.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked != rule.isEnabled) {
                    onRuleAction(rule, RuleAction.TOGGLE)
                }
            }
            
            // 點擊編輯
            itemView.setOnClickListener {
                onRuleAction(rule, RuleAction.EDIT)
            }
            
            // 長按顯示規則詳情
            itemView.setOnLongClickListener {
                showRuleDetail(rule)
                true
            }
        }
        
        private fun getRuleTypeColor(type: RuleType): Int {
            return when (type) {
                RuleType.WORKFLOW -> R.color.type_workflow
                RuleType.CODE_REVIEW -> R.color.type_code_review
                RuleType.COMMUNICATION -> R.color.type_communication
                RuleType.DEPLOYMENT -> R.color.type_deployment
                RuleType.SYNC -> R.color.type_sync
            }
        }
        
        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
        
        private fun showRuleDetail(rule: MissionRule) {
            // TODO: 顯示規則詳細資訊 Dialog
            val configStr = if (rule.config.isNotEmpty()) {
                "\n\n設定: ${rule.config}"
            } else ""
            val detail = "規則: ${rule.name}\n類型: ${rule.ruleType.label}\n說明: ${rule.description}$configStr"
            // TODO: 顯示 AlertDialog
        }
    }
    
    class RuleDiffCallback : DiffUtil.ItemCallback<MissionRule>() {
        override fun areItemsTheSame(oldItem: MissionRule, newItem: MissionRule): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: MissionRule, newItem: MissionRule): Boolean {
            return oldItem == newItem
        }
    }
}

/**
 * RuleType 擴展屬性 - 取得人類可讀的標籤
 */
private val RuleType.label: String
    get() = when (this) {
        RuleType.WORKFLOW -> "🔄 工作流程"
        RuleType.CODE_REVIEW -> "📝 Code Review"
        RuleType.COMMUNICATION -> "💬 通訊"
        RuleType.DEPLOYMENT -> "🚀 部署"
        RuleType.SYNC -> "🔁 同步"
    }

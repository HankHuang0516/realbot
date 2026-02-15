package com.hank.clawlive.ui.mission

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.materialswitch.MaterialSwitch
import com.hank.clawlive.R
import com.hank.clawlive.data.model.MissionRule

class MissionRuleAdapter(
    private val onRuleClick: (MissionRule) -> Unit,
    private val onRuleLongClick: (MissionRule) -> Unit,
    private val onToggle: (MissionRule) -> Unit
) : ListAdapter<MissionRule, MissionRuleAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mission_rule, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvRuleType: TextView = itemView.findViewById(R.id.tvRuleType)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        private val switchEnabled: MaterialSwitch = itemView.findViewById(R.id.switchEnabled)

        fun bind(rule: MissionRule) {
            tvName.text = rule.name
            tvRuleType.text = rule.ruleType.name
            tvDescription.text = rule.description

            // Avoid triggering listener during bind
            switchEnabled.setOnCheckedChangeListener(null)
            switchEnabled.isChecked = rule.isEnabled
            switchEnabled.setOnCheckedChangeListener { _, _ -> onToggle(rule) }

            itemView.setOnClickListener { onRuleClick(rule) }
            itemView.setOnLongClickListener {
                onRuleLongClick(rule)
                true
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<MissionRule>() {
        override fun areItemsTheSame(oldItem: MissionRule, newItem: MissionRule) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: MissionRule, newItem: MissionRule) =
            oldItem == newItem
    }
}

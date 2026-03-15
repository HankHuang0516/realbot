package com.hank.clawlive.ui.mission

import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hank.clawlive.R
import com.hank.clawlive.data.remote.SkillTemplate

/**
 * Gallery dialog listing rule templates with search bar and count badge
 * (aligned with Skill/Soul gallery and Web portal).
 * On selection, the callback receives the template name, description, and ruleType.
 */
class RuleGalleryDialog(
    private val context: Context,
    private val templates: List<SkillTemplate>,
    private val onTemplateSelected: (name: String, description: String, ruleType: String) -> Unit
) {
    fun show() {
        if (templates.isEmpty()) {
            MaterialAlertDialogBuilder(context)
                .setTitle(context.getString(R.string.mission_rule_gallery_title))
                .setMessage(context.getString(R.string.mission_template_empty))
                .setPositiveButton(android.R.string.ok, null)
                .show()
            return
        }

        val outerLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 8, 24, 8)
        }

        val etSearch = EditText(context).apply {
            hint = "搜尋…"
            inputType = android.text.InputType.TYPE_CLASS_TEXT
            setSingleLine(true)
            setBackgroundResource(android.R.drawable.edit_text)
        }
        val searchParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = 8 }
        outerLayout.addView(etSearch, searchParams)

        val scrollView = android.widget.ScrollView(context)
        val listLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 8, 0, 8)
        }
        scrollView.addView(listLayout)
        outerLayout.addView(scrollView)

        var dialog: androidx.appcompat.app.AlertDialog? = null

        fun buildRows(query: String) {
            listLayout.removeAllViews()
            val filtered = if (query.isBlank()) templates
            else templates.filter {
                it.label.contains(query, ignoreCase = true) ||
                (it.name?.contains(query, ignoreCase = true) == true) ||
                (it.author?.contains(query, ignoreCase = true) == true)
            }

            if (filtered.isEmpty()) {
                listLayout.addView(TextView(context).apply {
                    text = context.getString(R.string.mission_template_empty)
                    setPadding(0, 32, 0, 32)
                    setTextColor(android.graphics.Color.parseColor("#888888"))
                })
                return
            }

            for (tpl in filtered) {
                val row = LayoutInflater.from(context).inflate(R.layout.item_template_gallery, null)
                row.findViewById<TextView>(R.id.tvGalleryIcon).text = tpl.icon ?: "📋"
                row.findViewById<TextView>(R.id.tvGalleryTitle).text = tpl.label
                val ruleTypeDisplay = tpl.ruleType ?: "WORKFLOW"
                row.findViewById<TextView>(R.id.tvGalleryMeta).text =
                    "by ${tpl.author ?: "—"} · $ruleTypeDisplay"
                row.findViewById<TextView>(R.id.tvGalleryStatus).apply {
                    val desc = tpl.description ?: tpl.label
                    text = desc.take(60) + if (desc.length > 60) "…" else ""
                    setTextColor(android.graphics.Color.parseColor("#888888"))
                }
                row.setOnClickListener {
                    dialog?.dismiss()
                    val name = tpl.name ?: tpl.title
                    val desc = tpl.description ?: tpl.label
                    onTemplateSelected(name, desc, ruleTypeDisplay)
                }
                row.findViewById<MaterialButton>(R.id.btnGallerySelect).setOnClickListener {
                    dialog?.dismiss()
                    val name = tpl.name ?: tpl.title
                    val desc = tpl.description ?: tpl.label
                    onTemplateSelected(name, desc, ruleTypeDisplay)
                }
                listLayout.addView(row)
            }
        }

        buildRows("")

        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                buildRows(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        val title = "${context.getString(R.string.mission_rule_gallery_title)} (${templates.size})"
        dialog = MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setView(outerLayout)
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}

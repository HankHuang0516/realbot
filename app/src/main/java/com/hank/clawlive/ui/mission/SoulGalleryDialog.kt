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
 * Gallery dialog listing both built-in and community soul templates.
 * Includes search bar and count badge (aligned with Skill gallery and Web portal).
 * Callback receives (name, description, templateId) where templateId is non-null
 * for built-in templates and null for community templates.
 */
class SoulGalleryDialog(
    private val context: Context,
    private val builtinTemplates: List<BuiltinTemplate>,
    private val communityTemplates: List<SkillTemplate>,
    private val onSelected: (name: String, description: String, templateId: String?) -> Unit
) {
    data class BuiltinTemplate(
        val id: String,
        val icon: String,
        val name: String,
        val description: String
    )

    private data class Item(
        val label: String,
        val icon: String,
        val name: String,
        val description: String,
        val author: String,
        val templateId: String?
    )

    fun show() {
        val items = mutableListOf<Item>()
        builtinTemplates.forEach { t ->
            items.add(Item(t.name, t.icon, t.name, t.description, "", t.id))
        }
        communityTemplates.forEach { t ->
            val label = t.label ?: t.name ?: ""
            val name = t.name ?: t.title ?: label
            val desc = t.description ?: label
            val author = t.author ?: ""
            items.add(Item(label, t.icon ?: "🧠", name, desc, author, null))
        }

        val total = items.size

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
            val filtered = if (query.isBlank()) items
            else items.filter {
                it.label.contains(query, ignoreCase = true) ||
                it.name.contains(query, ignoreCase = true) ||
                it.author.contains(query, ignoreCase = true)
            }

            if (filtered.isEmpty()) {
                listLayout.addView(TextView(context).apply {
                    text = context.getString(R.string.mission_template_empty)
                    setPadding(0, 32, 0, 32)
                    setTextColor(android.graphics.Color.parseColor("#888888"))
                })
                return
            }

            for (item in filtered) {
                val row = LayoutInflater.from(context).inflate(R.layout.item_template_gallery, null)
                row.findViewById<TextView>(R.id.tvGalleryIcon).text = item.icon
                row.findViewById<TextView>(R.id.tvGalleryTitle).text = item.label
                val meta = if (item.templateId != null) context.getString(R.string.mission_soul_builtin)
                    else if (item.author.isNotEmpty()) "by ${item.author}"
                    else ""
                row.findViewById<TextView>(R.id.tvGalleryMeta).text = meta
                row.findViewById<TextView>(R.id.tvGalleryStatus).apply {
                    text = item.description.take(60) + if (item.description.length > 60) "…" else ""
                    setTextColor(android.graphics.Color.parseColor("#888888"))
                }
                row.setOnClickListener {
                    dialog?.dismiss()
                    onSelected(item.name, item.description, item.templateId)
                }
                row.findViewById<MaterialButton>(R.id.btnGallerySelect).setOnClickListener {
                    dialog?.dismiss()
                    onSelected(item.name, item.description, item.templateId)
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

        val title = "${context.getString(R.string.mission_soul_gallery_title)} ($total)"
        dialog = MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setView(outerLayout)
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}

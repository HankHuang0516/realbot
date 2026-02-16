package com.hank.clawlive

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.hank.clawlive.data.local.DeviceManager
import com.hank.clawlive.data.model.*
import com.hank.clawlive.ui.MissionUiState
import com.hank.clawlive.ui.MissionViewModel
import com.hank.clawlive.ui.mission.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MissionControlActivity : AppCompatActivity() {

    private val viewModel: MissionViewModel by viewModels()

    private lateinit var todoAdapter: MissionItemAdapter
    private lateinit var missionAdapter: MissionItemAdapter
    private lateinit var doneAdapter: MissionItemAdapter
    private lateinit var noteAdapter: MissionNoteAdapter
    private lateinit var ruleAdapter: MissionRuleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_mission_control)

        setupWindowInsets()
        setupAdapters()
        setupRecyclerViews()
        setupButtons()
        observeState()
    }

    private fun setupWindowInsets() {
        val topBar = findViewById<View>(R.id.topBar)
        ViewCompat.setOnApplyWindowInsetsListener(topBar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, systemBars.top + 12, v.paddingRight, v.paddingBottom)
            insets
        }
    }

    private fun setupAdapters() {
        todoAdapter = MissionItemAdapter(ListMode.TODO,
            onItemClick = { showEditItemDialog(it) },
            onItemLongClick = { showItemActionMenu(it, ListMode.TODO) }
        )
        missionAdapter = MissionItemAdapter(ListMode.MISSION,
            onItemClick = { showEditItemDialog(it) },
            onItemLongClick = { showItemActionMenu(it, ListMode.MISSION) }
        )
        doneAdapter = MissionItemAdapter(ListMode.DONE,
            onItemClick = { },
            onItemLongClick = { showItemActionMenu(it, ListMode.DONE) }
        )
        noteAdapter = MissionNoteAdapter(
            onNoteClick = { showEditNoteDialog(it) },
            onNoteLongClick = { showDeleteConfirm(it.title) { viewModel.deleteNote(it.id) } }
        )
        ruleAdapter = MissionRuleAdapter(
            onRuleClick = { showEditRuleDialog(it) },
            onRuleLongClick = { showDeleteConfirm(it.name) { viewModel.deleteRule(it.id) } },
            onToggle = { viewModel.toggleRule(it.id) }
        )
    }

    private fun setupRecyclerViews() {
        fun setup(rv: RecyclerView, adapter: RecyclerView.Adapter<*>) {
            rv.layoutManager = LinearLayoutManager(this)
            rv.adapter = adapter
        }
        setup(findViewById(R.id.rvTodo), todoAdapter)
        setup(findViewById(R.id.rvMission), missionAdapter)
        setup(findViewById(R.id.rvDone), doneAdapter)
        setup(findViewById(R.id.rvNotes), noteAdapter)
        setup(findViewById(R.id.rvRules), ruleAdapter)
    }

    private fun setupButtons() {
        findViewById<View>(R.id.btnBack).setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        findViewById<MaterialButton>(R.id.btnWebSync).setOnClickListener { showWebSyncDialog() }

        findViewById<MaterialButton>(R.id.btnUpload).setOnClickListener {
            viewModel.uploadDashboard { yourVersion, serverVersion ->
                runOnUiThread {
                    AlertDialog.Builder(this)
                        .setTitle("版本衝突")
                        .setMessage("Dashboard 已被其他客戶端更新 (你的版本: $yourVersion, 伺服器: $serverVersion)。要下載最新版本嗎？")
                        .setPositiveButton("下載最新") { _, _ -> viewModel.downloadDashboard() }
                        .setNegativeButton(R.string.cancel, null)
                        .show()
                }
            }
        }

        findViewById<MaterialButton>(R.id.btnAddTodo).setOnClickListener { showAddItemDialog() }
        findViewById<MaterialButton>(R.id.btnAddNote).setOnClickListener { showAddNoteDialog() }
        findViewById<MaterialButton>(R.id.btnAddRule).setOnClickListener { showAddRuleDialog() }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> updateUI(state) }
            }
        }
    }

    private fun updateUI(state: MissionUiState) {
        // Loading
        findViewById<View>(R.id.progressBar).visibility =
            if (state.isLoading || state.isSyncing) View.VISIBLE else View.GONE

        // Upload button
        val btnUpload = findViewById<MaterialButton>(R.id.btnUpload)
        btnUpload.isEnabled = !state.isSyncing
        btnUpload.text = if (state.isSyncing) getString(R.string.uploading)
        else if (state.hasLocalChanges) getString(R.string.upload) + " *"
        else getString(R.string.upload)

        // Lists
        todoAdapter.submitList(state.todoList)
        missionAdapter.submitList(state.missionList)
        doneAdapter.submitList(state.doneList)
        noteAdapter.submitList(state.notes)
        ruleAdapter.submitList(state.rules)

        // Empty states
        findViewById<View>(R.id.tvTodoEmpty).visibility =
            if (state.todoList.isEmpty() && !state.isLoading) View.VISIBLE else View.GONE
        findViewById<View>(R.id.tvMissionEmpty).visibility =
            if (state.missionList.isEmpty() && !state.isLoading) View.VISIBLE else View.GONE
        findViewById<View>(R.id.tvDoneEmpty).visibility =
            if (state.doneList.isEmpty() && !state.isLoading) View.VISIBLE else View.GONE
        findViewById<View>(R.id.tvNotesEmpty).visibility =
            if (state.notes.isEmpty() && !state.isLoading) View.VISIBLE else View.GONE
        findViewById<View>(R.id.tvRulesEmpty).visibility =
            if (state.rules.isEmpty() && !state.isLoading) View.VISIBLE else View.GONE

        // Sync status
        val syncText = buildString {
            append("v${state.version}")
            if (state.lastSyncedAt != null && state.lastSyncedAt > 0) {
                val sdf = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
                append(" | 上次同步: ${sdf.format(Date(state.lastSyncedAt))}")
            }
        }
        findViewById<TextView>(R.id.tvSyncStatus).text = syncText

        // Error
        if (state.error != null) {
            Toast.makeText(this, state.error, Toast.LENGTH_SHORT).show()
        }
    }

    // ============================================
    // Web Sync
    // ============================================

    private fun showWebSyncDialog() {
        val dm = DeviceManager.getInstance(this)
        val deviceId = dm.deviceId
        val deviceSecret = dm.deviceSecret

        val msg = "在瀏覽器開啟 Mission Control 網頁後，\n輸入以下憑證即可同步：\n\n" +
            "Device ID:\n$deviceId\n\n" +
            "Device Secret:\n$deviceSecret"

        AlertDialog.Builder(this)
            .setTitle("網頁同步憑證")
            .setMessage(msg)
            .setPositiveButton("複製全部") { _, _ ->
                val clip = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                clip.setPrimaryClip(ClipData.newPlainText("credentials",
                    "Device ID: $deviceId\nDevice Secret: $deviceSecret"))
                Toast.makeText(this, "已複製到剪貼簿", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("複製網址") { _, _ ->
                val clip = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                clip.setPrimaryClip(ClipData.newPlainText("url",
                    "https://eclaw.up.railway.app/mission/mission.html"))
                Toast.makeText(this, "已複製網址", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    // ============================================
    // Dialogs
    // ============================================

    private fun showAddItemDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_mission_item, null)
        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val spinnerPriority = view.findViewById<Spinner>(R.id.spinnerPriority)

        val priorities = Priority.values()
        spinnerPriority.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_dropdown_item,
            priorities.map { it.label })
        spinnerPriority.setSelection(1) // MEDIUM

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.add_todo))
            .setView(view)
            .setPositiveButton(R.string.send) { _, _ ->
                val title = etTitle.text.toString().trim()
                if (title.isNotEmpty()) {
                    viewModel.addTodoItem(
                        title = title,
                        description = etDescription.text.toString().trim(),
                        priority = priorities[spinnerPriority.selectedItemPosition]
                    )
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showEditItemDialog(item: MissionItem) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_mission_item, null)
        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val spinnerPriority = view.findViewById<Spinner>(R.id.spinnerPriority)

        val priorities = Priority.values()
        spinnerPriority.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_dropdown_item,
            priorities.map { it.label })

        etTitle.setText(item.title)
        etDescription.setText(item.description)
        spinnerPriority.setSelection(priorities.indexOf(item.priority))

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.edit))
            .setView(view)
            .setPositiveButton(R.string.done) { _, _ ->
                val title = etTitle.text.toString().trim()
                if (title.isNotEmpty()) {
                    viewModel.editItem(
                        itemId = item.id,
                        title = title,
                        description = etDescription.text.toString().trim(),
                        priority = priorities[spinnerPriority.selectedItemPosition]
                    )
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showItemActionMenu(item: MissionItem, mode: ListMode) {
        val options = mutableListOf<String>()
        val actions = mutableListOf<() -> Unit>()

        if (mode == ListMode.TODO) {
            options.add("➡️ 移至 Mission")
            actions.add { viewModel.moveToMission(item.id) }
            options.add("✅ 標記完成")
            actions.add { viewModel.moveToDone(item.id) }
        }
        if (mode == ListMode.MISSION) {
            options.add("✅ 標記完成")
            actions.add { viewModel.moveToDone(item.id) }
        }
        options.add("✏️ 編輯")
        actions.add { showEditItemDialog(item) }
        options.add("🗑️ 刪除")
        actions.add { showDeleteConfirm(item.title) { viewModel.deleteItem(item.id) } }

        AlertDialog.Builder(this)
            .setTitle(item.title)
            .setItems(options.toTypedArray()) { _, which -> actions[which]() }
            .show()
    }

    private fun showAddNoteDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_mission_note, null)
        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etContent = view.findViewById<EditText>(R.id.etContent)
        val etCategory = view.findViewById<EditText>(R.id.etCategory)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.add_note))
            .setView(view)
            .setPositiveButton(R.string.send) { _, _ ->
                val title = etTitle.text.toString().trim()
                val content = etContent.text.toString().trim()
                if (title.isNotEmpty()) {
                    viewModel.addNote(
                        title = title,
                        content = content,
                        category = etCategory.text.toString().trim().ifEmpty { "general" }
                    )
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showEditNoteDialog(note: MissionNote) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_mission_note, null)
        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etContent = view.findViewById<EditText>(R.id.etContent)
        val etCategory = view.findViewById<EditText>(R.id.etCategory)

        etTitle.setText(note.title)
        etContent.setText(note.content)
        etCategory.setText(note.category)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.edit))
            .setView(view)
            .setPositiveButton(R.string.done) { _, _ ->
                val title = etTitle.text.toString().trim()
                if (title.isNotEmpty()) {
                    viewModel.editNote(
                        noteId = note.id,
                        title = title,
                        content = etContent.text.toString().trim(),
                        category = etCategory.text.toString().trim().ifEmpty { "general" }
                    )
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showAddRuleDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_mission_rule, null)
        val etName = view.findViewById<EditText>(R.id.etName)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val spinnerType = view.findViewById<Spinner>(R.id.spinnerRuleType)

        val types = RuleType.values()
        spinnerType.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_dropdown_item,
            types.map { it.name })

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.add_rule))
            .setView(view)
            .setPositiveButton(R.string.send) { _, _ ->
                val name = etName.text.toString().trim()
                if (name.isNotEmpty()) {
                    viewModel.addRule(
                        name = name,
                        description = etDescription.text.toString().trim(),
                        ruleType = types[spinnerType.selectedItemPosition]
                    )
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showEditRuleDialog(rule: MissionRule) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_mission_rule, null)
        val etName = view.findViewById<EditText>(R.id.etName)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val spinnerType = view.findViewById<Spinner>(R.id.spinnerRuleType)

        val types = RuleType.values()
        spinnerType.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_dropdown_item,
            types.map { it.name })

        etName.setText(rule.name)
        etDescription.setText(rule.description)
        spinnerType.setSelection(types.indexOf(rule.ruleType))

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.edit))
            .setView(view)
            .setPositiveButton(R.string.done) { _, _ ->
                val name = etName.text.toString().trim()
                if (name.isNotEmpty()) {
                    viewModel.editRule(
                        ruleId = rule.id,
                        name = name,
                        description = etDescription.text.toString().trim(),
                        ruleType = types[spinnerType.selectedItemPosition]
                    )
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showDeleteConfirm(name: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("刪除確認")
            .setMessage("確定要刪除「$name」嗎？")
            .setPositiveButton("刪除") { _, _ -> onConfirm() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}

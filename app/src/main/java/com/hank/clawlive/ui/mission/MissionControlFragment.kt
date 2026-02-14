package com.hank.clawlive.ui.mission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hank.clawlive.R
import com.hank.clawlive.data.model.*

/**
 * Mission Control Dashboard Fragment
 * 
 * 包含：
 * 1. TODO List (優先權排序)
 * 2. Mission List (bot, 狀態, ETA)
 * 3. Done List (完成日期時間)
 * 4. Notes (Bot唯讀)
 * 5. Rules (工作流程)
 */
class MissionControlFragment : Fragment() {
    
    private lateinit var rvTodo: RecyclerView
    private lateinit var rvMission: RecyclerView
    private lateinit var rvDone: RecyclerView
    private lateinit var rvNotes: RecyclerView
    private lateinit var rvRules: RecyclerView
    
    private lateinit var todoAdapter: MissionAdapter
    private lateinit var missionAdapter: MissionAdapter
    private lateinit var doneAdapter: MissionAdapter
    private lateinit var notesAdapter: NoteAdapter
    private lateinit var rulesAdapter: RuleAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mission_control, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupAdapters()
        loadDashboard()
    }
    
    private fun initViews(view: View) {
        rvTodo = view.findViewById(R.id.rvTodo)
        rvMission = view.findViewById(R.id.rvMission)
        rvDone = view.findViewById(R.id.rvDone)
        rvNotes = view.findViewById(R.id.rvNotes)
        rvRules = view.findViewById(R.id.rvRules)
    }
    
    private fun setupAdapters() {
        todoAdapter = MissionAdapter { item, action -> 
            handleMissionAction(item, action) 
        }
        missionAdapter = MissionAdapter { item, action -> 
            handleMissionAction(item, action) 
        }
        doneAdapter = MissionAdapter { item, action -> 
            handleMissionAction(item, action) 
        }
        notesAdapter = NoteAdapter { note, action -> 
            handleNoteAction(note, action) 
        }
        rulesAdapter = RuleAdapter { rule, action -> 
            handleRuleAction(rule, action) 
        }
        
        rvTodo.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = todoAdapter
        }
        rvMission.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = missionAdapter
        }
        rvDone.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = doneAdapter
        }
        rvNotes.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = notesAdapter
        }
        rvRules.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = rulesAdapter
        }
    }
    
    private fun loadDashboard() {
        // TODO: 從 API 載入 Dashboard
        // repository.getMissionDashboard(...)
    }
    
    private fun handleMissionAction(item: MissionItem, action: MissionAction) {
        when (action) {
            MissionAction.EDIT -> { /* 編輯任務 */ }
            MissionAction.MOVE_TO_MISSION -> { /* 移到 Mission List */ }
            MissionAction.MOVE_TO_DONE -> { /* 移到 Done List */ }
            MissionAction.DELETE -> { /* 刪除 */ }
        }
    }
    
    private fun handleNoteAction(note: MissionNote, action: NoteAction) {
        when (action) {
            NoteAction.EDIT -> { /* 編輯筆記 (用戶) */ }
            NoteAction.READ -> { /* Bot 唯讀 */ }
        }
    }
    
    private fun handleRuleAction(rule: MissionRule, action: RuleAction) {
        when (action) {
            RuleAction.TOGGLE -> { /* 切換開關 */ }
            RuleAction.EDIT -> { /* 編輯規則 */ }
        }
    }
}

enum class MissionAction {
    EDIT, MOVE_TO_MISSION, MOVE_TO_DONE, DELETE
}

enum class NoteAction {
    EDIT, READ
}

enum class RuleAction {
    TOGGLE, EDIT
}

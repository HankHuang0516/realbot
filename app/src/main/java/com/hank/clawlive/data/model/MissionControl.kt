package com.hank.clawlive.data.model

import java.util.UUID

/**
 * Mission Control Dashboard - 資料模型
 * 
 * 支援：
 * 1. TODO List - 優先權排序
 * 2. Mission List - bot名稱、狀態、ETA
 * 3. Done List - 完成日期時間
 * 4. Notes - Bot唯讀，用戶可寫
 * 5. Rule List - workflow 規則
 */

enum class Priority(val value: Int, val label: String) {
    LOW(1, "🟢 低"),
    MEDIUM(2, "🟡 中"),
    HIGH(3, "🟠 高"),
    CRITICAL(4, "🔴 緊急")
}

enum class MissionStatus(val label: String) {
    PENDING("待處理"),
    IN_PROGRESS("執行中"),
    BLOCKED("阻塞中"),
    DONE("完成"),
    CANCELLED("已取消")
}

/**
 * 任務項目 (TODO/Mission/Done)
 */
data class MissionItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val priority: Priority = Priority.MEDIUM,
    val status: MissionStatus = MissionStatus.PENDING,
    val assignedBot: String? = null,  // 負責的 bot ID
    val eta: Long? = null,            // 預計完成時間 (timestamp)
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val createdBy: String = "user"
)

/**
 * 筆記 (用戶可寫，Bots 唯讀)
 */
data class MissionNote(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val category: String = "general",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String = "user"
)

/**
 * 規則列表 (bots 遵守的 workflow)
 */
data class MissionRule(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val ruleType: RuleType,
    val isEnabled: Boolean = true,
    val priority: Int = 0,
    val config: Map<String, Any> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class RuleType {
    WORKFLOW,      // 工作流程規則
    CODE_REVIEW,   // Code Review 規則
    COMMUNICATION, // 通訊規則
    DEPLOYMENT,    // 部署規則
    SYNC           // 同步規則
}

/**
 * Dashboard 完整快照
 */
data class MissionDashboardSnapshot(
    val todoList: List<MissionItem>,
    val missionList: List<MissionItem>,
    val doneList: List<MissionItem>,
    val notes: List<MissionNote>,
    val rules: List<MissionRule>,
    val version: Int = 1,
    val lastSyncedAt: Long = System.currentTimeMillis()
)

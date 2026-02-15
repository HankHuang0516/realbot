package com.hank.clawlive.data.repository

import com.hank.clawlive.data.local.database.AppDatabase
import com.hank.clawlive.data.model.*
import com.hank.clawlive.data.remote.ClawApiService
import com.hank.clawlive.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Mission Control Repository
 * 
 * 功能：
 * 1. 從後端 API 同步 Dashboard
 * 2. 儲存到本地資料庫
 * 3. 提供給 UI 使用
 */
class MissionRepository(
    private val api: ClawApiService,
    private val database: AppDatabase
) {
    private val missionDao = database.missionDao()
    
    /**
     * 取得完整 Dashboard
     */
    suspend fun getDashboard(deviceId: String, entityId: Int, botSecret: String): 
            Result<MissionDashboardSnapshot> = withContext(Dispatchers.IO) {
        try {
            // 嘗試從 API 取得
            val response = api.getMissionDashboard(
                mapOf(
                    "deviceId" to deviceId,
                    "entityId" to entityId.toString(),
                    "botSecret" to botSecret
                )
            )
            
            if (response.isSuccessful && response.body()?.success == true) {
                val dashboard = response.body()!!.dashboard
                
                // 快取到本地
                saveDashboard(dashboard)
                
                Result.Success(dashboard)
            } else {
                // 從本地載入
                val local = getLocalDashboard(deviceId)
                if (local != null) {
                    Result.Success(local)
                } else {
                    Result.Error(Exception("Failed to load dashboard"))
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching dashboard")
            
            // 從本地載入
            val local = getLocalDashboard(deviceId)
            if (local != null) {
                Result.Success(local)
            } else {
                Result.Error(e)
            }
        }
    }
    
    /**
     * 上傳 Dashboard 到後端
     */
    suspend fun uploadDashboard(
        deviceId: String, 
        entityId: Int, 
        botSecret: String,
        dashboard: MissionDashboardSnapshot
    ): Result<MissionDashboardSnapshot> = withContext(Dispatchers.IO) {
        try {
            val response = api.uploadMissionDashboard(
                mapOf(
                    "deviceId" to deviceId,
                    "entityId" to entityId.toString(),
                    "botSecret" to botSecret
                ),
                dashboard
            )
            
            if (response.isSuccessful && response.body()?.success == true) {
                // 更新本地版本
                saveDashboard(dashboard.copy(
                    version = response.body()!!.version ?: dashboard.version + 1,
                    lastSyncedAt = System.currentTimeMillis()
                ))
                
                Result.Success(dashboard)
            } else {
                Result.Error(Exception("Upload failed: ${response.body()?.error}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error uploading dashboard")
            Result.Error(e)
        }
    }
    
    /**
     * 新增任務
     */
    suspend fun addMissionItem(
        deviceId: String,
        entityId: Int,
        botSecret: String,
        item: MissionItem,
        list: String  // 'todo', 'mission', 'done'
    ): Result<MissionItem> = withContext(Dispatchers.IO) {
        try {
            val response = api.addMissionItem(
                mapOf(
                    "deviceId" to deviceId,
                    "entityId" to entityId.toString(),
                    "botSecret" to botSecret
                ),
                item,
                list
            )
            
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(item)
            } else {
                Result.Error(Exception("Failed to add item"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error adding mission item")
            Result.Error(e)
        }
    }
    
    /**
     * 取得所有任務
     */
    suspend fun getAllMissionItems(
        deviceId: String,
        entityId: Int,
        botSecret: String,
        status: String? = null,
        priority: String? = null
    ): Result<List<MissionItem>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMissionItems(
                mapOf(
                    "deviceId" to deviceId,
                    "entityId" to entityId.toString(),
                    "botSecret" to botSecret,
                    "status" to (status ?: ""),
                    "priority" to (priority ?: "")
                )
            )
            
            if (response.isSuccessful) {
                Result.Success(response.body()?.items ?: emptyList())
            } else {
                Result.Error(Exception("Failed to get items"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting mission items")
            Result.Error(e)
        }
    }
    
    /**
     * 取得筆記 (Bots 可讀)
     */
    suspend fun getNotes(
        deviceId: String,
        entityId: Int,
        botSecret: String,
        category: String? = null
    ): Result<List<MissionNote>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMissionNotes(
                mapOf(
                    "deviceId" to deviceId,
                    "entityId" to entityId.toString(),
                    "botSecret" to botSecret,
                    "category" to (category ?: "")
                )
            )
            
            if (response.isSuccessful) {
                Result.Success(response.body()?.notes ?: emptyList())
            } else {
                Result.Error(Exception("Failed to get notes"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting notes")
            Result.Error(e)
        }
    }
    
    /**
     * 取得規則
     */
    suspend fun getRules(
        deviceId: String,
        entityId: Int,
        botSecret: String,
        type: String? = null
    ): Result<List<MissionRule>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMissionRules(
                mapOf(
                    "deviceId" to deviceId,
                    "entityId" to entityId.toString(),
                    "botSecret" to botSecret,
                    "type" to (type ?: "")
                )
            )
            
            if (response.isSuccessful) {
                Result.Success(response.body()?.rules ?: emptyList())
            } else {
                Result.Error(Exception("Failed to get rules"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting rules")
            Result.Error(e)
        }
    }

    /**
     * 新增筆記
     */
    suspend fun addNote(
        deviceId: String,
        entityId: Int,
        botSecret: String,
        note: MissionNote
    ): Result<MissionNote> = withContext(Dispatchers.IO) {
        try {
            val response = api.addMissionNote(
                mapOf(
                    "deviceId" to deviceId,
                    "entityId" to entityId.toString(),
                    "botSecret" to botSecret
                ),
                note
            )

            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(note)
            } else {
                Result.Error(Exception("Failed to add note: ${response.body()?.error}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error adding note")
            Result.Error(e)
        }
    }

    /**
     * 更新筆記
     */
    suspend fun updateNote(
        deviceId: String,
        entityId: Int,
        botSecret: String,
        noteId: String,
        note: MissionNote
    ): Result<MissionNote> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateMissionNote(
                mapOf(
                    "deviceId" to deviceId,
                    "entityId" to entityId.toString(),
                    "botSecret" to botSecret
                ),
                noteId,
                note
            )

            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(note)
            } else {
                Result.Error(Exception("Failed to update note: ${response.body()?.error}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating note")
            Result.Error(e)
        }
    }

    /**
     * 刪除筆記
     */
    suspend fun deleteNote(
        deviceId: String,
        entityId: Int,
        botSecret: String,
        noteId: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteMissionNote(
                mapOf(
                    "deviceId" to deviceId,
                    "entityId" to entityId.toString(),
                    "botSecret" to botSecret
                ),
                mapOf("noteId" to noteId)
            )

            if (response.isSuccessful) {
                Result.Success(true)
            } else {
                Result.Error(Exception("Failed to delete note"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting note")
            Result.Error(e)
        }
    }

    /**
     * 更新規則 (toggle 或編輯)
     */
    suspend fun updateRule(
        deviceId: String,
        entityId: Int,
        botSecret: String,
        ruleId: String,
        rule: MissionRule
    ): Result<MissionRule> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateMissionRule(
                mapOf(
                    "deviceId" to deviceId,
                    "entityId" to entityId.toString(),
                    "botSecret" to botSecret
                ),
                ruleId,
                rule
            )

            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(rule)
            } else {
                Result.Error(Exception("Failed to update rule: ${response.body()?.error}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating rule")
            Result.Error(e)
        }
    }

    // ============ Local Storage ============
    
    private suspend fun saveDashboard(dashboard: MissionDashboardSnapshot) {
        missionDao.insertDashboard(dashboard)
    }
    
    private suspend fun getLocalDashboard(deviceId: String): MissionDashboardSnapshot? {
        return missionDao.getDashboard(deviceId)
    }
}

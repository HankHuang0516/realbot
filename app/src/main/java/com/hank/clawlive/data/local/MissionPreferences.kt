package com.hank.clawlive.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hank.clawlive.data.model.MissionDashboardSnapshot
import com.hank.clawlive.data.model.MissionItem
import com.hank.clawlive.data.model.MissionNote
import com.hank.clawlive.data.model.MissionRule

class MissionPreferences private constructor(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val gson = Gson()

    fun saveDashboard(snapshot: MissionDashboardSnapshot) {
        prefs.edit()
            .putString(KEY_DASHBOARD, gson.toJson(snapshot))
            .putInt(KEY_VERSION, snapshot.version)
            .putLong(KEY_LAST_SYNCED, snapshot.lastSyncedAt)
            .apply()
    }

    fun loadDashboard(): MissionDashboardSnapshot? {
        val json = prefs.getString(KEY_DASHBOARD, null) ?: return null
        return try {
            gson.fromJson(json, MissionDashboardSnapshot::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun getVersion(): Int = prefs.getInt(KEY_VERSION, 1)

    fun getLastSyncedAt(): Long = prefs.getLong(KEY_LAST_SYNCED, 0)

    fun clearDashboard() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "mission_dashboard_prefs"
        private const val KEY_DASHBOARD = "dashboard_json"
        private const val KEY_VERSION = "dashboard_version"
        private const val KEY_LAST_SYNCED = "last_synced_at"

        @Volatile
        private var instance: MissionPreferences? = null

        fun getInstance(context: Context): MissionPreferences {
            return instance ?: synchronized(this) {
                instance ?: MissionPreferences(context.applicationContext).also { instance = it }
            }
        }
    }
}

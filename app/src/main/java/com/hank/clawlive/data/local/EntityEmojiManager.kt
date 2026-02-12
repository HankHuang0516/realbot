package com.hank.clawlive.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages entity emoji avatars.
 * Each entity (identified by entityId) has a customizable emoji.
 */
class EntityEmojiManager private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("entity_emojis", Context.MODE_PRIVATE)

    companion object {
        @Volatile
        private var INSTANCE: EntityEmojiManager? = null

        fun getInstance(context: Context): EntityEmojiManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: EntityEmojiManager(context).also { INSTANCE = it }
            }
        }

        /** Default emoji for each entity slot */
        val DEFAULT_EMOJIS = mapOf(
            0 to "🦞",
            1 to "🐷",
            2 to "🦞",
            3 to "🐷"
        )

        /** 30 emoji options organized by category (6 columns x 5 rows) */
        val EMOJI_OPTIONS = listOf(
            // Row 1: Sea creatures
            "🦞", "🦀", "🐙", "🦑", "🐠", "🐟",
            // Row 2: Animals
            "🐷", "🐶", "🐱", "🐰", "🦊", "🐻",
            // Row 3: More animals
            "🐼", "🐨", "🦁", "🐯", "🐸", "🐵",
            // Row 4: Robots & fantasy
            "🤖", "👾", "🎃", "👻", "🦄", "🐲",
            // Row 5: People & misc
            "😎", "🥷", "🧙", "🦸", "🐧", "🦅"
        )
    }

    fun getEmoji(entityId: Int): String {
        return prefs.getString("emoji_$entityId", DEFAULT_EMOJIS[entityId] ?: "🦞") ?: "🦞"
    }

    fun setEmoji(entityId: Int, emoji: String) {
        prefs.edit().putString("emoji_$entityId", emoji).apply()
    }
}

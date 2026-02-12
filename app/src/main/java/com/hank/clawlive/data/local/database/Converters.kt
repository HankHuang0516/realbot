package com.hank.clawlive.data.local.database

import androidx.room.TypeConverter

/**
 * Type converters for Room database
 */
class Converters {

    @TypeConverter
    fun fromMessageType(value: MessageType): String {
        return value.name
    }

    @TypeConverter
    fun toMessageType(value: String): MessageType {
        return try {
            MessageType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            MessageType.ENTITY_RESPONSE
        }
    }
}

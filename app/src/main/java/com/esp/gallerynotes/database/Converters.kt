package com.esp.gallerynotes.database

import androidx.room.TypeConverter
import com.esp.gallerynotes.utils.Priority
import java.util.*

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }

    @TypeConverter
    fun fromPriority(priority: Priority?) : String? {
        return priority?.name
    }

    @TypeConverter
    fun toPriority(s: String) : Priority? {
        return when(s) {
            "HIGH" -> Priority.HIGH
            "MEDIUM" -> Priority.MEDIUM
            "LOW" -> Priority.LOW
            else -> null
        }
    }

}
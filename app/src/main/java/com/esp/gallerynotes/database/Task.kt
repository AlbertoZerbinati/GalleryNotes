package com.esp.gallerynotes.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.esp.gallerynotes.utils.Priority
import java.util.*

@Entity(tableName = "tasks")
data class Task (
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name="id")
    val id: Long,

    @ColumnInfo(name="content")
    var content: String,

    @ColumnInfo(name="priority")
    var priority : Priority,

    @ColumnInfo(name="creation_date")
    var creationDate : Date,

    @ColumnInfo(name="is_done")
    var isDone : Boolean
)
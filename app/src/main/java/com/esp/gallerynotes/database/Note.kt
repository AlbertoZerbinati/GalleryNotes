package com.esp.gallerynotes.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

// Note database table
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name="id")
    val id: Int,

    @ColumnInfo(name="title")
    var title: String,

    @ColumnInfo(name="content")
    var content: String,

    @ColumnInfo(name="imageUri")
    var imageUri: String,

    ) : Serializable
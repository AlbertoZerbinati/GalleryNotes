package com.example.gallerynotes.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note (
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name="id")
    val id : Int,

    @ColumnInfo(name="title")
    var title : String,

    @ColumnInfo(name="content")
    val content : String,

//    @ColumnInfo(name="date")
//    val date : String,
//
//    @ColumnInfo(name="color")
//    val color : String,

//    @ColumnInfo(name="image_path")
//    val imagePath : String,


)
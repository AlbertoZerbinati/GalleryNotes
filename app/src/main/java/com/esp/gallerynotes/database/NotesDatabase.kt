package com.esp.gallerynotes.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

//database containing Note table
@Database(entities=[Note::class],version=1)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao //for accessing data

    //SINGLETON DESIGN PATTERN: unique instance of DB
    companion object {
        @Volatile
        private var INSTANCE: NotesDatabase? = null

        fun getDatabase(context: Context): NotesDatabase {
            return INSTANCE ?: synchronized(this) { //if INSTANCE is null we build it synchronously
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotesDatabase::class.java,
                    "notes_database"
                ).build()

                //TODO: populate the DB with an initial tutorial-note if it was null

                INSTANCE = instance
                instance
            }
        }
    }
}
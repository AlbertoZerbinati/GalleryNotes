package com.esp.gallerynotes.database

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.esp.gallerynotes.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.random.Random

// Database containing Note table
@Database(entities=[Note::class],version=1)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    // SINGLETON DESIGN PATTERN: unique instance of DB
    companion object {
        @Volatile
        private var INSTANCE: NotesDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope)
        : NotesDatabase {
            return INSTANCE ?: synchronized(this) { // If INSTANCE is null we build it synchronously
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotesDatabase::class.java,
                    "notes_database"
                ).addCallback(WordDatabaseCallback(scope, context)) // Add a callback
                 .build()

                INSTANCE = instance
                // return instance
                instance
            }
        }

        // Database prepopulation callback
        private class WordDatabaseCallback(
            private val scope: CoroutineScope,
            var context: Context
        ) : RoomDatabase.Callback() {
            // Override the onCreate method: populate the database only after app installation or storage clear
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.noteDao(), context)
                    }
                }
            }
        }

        // Populate the database in a new coroutine with a Welcoming Note
        fun populateDatabase(noteDao: NoteDao, context: Context) {
            // Get note image from res/raw, compress it, save to internal storage.
            val res : Resources = context.applicationContext.resources;
            val imageStream : InputStream = res.openRawResource(R.raw.completelogo)
            var imageBitmap = BitmapFactory.decodeStream(imageStream)
            val filename = "___helpnoteimage___"
            val baos = ByteArrayOutputStream()
            imageBitmap?.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            context.applicationContext.openFileOutput(filename, Context.MODE_PRIVATE).use {
                it.write(baos.toByteArray())
            }

            // Create the Welcoming note and insert it into the DB
            val note = Note(
                0,
                "Welcome to GalleryNotes❕",
                "Create and manage your Notes \uD83E\uDD29\n" +
                        "Add Pictures to make them memorable \uD83C\uDF07\n" +
                        "Share them with your Friends \uD83D\uDE0E\n" +
                        "Have Fun‼️",
                filename)
            noteDao.insertNote(note)
        }
    }
}
package com.esp.gallerynotes.database

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.core.content.FileProvider
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.esp.gallerynotes.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
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

            //TODO - Should be processed in another thread
            val imagesFolder = File(context.filesDir, "images")
            var uri: Uri? = null
            try {
                imagesFolder.mkdirs()
                val file = File(imagesFolder, "$filename.jpeg")
                val stream = FileOutputStream(file)
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                stream.flush()
                stream.close()
                uri = FileProvider.getUriForFile(
                    context,
                    "com.esp.fileprovider",
                    file
                )
            } catch (e: IOException) {
                Log.d(
                    "EXC",
                    "IOException while trying to write file for sharing: " + e.message
                )
            }

            // Create the Welcoming note and insert it into the DB
            val note = Note(
                0,
                "Welcome to GalleryNotes❕",
                "Create and manage your Notes \uD83E\uDD29\n" +
                        "Add Pictures to make them memorable \uD83C\uDF07\n" +
                        "Share them with your Friends \uD83D\uDE0E\n" +
                        "Have Fun‼️",
                uri.toString())
            noteDao.insertNote(note)
        }
    }
}
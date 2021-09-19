package com.esp.gallerynotes.database

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.esp.gallerynotes.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*

/*
 * Database containing Note table
 */
@Database(
    entities = [Note::class, Task::class],
    version = 3,
)
@TypeConverters(Converters::class)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun taskDao(): TaskDao

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
                    context.getString(R.string.notes_database)
                ).addCallback(NotesDatabaseCallback(scope, context)) // Add a callback
                    .addMigrations(MIGRATION_1_2)
                    .addMigrations(MIGRATION_2_3)
                    .build()

                INSTANCE = instance
                // Return instance
                instance
            }
        }

        // Database prepopulation callback
        private class NotesDatabaseCallback(
            private val scope: CoroutineScope,
            var context: Context
        ) : RoomDatabase.Callback() {
            // Override the onCreate method: populate the database only after app installation or app-storage clear
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.noteDao(), context)
                    }
                }
            }
        }

        // Migration adding the tasks table
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE tasks (id INTEGER NOT NULL, content TEXT NOT NULL, priority TEXT NOT NULL, creation_date INTEGER NOT NULL, is_done INTEGER NOT NULL, PRIMARY KEY(id), CHECK(priority IN ('HIGH', 'MEDIUM', 'LOW')));"
                )
            }
        }
        // Correct the previous migration
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DELETE FROM tasks")
                database.execSQL("DROP TABLE tasks")
                database.execSQL(
                    "CREATE TABLE tasks (id INTEGER NOT NULL, content TEXT NOT NULL, priority TEXT NOT NULL, creation_date INTEGER NOT NULL, is_done INTEGER NOT NULL, PRIMARY KEY(id), CHECK(priority IN ('HIGH', 'MEDIUM', 'LOW')));"
                )
            }
        }

        // Populate the database in a new coroutine with a Welcoming Note
        fun populateDatabase(noteDao: NoteDao, context: Context) {
            // Get note image from res/raw, compress it, save to internal storage.
            val res: Resources = context.applicationContext.resources
            val imageStream: InputStream = res.openRawResource(R.raw.completelogo)
            val imageBitmap = BitmapFactory.decodeStream(imageStream)
            val filename = context.getString(R.string.help_note_filename)

            val imagesFolder = File(context.filesDir, "images")
            val uri: Uri?

            imagesFolder.mkdirs()
            val file = File(imagesFolder, "$filename.jpeg")
            val stream = FileOutputStream(file)
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
            stream.flush()
            stream.close()
            uri = FileProvider.getUriForFile(
                context,
                "com.esp.fileprovider",
                file
            )

            // Create the Welcoming note and insert it into the DB
            val note = Note(
                0,
                context.getString(R.string.help_note_title),
                context.getString(R.string.help_note_content),
                uri.toString(),
                false
            )
            noteDao.insertNote(note)
        }
    }
}
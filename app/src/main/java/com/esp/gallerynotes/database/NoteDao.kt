package com.esp.gallerynotes.database

import androidx.lifecycle.LiveData
import androidx.room.*

// Contains Data-accessing methods (queries)
@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY id DESC") // DESC ORDER: later added notes appear first
    fun getAllNotes(): LiveData<List<Note>> // LiveData is used because it will notify any observer
                                            // when the data has changed

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNote(note:Note)

    @Delete
    fun deleteNote(note:Note)

}
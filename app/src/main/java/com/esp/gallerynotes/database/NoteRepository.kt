package com.esp.gallerynotes.database

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

// Give access to the data source
class NoteRepository(private val noteDao: NoteDao) {

    // Room executes all queries on a separate thread
    // Observed LiveData will notify the observer when the data has changed
    val allNotes: LiveData<List<Note>> = noteDao.getAllNotes()

    // Methods executed on a non-UI thread: this way we ensure that we're not doing any long
    // running operations on the main thread, blocking the UI
    @WorkerThread
    fun insert(note: Note) {
        noteDao.insertNote(note)
    }

    @WorkerThread
    fun delete(note: Note) {
        noteDao.deleteNote(note)
    }
}
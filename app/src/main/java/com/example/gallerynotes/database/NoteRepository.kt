package com.example.gallerynotes.database

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

//repository giving access to the data source
class NoteRepository(private val noteDao: NoteDao) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    val allNotes: LiveData<List<Note>> = noteDao.getAllNotes()

    // You must call these methods on a non-UI thread or your app will crash.
    // Like this, Room ensures that you're not doing any long running operations on the main
    // thread, blocking the UI.
    @WorkerThread
    fun insert(note: Note) {
        noteDao.insertNote(note)
    }

    @WorkerThread
    fun delete(note: Note) {
        noteDao.deleteNote(note)
    }
}
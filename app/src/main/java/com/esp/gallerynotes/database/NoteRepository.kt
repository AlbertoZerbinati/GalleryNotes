package com.esp.gallerynotes.database

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

/*
 * Give access to the data source
 */
class NoteRepository(private val noteDao: NoteDao, private val taskDao: TaskDao) {

    // Observed LiveData will notify the observer when the data has changed
    val allNotes: LiveData<List<Note>> = noteDao.getAllNotes()

    val allTasks: LiveData<List<Task>> = taskDao.getAllTasks()

    // Methods executed on a non-UI thread: this way we ensure that we're not doing any long
    // running operations on the main thread, blocking the UI
    @WorkerThread
    fun insertNote(note: Note) {
        noteDao.insertNote(note)
    }

    @WorkerThread
    fun deleteNote(note: Note) {
        noteDao.deleteNote(note)
    }

    @WorkerThread
    fun insertTask(task:Task) {
        taskDao.insertTask(task)
    }

    @WorkerThread
    fun updateTask(task:Task) {
        taskDao.updateTask(task)
    }

    @WorkerThread
    fun deleteTask(task:Task) {
        taskDao.deleteTask(task)
    }

    fun getTask(id: Int) : LiveData<Task> {
        return taskDao.getTask(id)
    }
}
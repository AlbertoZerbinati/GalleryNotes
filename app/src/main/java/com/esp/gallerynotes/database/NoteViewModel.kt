package com.esp.gallerynotes.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/*
 * ViewModel role is to provide data for the UI and survive configuration changes (ex. screen orientation, language, ...)
 */
class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NoteRepository

    // Using LiveData and caching what getAllNotes returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes
    // - Repository is completely separated from the UI through the ViewModel
    val allNotes: LiveData<List<Note>>
    val allTasks: LiveData<List<Task>>

    // Initialize the repository and the cached notes
    init {
        val notesDao = NotesDatabase.getDatabase(application, viewModelScope).noteDao()
        val tasksDao = NotesDatabase.getDatabase(application, viewModelScope).taskDao()
        repository = NoteRepository(notesDao,tasksDao)
        allNotes = repository.allNotes
        allTasks = repository.allTasks
    }

    // Launching coroutines to manipulate the data in a non-blocking way
    fun insert(note: Note) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertNote(note)
    }
    fun delete(note: Note) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteNote(note)
    }

    fun insertTask(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertTask(task)
    }
    fun getTask(id: Int) : LiveData<Task> = repository.getTask(id)
    fun updateTask(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateTask(task)
    }
    fun deleteTask(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteTask(task)
    }
}
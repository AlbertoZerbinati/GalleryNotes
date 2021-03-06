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

    // Initialize the repository and the cached notes
    init {
        val notesDao = NotesDatabase.getDatabase(application, viewModelScope).noteDao()
        repository = NoteRepository(notesDao)
        allNotes = repository.allNotes
    }

    // Launching coroutines to manipulate the data in a non-blocking way
    fun insert(note: Note) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(note)
    }
    fun delete(note: Note) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(note)
    }
}
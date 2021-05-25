package com.example.gallerynotes.utils

import com.example.gallerynotes.database.Note

interface NotesListener {
    fun onNoteClicked(note: Note, position : Int)
}
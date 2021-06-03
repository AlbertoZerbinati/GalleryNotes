package com.esp.gallerynotes.utils

import com.esp.gallerynotes.database.Note

interface NotesListener {
    fun onNoteClicked(note: Note, position : Int)
}
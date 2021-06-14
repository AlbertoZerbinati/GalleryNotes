package com.esp.gallerynotes.utils

import android.view.View
import com.esp.gallerynotes.database.Note

// Interface for dealing with note click and long-click on the list activity
interface NotesListener {
    fun onNoteClicked(note: Note)
    fun onNoteLongClicked(note: Note, v: View)
}
package com.example.gallerynotes.activities

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.gallerynotes.R
import com.example.gallerynotes.database.Note
import com.example.gallerynotes.database.NoteViewModel

class CreateOrUpdateNoteActivity : AppCompatActivity() {
    private val RC_UPDATE_NOTE : Int = 2

    private lateinit var noteTitle : EditText
    private lateinit var noteContent : EditText
    private lateinit var noteViewModel : NoteViewModel

    private var isUpdate : Boolean = false
    private lateinit var oldNote : Note

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_or_update_note)

        //view model for saving data in db
        noteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)

        //note components
        noteTitle = findViewById(R.id.note_title)
        noteContent = findViewById(R.id.note_content)

        //if it is an UPDATE call, then we need to obtain the old note parameters
        if (intent.getIntExtra("requestCode",-1) == RC_UPDATE_NOTE) {
            isUpdate = true
            oldNote = intent.getSerializableExtra("note") as Note

            noteTitle.setText(oldNote.title)
            noteContent.setText(oldNote.content)
        }

        //back button
        val backImage : ImageView = findViewById(R.id.back)
        backImage.setOnClickListener{
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        //save notes before terminating this activity and going back to MainActivity
        if(isUpdate) {
            updateNote()
        } else {
            createNote()
        }
        super.onBackPressed()
    }

    private fun createNote() {
        //get inputs
        val title = noteTitle.getText().toString().trim()
        val content = noteContent.getText().toString().trim()

        //if empty note than don't save it
        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, getString(R.string.empty_not_saved), Toast.LENGTH_SHORT).show()
            return
        }

        //otherwise create a new note with the inputs
        val id = 0
        val note = Note(id,title,content)
        //and save it into the db through the viewmodel
        noteViewModel.insert(note)
    }

    private fun updateNote() {
        //get inputs
        val title = noteTitle.getText().toString().trim()
        val content = noteContent.getText().toString().trim()

        //if empty then don't update the note, leave it as it was before
        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, getString(R.string.empty_not_saved), Toast.LENGTH_SHORT).show()
            return
        }

        //otherwise update the note with the new inputs: insert works as update because the DAO REPLACES on ID conflict
        val id : Int = oldNote.id
        val note = Note(id,title,content)
        noteViewModel.insert(note)
    }
}


package com.example.gallerynotes.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.gallerynotes.R
import com.example.gallerynotes.database.Note
import com.example.gallerynotes.database.NoteViewModel

class CreateNoteActivity : AppCompatActivity() {
    private lateinit var noteTitle : EditText
    private lateinit var noteContent : EditText
    private lateinit var myNoteViewModel : NoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_note)

        //view model for saving data in db
        myNoteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)

        //note components
        noteTitle = findViewById(R.id.note_title)
        noteContent = findViewById(R.id.note_content)

        //back button
        val backImage : ImageView = findViewById(R.id.back)
        backImage.setOnClickListener{
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        //save notes before terminating this activity and going back to MainActivity
        saveNote()
        super.onBackPressed()
    }

    private fun saveNote() {
        //get inputs
        val title = noteTitle.getText().toString().trim()
        val content = noteContent.getText().toString().trim()

        //if empty note than not save it
        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, getString(R.string.empty_not_saved), Toast.LENGTH_SHORT).show()
            return
        }

        //else create a new note with the inputs
        val note = Note(0,title,content)
        //and save it into the db through the viewmodel
        myNoteViewModel.insert(note)
//        Toast.makeText(this, "saved", Toast.LENGTH_SHORT).show()
    }
}

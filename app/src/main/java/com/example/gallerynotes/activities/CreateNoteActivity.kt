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

        val backImage : ImageView = findViewById(R.id.back)
        myNoteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)
        noteTitle = findViewById(R.id.note_title)
        noteContent = findViewById(R.id.note_content)

        backImage.setOnClickListener{
            //TODO: automatically save input text in DB (if not both empty)
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        saveNote()
        super.onBackPressed()
    }

    fun saveNote() {
        val title = noteTitle.getText().toString().trim()
        val content = noteContent.getText().toString().trim()
        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, getString(R.string.empty_not_saved), Toast.LENGTH_SHORT).show()
            return
        }
        Log.e("insert","ciao")

        val note = Note(0,title,content)
        myNoteViewModel.insert(note)
        Toast.makeText(this, "saved", Toast.LENGTH_SHORT).show()
    }
}

package com.example.gallerynotes.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.gallerynotes.R
import com.example.gallerynotes.database.NoteViewModel

class MainActivity : AppCompatActivity() {
//    private val RC_ADD_NOTE : Int= 1;
    private lateinit var noteViewModel: NoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.notesRV)
        val adapter = NotesAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)

        // Get a new or existing ViewModel from the ViewModelProvider.
        noteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)

        // Add an observer on the LiveData returned by getAlphabetizedWords.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.
        noteViewModel.allNotes.observe(
            this,
            Observer { notes ->
                // Update the cached copy of the words in the adapter.
                notes?.let { adapter.setNotes(it) }
            }
        )



        val addNoteImage : ImageView = findViewById(R.id.imageAddNote)
        addNoteImage.setOnClickListener { view ->
            val intent = Intent(view.context, CreateNoteActivity::class.java)
            startActivity(intent)
        }


    }
}
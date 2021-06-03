package com.esp.gallerynotes.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.esp.gallerynotes.R
import com.esp.gallerynotes.database.Note
import com.esp.gallerynotes.database.NoteViewModel
import com.esp.gallerynotes.utils.NotesAdapter
import com.esp.gallerynotes.utils.NotesListener
import java.io.Serializable

class MainActivity : AppCompatActivity(), NotesListener {
    private val RC_ADD_NOTE : Int = 1
    private val RC_UPDATE_NOTE : Int = 2
    private lateinit var noteViewModel: NoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //setup recycler view
        val recyclerView = findViewById<RecyclerView>(R.id.notesRV)
        val adapter = NotesAdapter(this, this) //pass 'this' as NotesListener
        recyclerView.adapter = adapter
        recyclerView.layoutManager = StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)

        // Get a new or existing ViewModel from the ViewModelProvider.
        noteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)

        // Add an observer on the LiveData returned by getAllNotes.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.
        noteViewModel.allNotes.observe(
            this,
            { notes ->
                // Update the cached copy of the words in the adapter.
                notes?.let { adapter.setNotes(it) }
            }
        )


        // floating action button: starts CreateNoteActivity
        val fab: View = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            val intent = Intent(view.context, CreateOrUpdateNoteActivity::class.java)
            intent.putExtra("requestCode", RC_ADD_NOTE)
            startActivity(intent)
        }
    }

    //when a note is clicked we need to start the update activity
    override fun onNoteClicked(note: Note, position: Int) {
//        Toast.makeText(this, "$position", Toast.LENGTH_SHORT).show()
        val intent = Intent(applicationContext, CreateOrUpdateNoteActivity::class.java)
        with(intent) {
            putExtra("requestCode", RC_UPDATE_NOTE)
            putExtra("note", note as Serializable)
            startActivity(this)
        }
    }
}
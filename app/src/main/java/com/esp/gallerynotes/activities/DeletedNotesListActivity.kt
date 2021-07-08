@file:Suppress("PrivatePropertyName")

package com.esp.gallerynotes.activities

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.esp.gallerynotes.R
import com.esp.gallerynotes.database.Note
import com.esp.gallerynotes.database.NoteViewModel
import com.esp.gallerynotes.utils.NotesAdapter
import com.esp.gallerynotes.utils.NotesListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import java.io.Serializable

/*
 * DeletedNotesListActivity shows all the deleted Notes in a RecyclerView.
 * Allows to reset a Note on long click
 */
class DeletedNotesListActivity : AppCompatActivity(), NotesListener {

    private lateinit var noteViewModel: NoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes_list)

        // Setup recycler view
        val recyclerView = findViewById<RecyclerView>(R.id.notesRV)
        val adapter = NotesAdapter(this, this) // pass this Activity as NotesListener
        recyclerView.adapter = adapter
        recyclerView.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        // Get a new or existing ViewModel from the ViewModelProvider
        noteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)

        // Add an observer on the LiveData returned by noteViewModel
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground
        noteViewModel.allNotes.observe(
            this,
            { notes ->
                // Update the cached copy of notes in the adapter
                notes?.let {
                    adapter.setNotes(it.filter {
                        it.deleted // Only show the not-deleted Notes
                    })
                }
            }
        )

        // Get rid of non used UI components: fab and nav-drawer
        val fab: FloatingActionButton = findViewById(R.id.fab)
        (fab.parent as ViewGroup).removeView(fab)

        val navView : NavigationView = findViewById(R.id.nav_view)
        (navView.parent as ViewGroup).removeView(navView)

        supportActionBar?.title = "Cancelled Notes"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    // Inflate ActionBar Menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    // When a note is clicked
    override fun onNoteClicked(note: Note) {
        // blank
    }

    // When a note is long clicked
    override fun onNoteLongClicked(note: Note, v: View) {
        Toast.makeText(this, "Resetting Note", Toast.LENGTH_SHORT).show()

        note.deleted = false
        noteViewModel.insert(note)
    }
}

@file:Suppress("PrivatePropertyName")

package com.esp.gallerynotes.activities

import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
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

/*
 * DeletedNotesListActivity shows all the deleted Notes in a RecyclerView.
 * Allows to reset o forever-delete a Note on long click
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
                notes?.let { it ->
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

        val popupMenu = PopupMenu(v.context, v)

        // Detect the chosen action
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                // Restore note
                R.id.context_menu_restore -> {
                    // Set the Note as not deleted
                    note.deleted = false
                    // And update the instance in the database
                    noteViewModel.insert(note)

                    true
                }
                // Delete note forever
                R.id.context_menu_delete_forever -> {
                    val alert: AlertDialog.Builder = AlertDialog.Builder(this)
                    alert.setTitle(getString(R.string.delete_note))
                    alert.setMessage(getString(R.string.confirm_delete))
                    alert.setPositiveButton(getString(R.string.yes)) { _, _ -> // Confirmed note deletion

                        // Delete the image from the internal storage
                        if (note.imageUri.isNotEmpty())
                            applicationContext.contentResolver.delete(Uri.parse(note.imageUri),null,null)

                        // Delete the note from the DB
                        noteViewModel.delete(note)
                    }
                    alert.setNegativeButton(getString(R.string.no)) { dialog, _ -> // Rejected note deletion
                        dialog.cancel()
                    }
                    alert.show()
                    true
                }
                else -> false
            }
        }
        popupMenu.inflate(R.menu.deleted_note_context_menu)
        popupMenu.show()

    }
}

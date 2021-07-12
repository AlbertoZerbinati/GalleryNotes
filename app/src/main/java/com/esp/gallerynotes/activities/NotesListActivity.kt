@file:Suppress("PrivatePropertyName")

package com.esp.gallerynotes.activities

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.esp.gallerynotes.R
import com.esp.gallerynotes.database.Note
import com.esp.gallerynotes.database.NoteViewModel
import com.esp.gallerynotes.utils.NotesAdapter
import com.esp.gallerynotes.utils.NotesListener
import com.google.android.material.navigation.NavigationView
import java.io.Serializable

/*
 * NotesListActivity shows all the Notes in a RecyclerView.
 * Allows to create new Notes through the FloatingActionButton
 * Allows to edit a Note by clicking on it
 * Allows to edit, share or delete a Note on long click
 */
class NotesListActivity : AppCompatActivity(), NotesListener, NavigationView.OnNavigationItemSelectedListener {
    // Request codes
    private val RC_ADD_NOTE: Int = 1
    private val RC_UPDATE_NOTE: Int = 2

    private lateinit var noteViewModel: NoteViewModel

    private lateinit var drawerLayout : DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes_list)

        // Setup navigation drawer
        drawerLayout = findViewById(R.id.drawer)
        val navView : NavigationView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener(this)
        val abdToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)

        drawerLayout.addDrawerListener(abdToggle)
        abdToggle.isDrawerIndicatorEnabled = true
        abdToggle.isDrawerSlideAnimationEnabled = true
        abdToggle.syncState()

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
                    adapter.setNotes(it.filter { note ->
                        !note.deleted // Only show the not-deleted Notes
                    })
                }
            }
        )

        // Floating action button: starts NoteDetailActivity for Note creation
        val fab: View = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            val intent = Intent(view.context, NoteDetailActivity::class.java)
            intent.putExtra("requestCode", RC_ADD_NOTE)
            startActivity(intent)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                if(!drawerLayout.isDrawerOpen(GravityCompat.START))
                    drawerLayout.openDrawer(GravityCompat.START)
                else
                    drawerLayout.closeDrawer(GravityCompat.START)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // When a note is clicked start NoteDetailActivity for Note Update
    override fun onNoteClicked(note: Note) {
        val intent = Intent(applicationContext, NoteDetailActivity::class.java)
        with(intent) {
            putExtra("requestCode", RC_UPDATE_NOTE)
            putExtra("note", note as Serializable)
            startActivity(this)
        }
    }

    // When a note is long clicked show the PopupMenu
    override fun onNoteLongClicked(note: Note, v: View) {
        val popupMenu = PopupMenu(v.context, v)

        // Detect the chosen action
        popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    // Edit note: start NoteDetailActivity for Note Update
                    R.id.context_menu_edit -> {
                        val intent = Intent(applicationContext, NoteDetailActivity::class.java)
                        with(intent) {
                            putExtra("requestCode", RC_UPDATE_NOTE)
                            putExtra("note", note as Serializable)
                            startActivity(this)
                        }
                        true
                    }

                    // Share note: start share intent
                    R.id.context_menu_share -> {
                        // If empty content there is nothing to share
                        if (note.content.isEmpty()) {
                            Toast.makeText(
                                this,
                                getString(R.string.no_content_to_share),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        // Otherwise start a SEND intent with note.content as text
                        // and note.title as title. Also eventually add image.
                        else {
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TITLE, note.title)
                                putExtra(Intent.EXTRA_TEXT, note.content)
                                type = "text/plain" // Default text/plain SEND_INTENT
                            }
                            if (note.imageUri.isNotEmpty()) {
                                sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(note.imageUri))
                                sendIntent.clipData =
                                    ClipData.newRawUri("image", Uri.parse(note.imageUri))
                                sendIntent.type = "image/jpeg"  // Becomes an image/jpeg SEND_INTENT
                            }
                            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grant image read permission

                            val shareIntent = Intent.createChooser(sendIntent, note.title)
                            startActivity(shareIntent)
                        }
                        true
                    }
                    // Delete note: ask confirmation before deleting from DB
                    R.id.context_menu_delete -> {
                        /*
                        val alert: AlertDialog.Builder = AlertDialog.Builder(this)
                        alert.setTitle(getString(R.string.delete_note))
                        alert.setMessage(getString(R.string.confirm_delete))
                        alert.setPositiveButton(getString(R.string.yes)) { _, _ -> // Confirmed note deletion


                            // Delete the image from the internal storage
                            if (note.imageUri.isNotEmpty())
                                applicationContext.contentResolver.delete(Uri.parse(note.imageUri),null,null)
                            // Delete the note from the DB
                            noteViewModel.delete(note)
                        */

                        // Set the Note as deleted
                        note.deleted = true
                        // And update the instance in the database
                        noteViewModel.insert(note)
                        Toast.makeText(this, "Note moved to the Bin", Toast.LENGTH_SHORT).show()
                        /*
                        }
                        alert.setNegativeButton(getString(R.string.no)) { dialog, _ -> // Rejected note deletion
                            dialog.cancel()
                        }
                        alert.show()

                         */
                        true
                    }
                    else -> false
                }
            }
        popupMenu.inflate(R.menu.note_context_menu)
        popupMenu.show()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.bin -> {
                val intent = Intent(applicationContext, DeletedNotesListActivity::class.java)
                with(intent) {
                    startActivity(this)
                }
            }
        }
        return false
    }
}

package com.esp.gallerynotes.utils

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.esp.gallerynotes.R
import com.esp.gallerynotes.database.Note

class NotesAdapter(var context: Context, notesListenerParam: NotesListener) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val notesListener : NotesListener = notesListenerParam // Reference to an activity able to manage click and long click on a note (NotesListActivity)

    private var notes = emptyList<Note>() // Cached copy of notes

    // Define a holder
    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container : View = itemView.findViewById(R.id.container_note_layout)
        val noteTitleView: TextView = itemView.findViewById(R.id.container_note_title)
        val noteContentView: TextView = itemView.findViewById(R.id.container_note_content)
        val noteImageView: ImageView = itemView.findViewById(R.id.container_note_image)
    }

    // Create a new holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView = inflater.inflate(R.layout.note_container, parent, false)
        return NoteViewHolder(itemView)
    }

    // Bind and existing holder with a particular Note to display
    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val current = notes[position]

        holder.noteTitleView.visibility = View.GONE
        holder.noteContentView.visibility = View.GONE
        holder.noteImageView.visibility = View.GONE

        // Title
        if(current.title.isNotBlank()) {
            holder.noteTitleView.text = current.title
            holder.noteTitleView.visibility = View.VISIBLE
        }
        // Content
        if(current.content.isNotBlank()) {
            holder.noteContentView.text = current.content
            holder.noteContentView.visibility = View.VISIBLE
        }
        // Image
        if(current.imageUri.isNotBlank()) {
            holder.noteImageView.setImageURI(Uri.parse(current.imageUri))
            holder.noteImageView.visibility = View.VISIBLE
        }

        // Set the click listener: call the appropriate method of the listener
        holder.container.setOnClickListener {
            notesListener.onNoteClicked(notes[position])
        }

        // Set the long click listener: display popup menu
        holder.container.setOnLongClickListener {
            notesListener.onNoteLongClicked(notes[position], holder.container)
            true
        }
    }

    // Set notes: will be triggered through OBSERVER DESIGN PATTERN
    internal fun setNotes(notes: List<Note>) {
        this.notes = notes
        notifyDataSetChanged()
    }

    // Number of notes
    override fun getItemCount() = notes.size
}

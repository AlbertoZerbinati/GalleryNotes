package com.example.gallerynotes.activities

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gallerynotes.R
import com.example.gallerynotes.database.Note

class NotesAdapter(context: Context) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var notes = emptyList<Note>() // Cached copy of notes

    //defines a holder
    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noteTitleView: TextView = itemView.findViewById(R.id.container_note_title)
        val noteContentView: TextView = itemView.findViewById(R.id.container_note_content)
    }

    //create a new holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView = inflater.inflate(R.layout.note_container, parent, false)
        return NoteViewHolder(itemView)
    }

    //bind and existing holder with a particular Note data
    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val current = notes[position]
        holder.noteTitleView.text = current.title
        holder.noteContentView.text = current.content
    }

    //sets the notes: will be triggered through OBSERVER DESIGN PATTERN
    internal fun setNotes(notes: List<Note>) {
        this.notes = notes
        notifyDataSetChanged()
    }

    //# of notes
    override fun getItemCount() = notes.size
}

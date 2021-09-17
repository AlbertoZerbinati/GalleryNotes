package com.esp.gallerynotes.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.esp.gallerynotes.R
import com.esp.gallerynotes.database.Task

/*
 * Adapter for Tasks RecyclerView
 */
class TasksAdapter(var context: Context) : RecyclerView.Adapter<TasksAdapter.TaskViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var tasks = emptyList<Task>() // Cached copy of tasks

    // Define a holder
    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var radioButton: AppCompatRadioButton = itemView.findViewById(R.id.task_radio_button)
        var content: AppCompatTextView = itemView.findViewById(R.id.task_content)
    }

    // Create a new holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView = inflater.inflate(R.layout.task_container, parent, false)
        return TaskViewHolder(itemView)
    }

    // Bind and existing holder with a particular Task to display
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task : Task = tasks[position]

        holder.radioButton.isChecked = task.isDone
        holder.content.text = task.content
    }

    // Set notes: will be triggered through OBSERVER DESIGN PATTERN
    internal fun setTasks(tasks: List<Task>) {
        this.tasks = tasks
        notifyDataSetChanged()
    }

    // Number of notes
    override fun getItemCount() = tasks.size
}

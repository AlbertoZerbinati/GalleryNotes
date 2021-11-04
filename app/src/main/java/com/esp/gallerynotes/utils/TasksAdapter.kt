package com.esp.gallerynotes.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.esp.gallerynotes.R
import com.esp.gallerynotes.database.Task

/*
 * Adapter for Tasks RecyclerView
 */
class TasksAdapter(var context: Context, var tasksListener: TasksListener) :
    RecyclerView.Adapter<TasksAdapter.TaskViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    var tasks = emptyList<Task>() // Cached copy of tasks

    // Define a holder
    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var container: ConstraintLayout = itemView.findViewById(R.id.task_container)
        var radioButton: AppCompatCheckBox = itemView.findViewById(R.id.task_radio_button)
        var content: AppCompatTextView = itemView.findViewById(R.id.task_content)
    }

    // Create a new holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView = inflater.inflate(R.layout.task_container, parent, false)
        return TaskViewHolder(itemView)
    }

    // Bind and existing holder with a particular Task to display
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task: Task = tasks[position]

        // Check box
        holder.radioButton.isChecked = task.isDone
        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_enabled)
            ), when (task.priority) {
                Priority.HIGH -> intArrayOf(
                    Color.parseColor("#F15859") // enabled
                )
                Priority.MEDIUM -> intArrayOf(
                    Color.parseColor("#FFA405")
                )
                else -> intArrayOf(
                    Color.parseColor("#3ABA56")
                )
            }

        )
        holder.radioButton.buttonTintList = colorStateList

        // Text
        holder.content.text = task.content

        // Strike through the text if the task is done
        if(task.isDone)
            holder.content.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        else
            holder.content.paintFlags = 0


        // Listener modify
        holder.container.setOnClickListener {
            tasksListener.onTaskClicked(task)
        }
        // Listener CheckBox
        holder.radioButton.setOnClickListener {
            tasksListener.onTaskRadioButtonClicked(task, holder.radioButton.isChecked)
        }


    }

    // Set notes: will be triggered through OBSERVER DESIGN PATTERN
    internal fun setTasks(tasks: List<Task>) {
        this.tasks = tasks
        notifyDataSetChanged()
    }

    // Number of notes
    override fun getItemCount() = tasks.size
}

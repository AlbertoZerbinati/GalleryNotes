package com.esp.gallerynotes.utils

import android.view.View
import com.esp.gallerynotes.database.Note
import com.esp.gallerynotes.database.Task

/*
 * Interface for dealing with task click and long-click on the list activity
 */
interface TasksListener {
    fun onTaskClicked(task: Task)
    fun onTaskLongClicked(task: Task, v: View)
    fun onTaskRadioButtonClicked(task: Task, isChecked: Boolean)
}
package com.esp.gallerynotes.utils

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.esp.gallerynotes.database.NoteViewModel

class RecyclerItemTouchHelper(dragDirs: Int,
                              swipeDirs: Int,
                              owner: ViewModelStoreOwner,
                              private val adapter: TasksAdapter
) : ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs,) {
    private val taskViewModel = ViewModelProvider(owner).get(NoteViewModel::class.java)

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        taskViewModel.deleteTask(adapter.tasks[viewHolder.absoluteAdapterPosition])
        adapter.notifyItemRemoved(viewHolder.absoluteAdapterPosition)
    }

}
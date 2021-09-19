package com.esp.gallerynotes.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.esp.gallerynotes.R
import com.esp.gallerynotes.database.NoteViewModel
import com.esp.gallerynotes.database.Task
import com.esp.gallerynotes.utils.Priority
import com.esp.gallerynotes.utils.SharedTaskViewModel
import com.esp.gallerynotes.utils.TasksAdapter
import com.esp.gallerynotes.utils.TasksListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior

class TodoActivity : AppCompatActivity(), TasksListener {

    private lateinit var bottomNavView: BottomNavigationView
    private lateinit var taskViewModel: NoteViewModel
    private lateinit var bottomSheetFragment: TodoBottomSheetFragment
    private lateinit var adapter : TasksAdapter

    private lateinit var sharedTaskViewModel: SharedTaskViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo)

        // Setup bottom nav view
        bottomNavView = findViewById(R.id.bottomNav)
        bottomNavView.background = null
        bottomNavView.setSelectedItemId(R.id.todos);
        bottomNavView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.notes -> {
                    val intent = Intent(applicationContext, NotesListActivity::class.java)
                    with(intent) {
                        startActivity(this)
                    }
                    finish()
                    true
                }
                else -> false
            }
        }
        bottomNavView.setOnNavigationItemReselectedListener { true }

        // Setup bottom sheet fragment (hidden default behavior)
        bottomSheetFragment = TodoBottomSheetFragment()
        val bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottomSheet))
        bottomSheetBehavior.peekHeight = BottomSheetBehavior.STATE_HIDDEN

        // Setup shared ViewModel
        sharedTaskViewModel = ViewModelProvider(this).get(SharedTaskViewModel::class.java)

        // Setup recycler view
        val recyclerView = findViewById<RecyclerView>(R.id.tasksRV)
        adapter = TasksAdapter(this, this) // pass this Activity as TasksListener
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Get a new or existing ViewModel from the ViewModelProvider
        taskViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)

        // Add an observer on the LiveData returned by noteViewModel
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground
        taskViewModel.allTasks.observe(this, { tasks ->
            adapter.setTasks(tasks)
        })

        // Floating action button: show the bottom sheet
        val fab: View = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            sharedTaskViewModel.selectedTask.value = null
            showBottomSheetFragment()
        }

        Log.d("TAG", "onCreate: ${Priority.HIGH.name}")
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.title = "GalleryNotes - Tasks"
    }

    private fun showBottomSheetFragment() {
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
    }

    override fun onTaskClicked(task: Task) {
        sharedTaskViewModel.selectedTask.value = task
        showBottomSheetFragment()
    }

    override fun onTaskLongClicked(task: Task, v: View) {
        TODO("Not yet implemented")
    }

    override fun onTaskRadioButtonClicked(task: Task, isChecked: Boolean) {
        val newTask = Task(task.id, task.content, task.priority, task.creationDate, isChecked)
        taskViewModel.updateTask(newTask)

        adapter.notifyDataSetChanged()
    }
}
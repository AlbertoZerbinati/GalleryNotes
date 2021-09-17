package com.esp.gallerynotes.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.esp.gallerynotes.R
import com.esp.gallerynotes.database.NoteViewModel
import com.esp.gallerynotes.database.Task
import com.esp.gallerynotes.utils.NotesAdapter
import com.esp.gallerynotes.utils.Priority
import com.esp.gallerynotes.utils.TasksAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.util.*

class TodoActivity : AppCompatActivity() {

    private lateinit var bottomNavView: BottomNavigationView
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var bottomSheetFragment: TodoBottomSheetFragment

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
        val bottomSheetBehaviour = BottomSheetBehavior.from(findViewById(R.id.bottomSheet))
        bottomSheetBehaviour.peekHeight = BottomSheetBehavior.STATE_HIDDEN

        // Setup recycler view
        val recyclerView = findViewById<RecyclerView>(R.id.tasksRV)
        val adapter = TasksAdapter(this) // pass this Activity as NotesListener
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Get a new or existing ViewModel from the ViewModelProvider
        noteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)

        // Add an observer on the LiveData returned by noteViewModel
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground
        noteViewModel.allTasks.observe(this, { tasks ->
            adapter.setTasks(tasks)
//            for (task in tasks) {
//                Log.e("AAAAAAAA", task.content)
//            }
        })

        // Floating action button: show the bottom sheet
        val fab: View = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
//            val task: Task = Task(0, "TodoTodoTodoTodoTodoTodoTodoTodoTodoTodoTodoTodoTodoTodoTodoTodoTodo", Priority.HIGH, Calendar.getInstance().getTime(), false)
//            noteViewModel.insertTask(task)
//
//            val retrieved = noteViewModel.getTask(1)
//            Log.e("AAA", "${retrieved?.observe(this, { it?.content })}")
            showBottomSheetFragment()
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.title = "GalleryNotes - Tasks"


    }

    private fun showBottomSheetFragment() {
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
    }
}
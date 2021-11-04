package com.esp.gallerynotes.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.esp.gallerynotes.R
import com.esp.gallerynotes.database.NoteViewModel
import com.esp.gallerynotes.database.Task
import com.esp.gallerynotes.utils.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar

class TodoActivity : AppCompatActivity(), TasksListener,
    NavigationView.OnNavigationItemSelectedListener {

    private lateinit var bottomNavView: BottomNavigationView
    private lateinit var taskViewModel: NoteViewModel
    private lateinit var bottomSheetFragment: TodoBottomSheetFragment
    private lateinit var adapter : TasksAdapter

    private lateinit var sharedTaskViewModel: SharedTaskViewModel

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo)

        // Setup navigation drawer
        drawerLayout = findViewById(R.id.drawer)
        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener(this)
        val abdToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)

        drawerLayout.addDrawerListener(abdToggle)
        abdToggle.isDrawerIndicatorEnabled = true
        abdToggle.isDrawerSlideAnimationEnabled = true
        abdToggle.syncState()

        // Setup bottom nav view
        bottomNavView = findViewById(R.id.bottomNav)
        bottomNavView.background = null
        bottomNavView.selectedItemId = R.id.todos;
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
        // Swipe to delete with itemtouchhelper
        ItemTouchHelper(RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT,this, adapter)).attachToRecyclerView(recyclerView)

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

        supportActionBar?.title = "GalleryNotes - Tasks"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    // Inflate ActionBar Menu (add_image, share_note, delete_note buttons)
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.todo_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // When an ActionBar button is selected
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Detect the chosen action
        when(item.itemId) {
            android.R.id.home -> {
                if (!drawerLayout.isDrawerOpen(GravityCompat.START))
                    drawerLayout.openDrawer(GravityCompat.START)
                else
                    drawerLayout.closeDrawer(GravityCompat.START)
            }

        }
        return super.onOptionsItemSelected(item)
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

    // overloads method both for the navigation drawer
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.notes -> {
                val intent = Intent(applicationContext, NotesListActivity::class.java)
                with(intent) {
                    startActivity(this)
                }
                finish()
            }
            R.id.tasks -> {
                val intent = Intent(applicationContext, TodoActivity::class.java)
                with(intent) {
                    startActivity(this)
                }
                finish()
            }
            R.id.settings -> {
//                val intent = Intent(applicationContext, Settings::class.java)
//                with(intent) {
//                    startActivity(this)
//                }
                Snackbar.make(findViewById(R.id.drawer),"Not implemented yet (theme & main)...", Snackbar.LENGTH_SHORT).show()
            }
            R.id.notes_bin -> {
                val intent = Intent(applicationContext, DeletedNotesListActivity::class.java)
                with(intent) {
                    startActivity(this)
                }
            }
        }
        return false
    }
}
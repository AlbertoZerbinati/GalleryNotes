package com.esp.gallerynotes.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.esp.gallerynotes.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class TodoActivity : AppCompatActivity() {

    private lateinit var bottomNavView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo)

        // Setup bottom nav view
        bottomNavView = findViewById(R.id.bottomNav)
        bottomNavView.background = null
        bottomNavView.setSelectedItemId(R.id.todos);
        bottomNavView.setOnNavigationItemSelectedListener { item ->
            when(item.itemId) {
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
        bottomNavView.setOnNavigationItemReselectedListener{true}
    }
}
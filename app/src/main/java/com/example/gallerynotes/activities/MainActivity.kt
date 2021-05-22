package com.example.gallerynotes.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.example.gallerynotes.R

class MainActivity : AppCompatActivity() {
    private val RC_ADD_NOTE : Int= 1;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val addNoteImage : ImageView = findViewById(R.id.imageAddNote)
        addNoteImage.setOnClickListener { view ->
            val intent = Intent(view.context, CreateNoteActivity::class.java)
            startActivity(intent)
        }
    }
}
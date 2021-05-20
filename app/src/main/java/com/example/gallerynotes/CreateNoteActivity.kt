package com.example.gallerynotes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView

class CreateNoteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_note)

        val backImage : ImageView = findViewById(R.id.back)
        backImage.setOnClickListener{
            //TODO: automatically save input text in DB (if not both empty)
            finish()
        }
    }
}
package com.esp.gallerynotes.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.esp.gallerynotes.R
import com.esp.gallerynotes.database.Note
import com.esp.gallerynotes.database.NoteViewModel
import java.io.ByteArrayOutputStream
import java.io.InputStream


class CreateOrUpdateNoteActivity : AppCompatActivity() {
    private val RC_UPDATE_NOTE : Int = 2

    private lateinit var noteTitle : EditText
    private lateinit var noteContent : EditText
    private lateinit var noteImage : ImageView

    private lateinit var addImageButton : ImageView
    private lateinit var deleteImageButton : ImageView
    private lateinit var noteViewModel : NoteViewModel

    private var isUpdate : Boolean = false
    private lateinit var oldNote : Note

    private var imagePath : String = ""

    companion object {
        var imageFileNumber : Int = 0
    }

    //this is the new way of receiving an activity result
    private var pickImageLauncherResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        // There are no request codes
        //if everything is ok, than we can display the selected image and save its path in the ImagePath variable to be saved later
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            if (data != null) {
                val selectedImageUri: Uri? = data.data
                if (selectedImageUri != null) {
                    val inputStream: InputStream? =
                        contentResolver.openInputStream(selectedImageUri)
                    if (inputStream != null) {
                        var imageBitmap = BitmapFactory.decodeStream(inputStream)
                        if(imageBitmap != null) {
                            // write to internal storage
                            val filename = "${selectedImageUri.toString().replace('/','_',true)}"
                            imagePath = filename

                            val baos = ByteArrayOutputStream()
                            imageBitmap?.compress(Bitmap.CompressFormat.JPEG, 70, baos)
                            applicationContext.openFileOutput(filename, Context.MODE_PRIVATE).use {
                                it.write(baos.toByteArray())
                            }

                            // attenzione sto mostrando a full quality, mentre quando riapro la vedrò compressa jpeg 70%
                            noteImage.setImageBitmap(imageBitmap)

//                            val preferences = getPreferences(MODE_PRIVATE)
//                            val editor = preferences.edit()
//
//                            // Store image in the persistent state: THE KEY IS THE PATH AND I'M SAVING IT IN THE DB
//                            editor.putString(imagePath, encodeBase64(imageBitmap))
//
//                            // Commit to storage synchronously
//                            editor.apply()

                            noteImage.visibility = View.VISIBLE
                            deleteImageButton.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

//    private fun encodeBase64(image: Bitmap?) : String {
//        val baos = ByteArrayOutputStream()
//        image?.compress(Bitmap.CompressFormat.JPEG, 50, baos)
//
//        val b = baos.toByteArray()
//        val imageEncoded: String = Base64.encodeToString(b, Base64.DEFAULT)
//
//        Log.d("Image Log:", imageEncoded)
//        return imageEncoded
//    }
//
//    private fun decodeBase64(input: String?): Bitmap? {
//        val decodedByte: ByteArray = Base64.decode(input, 0)
//        return BitmapFactory
//            .decodeByteArray(decodedByte, 0, decodedByte.size)
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_or_update_note)
        Log.e("AAA", "create $isUpdate")

        //view model for saving data in db
        noteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)

        //note components
        noteTitle = findViewById(R.id.note_title)
        noteContent = findViewById(R.id.note_content)
        noteImage = findViewById(R.id.note_image)
//        noteImage.visibility = View.GONE


        deleteImageButton = findViewById(R.id.delete_image)
        deleteImageButton.setOnClickListener {
            deleteImage()
        }


        //if it is an UPDATE call, then we need to obtain the old note parameters
        if (intent.getIntExtra("requestCode",-1) == RC_UPDATE_NOTE) {
            isUpdate = true
        }
        // for both UPDATE (always) and CREATE (only on screen-rotation
        if (intent.hasExtra("note")) {
            oldNote = intent.getSerializableExtra("note") as Note

            noteTitle.setText(oldNote.title)
            noteContent.setText(oldNote.content)

            if (oldNote.imagePath.isNotBlank()) {
                Log.e("AAA", "ENTRO QUI???")
                //                noteImage.setImageBitmap(BitmapFactory.decodeFile(oldNote.imagePath))
                //                noteImage.visibility = View.VISIBLE
                imagePath = oldNote.imagePath

                //                val preferences = getPreferences(MODE_PRIVATE)
                //                noteImage.setImageBitmap(decodeBase64(preferences.getString(imagePath, "")))


                noteImage.setImageBitmap(
                    BitmapFactory.decodeStream(
                        applicationContext.openFileInput(
                            imagePath
                        )
                    )
                )

                noteImage.visibility = View.VISIBLE
                deleteImageButton.visibility = View.VISIBLE
            }
        }

        //manage add image button
        addImageButton = findViewById(R.id.add_image)
        addImageButton.setOnClickListener {
            selectImage()
        }



        //back button
        val backImage : ImageView = findViewById(R.id.back)
        backImage.setOnClickListener{
            onBackPressed()
        }
    }

    private fun deleteImage() {
        Log.e("AAA", "DELETE")
        // set image path=""
        imagePath = ""
        // set image visibility to GONE
        noteImage.visibility = View.GONE
        deleteImageButton.visibility = View.GONE
        // delete image from internal storage
//        val file = File(applicationContext.filesDir, imagePath)
        applicationContext.deleteFile(imagePath)


    }

    override fun onPause() {
        Log.e("AAA", "pause")
        // manage the screen rotation

        if (isUpdate) {
            // change the intent from which the activity will build oldNote

            intent.removeExtra("note")
            intent.putExtra("note", Note(oldNote.id, noteTitle.text.toString().trim(), noteContent.text.toString().trim(), imagePath))

            //updateNote()


        } else {
            // in this case we have to put the created note inside the intent, so that we find it
            // as we reCreate the activity
            intent.putExtra("note", Note(0, noteTitle.text.toString().trim(), noteContent.text.toString().trim(), imagePath))


            //createNote()
        }

        super.onPause()
    }




    override fun onBackPressed() {
        //save notes before terminating this activity and going back to MainActivity
        if(isUpdate) {
            updateNote()
        } else {
            createNote()
        }
        super.onBackPressed()
    }

    //starts pick image activity
    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        pickImageLauncherResult.launch(intent)
    }


    private fun createNote() {
        //get inputs
        val title = noteTitle.getText().toString().trim()
        val content = noteContent.getText().toString().trim()

        //if empty note than don't save it
        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, getString(R.string.empty_not_saved), Toast.LENGTH_SHORT).show()
            return
        }

        //otherwise create a new note with the inputs
        val id = 0
        val note = Note(id,title,content,imagePath)
        //and save it into the db through the viewmodel
        noteViewModel.insert(note)
    }

    private fun updateNote() {
        //get inputs
        val title = noteTitle.getText().toString().trim()
        val content = noteContent.getText().toString().trim()

        //if empty then don't update the note, leave it as it was before
        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, getString(R.string.empty_not_saved), Toast.LENGTH_SHORT).show()
            return
        }

        //otherwise update the note with the new inputs: insert works as update because the DAO REPLACES on ID conflict
        val id : Int = oldNote.id
        val note = Note(id,title,content,imagePath)

        noteViewModel.insert(note)
    }
}


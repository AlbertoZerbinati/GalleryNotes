package com.esp.gallerynotes.activities

import android.app.Activity
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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.esp.gallerynotes.R
import com.esp.gallerynotes.database.Note
import com.esp.gallerynotes.database.NoteViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import kotlin.random.Random


class NoteDetailActivity : AppCompatActivity() {
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

    // receive ACTION_PICK activity result
    private var pickImageLauncherResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        // There are no request codes
        // if everything is ok, than we can display the selected image and save its path in the imagePath variable to be saved later
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            if (data != null) {
                val selectedImageUri: Uri? = data.data
                if (selectedImageUri != null) {
                    val inputStream: InputStream? =
                        contentResolver.openInputStream(selectedImageUri)
                    if (inputStream != null) {
                        var imageBitmap = BitmapFactory.decodeStream(inputStream)
                        imageBitmap = Bitmap.createScaledBitmap(imageBitmap, imageBitmap.width/4, imageBitmap.height/4, false)

                        if(imageBitmap != null) {
                            // if there was an old image displayed (and saved as File in internal storage)
                            // we need to delete it before assigning a new one
                            if (noteImage.drawable != null) {
                                deleteImage()
                            }

                            // generate filename
                            val generator = Random
                            val randomStringBuilder = StringBuilder()
                            var tempChar: Char
                            for (i in 0 until 20) {
                                tempChar = (generator.nextInt(26) + 97).toChar()
                                if (tempChar != '/')
                                    randomStringBuilder.append(tempChar)
                            }
                            val filename = randomStringBuilder.toString()

                            //TODO - Should be processed in another thread
                            val imagesFolder = File(filesDir, "images")
                            var uri: Uri? = null
                            try {
                                imagesFolder.mkdirs()
                                val file = File(imagesFolder, "$filename.jpeg")
                                val stream = FileOutputStream(file)
                                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                                stream.flush()
                                stream.close()
                                uri = FileProvider.getUriForFile(
                                    this,
                                    "com.esp.fileprovider",
                                    file
                                )
                            } catch (e: IOException) {
                                Log.d(
                                    "EXC",
                                    "IOException while trying to write file for sharing: " + e.message
                                )
                            }

                            // set imagePath for database
                            imagePath = uri.toString()
                            // show compressed image
                            noteImage.setImageURI(uri)
                            //
                            noteImage.visibility = View.VISIBLE
                            deleteImageButton.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_detail)

        Log.e("AAA",applicationContext.fileList().size.toString())

        // view model for saving data in db
        noteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)

        // note componentsContext
        noteTitle = findViewById(R.id.note_title)
        noteContent = findViewById(R.id.note_content)
        noteImage = findViewById(R.id.note_image)

        // delete image button
        deleteImageButton = findViewById(R.id.delete_image)
        deleteImageButton.setOnClickListener {
            deleteImage()
        }

        // if it is an UPDATE call we take note of that
        if (intent.getIntExtra("requestCode",-1) == RC_UPDATE_NOTE) {
            isUpdate = true
        }
        // this will be true both for UPDATE (always) and CREATE (only on screen-rotation)
        if (intent.hasExtra("note")) {
            // get the old note parameters
            oldNote = intent.getSerializableExtra("note") as Note

            noteTitle.setText(oldNote.title)
            noteContent.setText(oldNote.content)

            if (oldNote.imagePath.isNotBlank()) {
                //
                imagePath = oldNote.imagePath
                //
                noteImage.setImageURI(Uri.parse(imagePath))

                //
                noteImage.visibility = View.VISIBLE
                deleteImageButton.visibility = View.VISIBLE
            }
        }

        // back button
        val backButton : ImageView = findViewById(R.id.back)
        backButton.setOnClickListener{
            onBackPressed()
        }

        // add image button
        addImageButton = findViewById(R.id.add_image)
        addImageButton.setOnClickListener {
            selectImage()
        }

        // share note button
        val shareButton : ImageView = findViewById(R.id.share)
        shareButton.setOnClickListener{
            shareNote()
        }

        // delete note button WITH CONFIRMATION
        val deleteButton : ImageView = findViewById(R.id.delete)
        deleteButton.setOnClickListener{
            val alert: AlertDialog.Builder = AlertDialog.Builder(this)
            alert.setTitle("Delete Note")
            alert.setMessage("Are you sure you want to delete the Note?")
            alert.setPositiveButton("Yes") { _, _ -> // confirmed note deletion
                deleteNote()
            }
            alert.setNegativeButton("No") { dialog, _ -> // close dialog
                dialog.cancel()
            }
            alert.show()
        }
    }

    // Manage screen rotation by updating the Note data in the intent this activity will read after
    // eventual Destruction and Creation
    override fun onPause() {
        if (isUpdate) {
            // in this case we change the intent-extra from which the activity will build oldNote
            intent.removeExtra("note")
            intent.putExtra(
                "note",
                Note(
                    oldNote.id,
                    noteTitle.text.toString().trim(),
                    noteContent.text.toString().trim(),
                    imagePath
                )
            )
        } else {
            // in this case we have to put the created note inside the intent, so that we find it
            // as we reCreate the activity
            intent.putExtra(
                "note",
                Note(
                    0,
                    noteTitle.text.toString().trim(),
                    noteContent.text.toString().trim(),
                    imagePath
                )
            )
        }
        super.onPause()
    }

    // if back arrow or back button pressed
    override fun onBackPressed() {
        // save notes before terminating this activity and going back to MainActivity
        if(isUpdate)
            updateNote()
        else
            createNote()

        super.onBackPressed()
    }

    //starts pick image activity
    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        pickImageLauncherResult.launch(intent)
    }

    private fun deleteImage() {
        // set image visibility to GONE
        noteImage.visibility = View.GONE
        deleteImageButton.visibility = View.GONE
        // delete image from internal storage
        applicationContext.contentResolver.delete(Uri.parse(imagePath),null,null)
        // set image path=""
        imagePath = ""
    }

    private fun createNote() {
        // get inputs
        val title = noteTitle.text.toString().trim()
        val content = noteContent.text.toString().trim()

        // if empty note than don't save it
        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, getString(R.string.empty_not_saved), Toast.LENGTH_SHORT).show()
            return
        }

        // otherwise create a new note with the inputs
        val id = 0
        val note = Note(id,title,content,imagePath)
        // and save it into the db through the ViewModel
        noteViewModel.insert(note)
    }

    private fun updateNote() {
        // get inputs
        val title = noteTitle.text.toString().trim()
        val content = noteContent.text.toString().trim()

        // if empty then don't update the note, leave it as it was before
        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, getString(R.string.empty_not_saved), Toast.LENGTH_SHORT).show()
            return
        }

        // otherwise update the note with the new inputs: insert works as update because the DAO REPLACES on ID conflict
        val id : Int = oldNote.id
        val note = Note(id,title,content,imagePath)
        // and save it into the db through the ViewModel
        noteViewModel.insert(note)
    }

    private fun shareNote() {
        // if there is no content to share return
        if (noteContent.text.toString().trim().isEmpty()) {
            Toast.makeText(this, getString(R.string.no_content_to_share), Toast.LENGTH_SHORT).show()
            return
        }
        // otherwise start a SEND intent with noteContent as text
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, noteContent.text.toString())
            putExtra(Intent.EXTRA_TITLE, noteTitle.text.toString())
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    private fun deleteNote() {
        // if there is an oldNote in the DB then we delete it
        if (::oldNote.isInitialized) {
            noteViewModel.delete(oldNote)
        }

        // and then we (eventually) delete the inserted image file and finish this activity
        deleteImage()
        finish() // doesn't trigger onBackPressed(), so don't create/update the note
    }
}


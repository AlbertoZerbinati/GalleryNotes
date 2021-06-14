package com.esp.gallerynotes.activities

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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
    private lateinit var deleteImageButton : ImageView

    private lateinit var noteViewModel : NoteViewModel

    private var isUpdate : Boolean = false
    private lateinit var oldNote : Note

    private var imageUri : String = ""

    // receive ACTION_PICK activity result
    private var pickImageLauncherResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        // There are no request codes
        // if everything is ok, than we can display the selected image and save its path in the imageUri variable to be saved later
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
                            imageBitmap = Bitmap.createScaledBitmap(imageBitmap, imageBitmap.width/2, imageBitmap.height/2, false)

                            // if there was an old image displayed (and saved as File in internal storage)
                            // we need to delete it before assigning a new one
                            if (imageUri.isNotEmpty()) {
                                deleteImage()
                            }

                            // generate random filename
                            val generator = Random
                            val randomStringBuilder = StringBuilder()
                            for (i in 0 until 20) {
                                var tempChar: Char = (generator.nextInt(26) + 97).toChar()
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
                                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
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

                            // set imageUri for database
                            imageUri = uri.toString()
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

        // View model for saving data in db
        noteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)

        // Note components mapped to Views
        noteTitle = findViewById(R.id.note_title)
        noteContent = findViewById(R.id.note_content)
        noteImage = findViewById(R.id.note_image)

        // Button for image deletion
        deleteImageButton = findViewById(R.id.delete_image)
        deleteImageButton.setOnClickListener {
            deleteImage()
        }

        // If it is an UPDATE call we take note of that
        if (intent.getIntExtra("requestCode",-1) == RC_UPDATE_NOTE) {
            isUpdate = true
        }
        // The incoming intent will always contain a "note" Extra if this is an UPDATE call.
        // Also ADD calls might have a "note" Extra for example when this activity is recreated
        // after a screen rotation (because we modify the intent in the onPause() method)
        if (intent.hasExtra("note")) {
            // Get the old note parameters
            oldNote = intent.getSerializableExtra("note") as Note

            // Populate views with oldNote content
            noteTitle.setText(oldNote.title)
            noteContent.setText(oldNote.content)

            // noteImage is only populated if there is an image in oldNote to show
            if (oldNote.imageUri.isNotEmpty()) {
                // Set image via URI and adjust visibility
                imageUri = oldNote.imageUri
                noteImage.setImageURI(Uri.parse(imageUri))
                noteImage.visibility = View.VISIBLE
                deleteImageButton.visibility = View.VISIBLE
            }
        }
        supportActionBar?.setDisplayShowTitleEnabled(false) // Remove title from ActionBar
    }

    // Inflate ActionBar Menu (add_image, share_note, delete_note buttons)
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.note_detail_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // When an ActionBar button is selected
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Detect the chosen action
        when(item.itemId) {
            R.id.add_image -> selectImage() // add or replace image
            R.id.share -> shareNote()       // share note
            R.id.delete -> {                // delete note
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
        return super.onOptionsItemSelected(item)
    }

    // Manage screen rotation by updating the Note data in the intent this activity will read after
    // eventual Destruction and reCreation
    override fun onPause() {
        if (isUpdate) {
            // In this case we change the intent-extra from which the activity will build oldNote
            intent.removeExtra("note")
            intent.putExtra("note",
                Note(
                    oldNote.id,
                    noteTitle.text.toString().trim(),
                    noteContent.text.toString().trim(),
                    imageUri
                )
            )
        } else {
            // In this case we have to put the created note inside the intent, so that we find it
            // as we reCreate the activity
            intent.putExtra("note",
                Note(
                    0,
                    noteTitle.text.toString().trim(),
                    noteContent.text.toString().trim(),
                    imageUri
                )
            )
        }
        super.onPause()
    }

    // If back arrow or back button is pressed
    override fun onBackPressed() {
        // save notes before terminating this activity and going back to NotesListActivity
        saveNote()
        super.onBackPressed()
    }

    // Starts pick image activity
    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        pickImageLauncherResult.launch(intent)
    }

    // Delete image from internal storage through contentResolver
    private fun deleteImage() {
        // Adjust image visibility
        noteImage.visibility = View.GONE
        deleteImageButton.visibility = View.GONE

        // Delete image from internal storage
        if (imageUri.isNotBlank()) {
            applicationContext.contentResolver.delete(Uri.parse(imageUri), null, null)
            // Empty image path
            imageUri = ""
        }
    }

    // Create or Update a Note in the DB
    private fun saveNote() {
        // Get inputs
        val title = noteTitle.text.toString().trim()
        val content = noteContent.text.toString().trim()

        // If empty note than don't save it
        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, getString(R.string.empty_not_saved), Toast.LENGTH_SHORT).show()
            deleteImage() // Also delete eventual image saved in internal storage in order not to leak memory
            return
        }

        // Decide whether to update an existing note or create a new one:
        // this works thanks to the fact that the DAO REPLACES on ID conflict
        val id : Int
        if (isUpdate)
            id = oldNote.id
        else
            id = 0

        // Create a new Note with the inputs (imageUri was set after an image was selected from gallery)
        val note = Note(id,title,content,imageUri)

        // Save Note into the DB through the ViewModel
        noteViewModel.insert(note)
    }

    private fun shareNote() {
        // if there is no content to share return
        if (noteContent.text.toString().trim().isEmpty()) {
            Toast.makeText(this, getString(R.string.no_content_to_share), Toast.LENGTH_SHORT).show()
            return
        }
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TITLE, noteTitle.text)
            putExtra(Intent.EXTRA_TEXT, noteContent.text)
            type = "text/plain"
        }
        if (imageUri.isNotBlank()) {
            sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(imageUri))
            sendIntent.clipData =
                ClipData.newRawUri("image", Uri.parse(imageUri))
            sendIntent.type = "image/jpeg"
        }
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val shareIntent = Intent.createChooser(sendIntent, noteTitle.text)
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


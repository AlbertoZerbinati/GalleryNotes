@file:Suppress("PrivatePropertyName")

package com.esp.gallerynotes.activities

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import java.io.InputStream
import java.io.Serializable
import kotlin.random.Random

/*
 * NoteDetailActivity show an interface for Note creation and editing
 * Allows to add/edit Title, Content and an Image to the Note
 * Manages the image as a file, storing and deleting it from internal storage
 * Also allows to share and delete a Note
 * The Note is automatically saved when back button is pressed and the instance state is automatically
 *  saved in order to grant coherent experience on device configuration changes (ex. screen rotation)
 */
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

    // Receiver for ACTION_PICK activity result: the data is the selected image
    private var pickImageLauncherResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->

        // If result is OK, then
        // (1) save a compressed copy of the image in internal storage
        // (2) save its URI in the imageUri variable to be eventually saved later in the DB
        // (3) display the compressed image
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            if (data != null) {
                val selectedImageUri: Uri? = data.data // The original URI of the selected image
                if (selectedImageUri != null) {
                    val inputStream: InputStream? =
                        contentResolver.openInputStream(selectedImageUri) // Get image stream to manipulate the Bitmap
                    if (inputStream != null) {
                        var imageBitmap = BitmapFactory.decodeStream(inputStream) // Get the Bitmap
                        if(imageBitmap != null) {
                            // Scale down the Bitmap size by factor of 2 for saving internal memory and faster performance when reading it later
                            imageBitmap = Bitmap.createScaledBitmap(
                                imageBitmap,
                                imageBitmap.width / 2,
                                imageBitmap.height / 2,
                                false
                            )

                            // If there was an old image displayed ( -> saved as File in internal storage)
                            // delete it before assigning a new one
                            if (imageUri.isNotEmpty()) {
                                deleteImage()
                            }

                            // Generate random 25 char long filename
                            val generator = Random
                            val randomStringBuilder = StringBuilder()
                            for (i in 0 until 25) {
                                val tempChar: Char = (generator.nextInt(26) + 97).toChar() // Only lowercase letters
                                randomStringBuilder.append(tempChar)
                            }
                            val filename = randomStringBuilder.toString()

                            // Use FileProvider to create a File containing a compressed copy of the image
                            val imagesFolder = File(filesDir, "images")

                            imagesFolder.mkdirs() // Create folder
                            val file = File(imagesFolder, "$filename.jpeg") // Create file
                            val stream = FileOutputStream(file)
                            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream) // (1) Write compressed JPEG to the file stream
                            stream.flush()
                            stream.close()
                            // Get the URI of the image copy that will be saved in internal storage
                            val uri = FileProvider.getUriForFile(this,"com.esp.fileprovider", file)
                            
                            // (2) Set imageUri for database
                            imageUri = uri.toString()
                            // (3) Show compressed image
                            noteImage.setImageURI(uri)
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

        // Check if there is a savedInstanceState:
        // after screen rotation recover the note that was being written
        if (savedInstanceState != null) {
            val recoveredNote: Note? = savedInstanceState.getSerializable("note") as Note?
            if (recoveredNote != null) {
                isUpdate = recoveredNote.id != 0 // Check if it is update
                oldNote =
                    recoveredNote // Set oldNote = recoveredNote so that this note is saved appropriately

                // Set views fields
                noteTitle.setText(recoveredNote.title)
                noteContent.setText(recoveredNote.content)

                // Set imageUri and eventually populate the imageView
                imageUri = recoveredNote.imageUri
                if (imageUri.isNotEmpty()) {
                    noteImage.setImageURI(Uri.parse(imageUri))
                    noteImage.visibility = View.VISIBLE
                    deleteImageButton.visibility = View.VISIBLE
                }
            }
        }
        else {  // Otherwise this is a fresh call to NoteDetailActivity:
                // check if it is an update or create call

            if (intent.getIntExtra("requestCode", -100) == RC_UPDATE_NOTE) {
                isUpdate = true // If it is an UPDATE call then take note of that.

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

                // Remove the Extra so that after screen rotation this activity doesn't execute code inside this if
                intent.removeExtra("requestCode")
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
            android.R.id.home -> onBackPressed()    // Save Note and finish
            R.id.add_image -> selectImage()         // Add or replace image
            R.id.share -> shareNote()               // Share note
            R.id.delete -> {                        // Delete note
                val alert: AlertDialog.Builder = AlertDialog.Builder(this)
                alert.setTitle(R.string.delete_note)
                alert.setMessage(R.string.confirm_delete)
                alert.setPositiveButton(R.string.yes) { _, _ -> // confirmed note deletion
                    deleteNote()
                }
                alert.setNegativeButton(R.string.no) { dialog, _ -> // close dialog
                    dialog.cancel()
                }
                alert.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Save the INSTANCE STATE:
    // textViews do so automatically but ImageView doesn't, so override this method
    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        // Set all Note fields and build the Note with that
        val title = noteTitle.text.toString().trim()
        val content = noteContent.text.toString().trim()
        val id : Int = if(isUpdate) oldNote.id else 0
        val recoveredNote = Note(id,title,content,imageUri)

        // Save in Instance State
        savedInstanceState.putSerializable("note", recoveredNote as Serializable)

        super.onSaveInstanceState(savedInstanceState)
    }

    // If back arrow or back button is pressed
    override fun onBackPressed() {
        // Save Note before terminating this activity and going back to NotesListActivity
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
        if (imageUri.isNotEmpty()) {
            applicationContext.contentResolver.delete(Uri.parse(imageUri), null, null)
            // Empty image path
            imageUri = ""
        }
    }

    // Share Note through ACTION_SEND Intent
    private fun shareNote() {
        // If there is no content to share return
        if (noteContent.text.toString().trim().isEmpty()) {
            Toast.makeText(this, getString(R.string.no_content_to_share), Toast.LENGTH_SHORT).show()
            return
        }
        // Otherwise start a SEND intent with note.content as text
        // and note.title as title. Also eventually add image.
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TITLE, noteTitle.text.toString())
            putExtra(Intent.EXTRA_TEXT, noteContent.text.toString())
            type = "text/plain" // Default text/plain SEND_INTENT
        }
        if (imageUri.isNotEmpty()) {
            sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(imageUri))
            sendIntent.clipData =
                ClipData.newRawUri("image", Uri.parse(imageUri))
            sendIntent.type = "image/jpeg"  // Becomes an image/jpeg SEND_INTENT
        }
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        // Start intent
        val shareIntent = Intent.createChooser(sendIntent, noteTitle.text)
        startActivity(shareIntent)
    }

    // Delete Note from DB
    private fun deleteNote() {
        // If there is an oldNote in the DB, that's the one to delete
        if (::oldNote.isInitialized)
            noteViewModel.delete(oldNote)

        // Then (eventually) delete the inserted image file and finish this activity
        deleteImage()
        finish() // doesn't trigger onBackPressed(), so doesn't save the Note
    }

    // Create or Update a Note in the DB
    private fun saveNote() {
        // Get inputs
        val title = noteTitle.text.toString().trim()
        val content = noteContent.text.toString().trim()

        // If empty note than don't save it
        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, getString(R.string.empty_not_saved), Toast.LENGTH_SHORT).show()

            // Also delete eventual image saved in internal storage in order not to leak memory
            // but do so only if this is a Note creation, in order not to delete the oldNote image
            if (!isUpdate)
                deleteImage()

            return
        }

        // Decide whether to update an existing note or create a new one:
        // this works thanks to the fact that the DAO REPLACES on ID conflict
        val id : Int = if(isUpdate) oldNote.id else 0

        // Create a new Note with the inputs (imageUri was set after an image was selected from gallery)
        val note = Note(id,title,content,imageUri)

        // Save Note into the DB through the ViewModel
        noteViewModel.insert(note)
    }
}


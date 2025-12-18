package com.alphacoms.catatin.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alphacoms.catatin.MainActivity
import com.alphacoms.catatin.R
import com.alphacoms.catatin.data.AppDatabase
import com.alphacoms.catatin.data.Note
import com.alphacoms.catatin.data.PreferenceHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotesActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var preferenceHelper: PreferenceHelper
    private lateinit var recyclerViewPinned: RecyclerView
    private lateinit var recyclerViewOthers: RecyclerView
    private lateinit var pinnedAdapter: NotesAdapter
    private lateinit var othersAdapter: NotesAdapter
    private lateinit var fabAddNote: FloatingActionButton
    private lateinit var tvName: TextView
    private lateinit var tvRole: TextView
    private lateinit var tvNoteCount: TextView
    private lateinit var cardPinned: CardView
    private lateinit var cardOthers: CardView
    private lateinit var tvEmptyPin: TextView
    private lateinit var tvEmptyOthers: TextView
    private lateinit var sectionPin: LinearLayout
    
    // Voice input
    private var activeEditText: EditText? = null
    
    // Camera
    private var currentPhotoPath: String? = null
    private var capturedImageUri: Uri? = null
    private var currentNoteImagePath: String? = null
    
    private val speechRecognizerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                val spokenText = matches[0]
                activeEditText?.let { et ->
                    val currentText = et.text.toString()
                    et.setText(if (currentText.isEmpty()) spokenText else "$currentText $spokenText")
                    et.setSelection(et.text.length)
                }
            }
        }
    }
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startVoiceRecognition()
        } else {
            Toast.makeText(this, "Permission ditolak. Tidak dapat menggunakan voice input.", Toast.LENGTH_SHORT).show()
        }
    }
    
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "Permission kamera ditolak.", Toast.LENGTH_SHORT).show()
        }
    }
    
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && capturedImageUri != null) {
            currentNoteImagePath = currentPhotoPath
            Toast.makeText(this, "Foto berhasil diambil", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        preferenceHelper = PreferenceHelper(this)
        initViews()
        setupDatabase()
        setupRecyclerViews()
        setupClickListeners()
        loadNotes()
        loadUserProfile()
        
        // Check if should start with voice input
        if (intent.getBooleanExtra("start_voice", false)) {
            showAddNoteDialog(null, true)
        }
    }
    
    private fun loadUserProfile() {
        tvName.text = preferenceHelper.getUserName()
        tvRole.text = preferenceHelper.getUserRole()
        loadProfileImage()
    }
    
    private fun loadProfileImage() {
        val profileImagePath = preferenceHelper.getProfileImagePath()
        if (profileImagePath.isNotEmpty()) {
            val file = File(profileImagePath)
            if (file.exists()) {
                val bitmap = android.graphics.BitmapFactory.decodeFile(profileImagePath)
                findViewById<ImageView>(R.id.imgProfile).setImageBitmap(bitmap)
                return
            }
        }
        findViewById<ImageView>(R.id.imgProfile).setImageResource(android.R.drawable.sym_def_app_icon)
    }

    private fun initViews() {
        recyclerViewPinned = findViewById(R.id.recyclerViewPinned)
        recyclerViewOthers = findViewById(R.id.recyclerViewOthers)
        fabAddNote = findViewById(R.id.fabAdd)
        tvName = findViewById(R.id.tvName)
        tvRole = findViewById(R.id.tvRole)
        tvNoteCount = findViewById(R.id.tvNoteCount)
        cardPinned = findViewById(R.id.cardPinned)
        cardOthers = findViewById(R.id.cardOthers)
        tvEmptyPin = findViewById(R.id.tvEmptyPin)
        tvEmptyOthers = findViewById(R.id.tvEmptyOthers)
        sectionPin = findViewById(R.id.sectionPin)
    }

    private fun setupDatabase() {
        database = AppDatabase.getDatabase(this)
    }

    private fun setupRecyclerViews() {
        pinnedAdapter = NotesAdapter(
            onItemClick = { note -> showAddNoteDialog(note) },
            onDeleteClick = { note -> deleteNote(note) },
            onPinClick = { note -> togglePin(note) }
        )
        recyclerViewPinned.layoutManager = LinearLayoutManager(this)
        recyclerViewPinned.adapter = pinnedAdapter
        
        othersAdapter = NotesAdapter(
            onItemClick = { note -> showAddNoteDialog(note) },
            onDeleteClick = { note -> deleteNote(note) },
            onPinClick = { note -> togglePin(note) }
        )
        recyclerViewOthers.layoutManager = LinearLayoutManager(this)
        recyclerViewOthers.adapter = othersAdapter
    }
    
    private fun togglePin(note: Note) {
        lifecycleScope.launch {
            val updatedNote = note.copy(isPinned = !note.isPinned)
            database.noteDao().update(updatedNote)
            loadNotes()
            
            val message = if (updatedNote.isPinned) "Catatan di-pin" else "Catatan di-unpin"
            Toast.makeText(this@NotesActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        fabAddNote.setOnClickListener {
            showAddNoteDialog()
        }
        
        // Header buttons
        findViewById<View>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        
        findViewById<View>(R.id.btnNotif).setOnClickListener {
            Toast.makeText(this, "Tidak ada notifikasi baru", Toast.LENGTH_SHORT).show()
        }
        
        findViewById<View>(R.id.imgProfile).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        
        // Filter chips
        val chipAll = findViewById<TextView>(R.id.chipAll)
        val chipPinned = findViewById<TextView>(R.id.chipPinned)
        val chipDefault = findViewById<TextView>(R.id.chipDefault)
        val chipDeleted = findViewById<TextView>(R.id.chipDeleted)
        
        chipAll.setOnClickListener {
            setActiveChip(chipAll, chipPinned, chipDefault, chipDeleted)
            loadNotes("all")
        }
        
        chipPinned.setOnClickListener {
            setActiveChip(chipPinned, chipAll, chipDefault, chipDeleted)
            loadNotes("pinned")
        }
        
        chipDefault.setOnClickListener {
            setActiveChip(chipDefault, chipAll, chipPinned, chipDeleted)
            loadNotes("default")
        }
        
        chipDeleted.setOnClickListener {
            setActiveChip(chipDeleted, chipAll, chipPinned, chipDefault)
            loadNotes("deleted")
        }
        
        // Expand/Collapse Pin section
        findViewById<View>(R.id.headerPin).setOnClickListener {
            val btnExpand = findViewById<ImageView>(R.id.btnExpandPin)
            if (cardPinned.visibility == View.VISIBLE) {
                cardPinned.visibility = View.GONE
                btnExpand.setImageResource(android.R.drawable.arrow_down_float)
            } else {
                cardPinned.visibility = View.VISIBLE
                btnExpand.setImageResource(android.R.drawable.arrow_up_float)
            }
        }
        
        // Bottom Navigation with transitions
        findViewById<View>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            finish()
        }
        
        findViewById<View>(R.id.navNotes).setOnClickListener {
            // Already in Notes, do nothing
        }
        
        findViewById<View>(R.id.navTodo).setOnClickListener {
            startActivity(Intent(this, ToDoListActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        }
        
        findViewById<View>(R.id.navFinance).setOnClickListener {
            startActivity(Intent(this, FinanceActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        }
    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
    
    private fun setActiveChip(active: TextView, vararg inactive: TextView) {
        active.setBackgroundResource(R.drawable.bg_chip_grey)
        active.setTextColor(resources.getColor(android.R.color.black, null))
        for (chip in inactive) {
            chip.setBackgroundResource(R.drawable.bg_chip_transparent)
            chip.setTextColor(resources.getColor(android.R.color.darker_gray, null))
        }
    }

    private fun showAddNoteDialog(note: Note? = null, startWithVoice: Boolean = false) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_note, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etNoteTitle)
        val etContent = dialogView.findViewById<EditText>(R.id.etNoteContent)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<TextView>(R.id.btnSave)
        
        // Bottom action buttons
        val btnActionChecklist = dialogView.findViewById<ImageView>(R.id.btnActionChecklist)
        val btnActionMic = dialogView.findViewById<ImageView>(R.id.btnActionMic)
        val btnActionCamera = dialogView.findViewById<ImageView>(R.id.btnActionCamera)
        
        // Track if voice was used
        var usedVoiceInput = startWithVoice
        currentNoteImagePath = note?.imagePath

        if (note != null) {
            etTitle.setText(note.title)
            etContent.setText(note.content)
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val content = etContent.text.toString().trim()

            if (title.isNotEmpty() && content.isNotEmpty()) {
                if (note == null) {
                    addNote(title, content, usedVoiceInput, currentNoteImagePath)
                } else {
                    updateNote(note.copy(title = title, content = content, imagePath = currentNoteImagePath))
                }
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Silakan isi semua field", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Action button listeners
        btnActionChecklist.setOnClickListener {
            val currentContent = etContent.text.toString()
            val newContent = if (currentContent.isEmpty()) {
                "☐ "
            } else {
                "$currentContent\n☐ "
            }
            etContent.setText(newContent)
            etContent.setSelection(newContent.length)
            Toast.makeText(this, "Checklist ditambahkan", Toast.LENGTH_SHORT).show()
        }

        btnActionMic.setOnClickListener {
            activeEditText = etContent
            usedVoiceInput = true
            checkAndStartVoiceInput()
        }

        btnActionCamera.setOnClickListener {
            checkCameraPermissionAndOpen()
        }
        
        dialog.show()
        
        // Auto start voice input if requested
        if (startWithVoice) {
            activeEditText = etContent
            dialog.setOnShowListener {
                checkAndStartVoiceInput()
            }
        }
    }
    
    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    
    private fun openCamera() {
        val photoFile = createImageFile()
        photoFile?.let {
            capturedImageUri = FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                it
            )
            takePictureLauncher.launch(capturedImageUri)
        }
    }
    
    private fun createImageFile(): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            File.createTempFile(
                "CATATIN_${timeStamp}_",
                ".jpg",
                storageDir
            ).apply {
                currentPhotoPath = absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun checkAndStartVoiceInput() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
            == PackageManager.PERMISSION_GRANTED) {
            startVoiceRecognition()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    
    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale("id", "ID"))
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Bicara sekarang...")
        }
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Voice input tidak tersedia di perangkat ini", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addNote(title: String, content: String, isVoice: Boolean = false, imagePath: String? = null) {
        val note = Note(
            title = title,
            content = content,
            createdAt = Date(),
            isVoiceNote = isVoice,
            imagePath = imagePath
        )

        lifecycleScope.launch {
            database.noteDao().insert(note)
            loadNotes()
            Toast.makeText(this@NotesActivity, "Catatan berhasil ditambahkan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateNote(note: Note) {
        lifecycleScope.launch {
            database.noteDao().update(note)
            loadNotes()
            Toast.makeText(this@NotesActivity, "Catatan berhasil diperbarui", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteNote(note: Note) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Catatan")
            .setMessage("Catatan akan dipindahkan ke 'Baru saja dihapus' dan akan dihapus permanen setelah 30 hari.")
            .setPositiveButton("Hapus") { _, _ ->
                lifecycleScope.launch {
                    // Soft delete - move to trash
                    database.noteDao().softDelete(note.id, System.currentTimeMillis())
                    loadNotes()
                    Toast.makeText(this@NotesActivity, "Catatan dipindahkan ke Baru saja dihapus", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
    
    private fun restoreNote(note: Note) {
        lifecycleScope.launch {
            database.noteDao().restoreNote(note.id)
            loadNotes("deleted")
            Toast.makeText(this@NotesActivity, "Catatan berhasil dipulihkan", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun permanentDeleteNote(note: Note) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Permanen")
            .setMessage("Catatan akan dihapus permanen dan tidak dapat dipulihkan. Lanjutkan?")
            .setPositiveButton("Hapus Permanen") { _, _ ->
                lifecycleScope.launch {
                    database.noteDao().delete(note)
                    loadNotes("deleted")
                    Toast.makeText(this@NotesActivity, "Catatan dihapus permanen", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
    
    private fun showDeletedNoteOptions(note: Note) {
        val daysLeft = calculateDaysLeft(note.deletedAt)
        
        AlertDialog.Builder(this)
            .setTitle(note.title)
            .setMessage("Catatan ini akan dihapus permanen dalam $daysLeft hari")
            .setPositiveButton("Pulihkan") { _, _ ->
                restoreNote(note)
            }
            .setNegativeButton("Hapus Permanen") { _, _ ->
                permanentDeleteNote(note)
            }
            .setNeutralButton("Batal", null)
            .show()
    }
    
    private fun calculateDaysLeft(deletedAt: Date?): Int {
        if (deletedAt == null) return 30
        val now = System.currentTimeMillis()
        val deleteTime = deletedAt.time
        val daysPassed = ((now - deleteTime) / (1000 * 60 * 60 * 24)).toInt()
        return maxOf(0, 30 - daysPassed)
    }
    
    private fun cleanupOldTrash() {
        lifecycleScope.launch {
            // Delete notes older than 30 days
            val threshold = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
            database.noteDao().deleteOldTrash(threshold)
        }
    }

    private fun loadNotes(filter: String = "all") {
        // Cleanup old trash on load
        cleanupOldTrash()
        
        lifecycleScope.launch {
            when (filter) {
                "deleted" -> {
                    val deletedNotes = database.noteDao().getDeletedNotesSync()
                    
                    // Use othersAdapter to show deleted notes
                    pinnedAdapter.submitList(emptyList())
                    othersAdapter.submitList(deletedNotes)
                    
                    sectionPin.visibility = View.GONE
                    
                    findViewById<LinearLayout>(R.id.sectionOthers).visibility = View.VISIBLE
                    findViewById<TextView>(R.id.tvOthersTitle).text = "Baru saja dihapus"
                    cardOthers.visibility = if (deletedNotes.isEmpty()) View.GONE else View.VISIBLE
                    tvEmptyOthers.visibility = if (deletedNotes.isEmpty()) View.VISIBLE else View.GONE
                    tvEmptyOthers.text = "Tidak ada catatan yang dihapus"
                    
                    tvNoteCount.text = "${deletedNotes.size} Catatan dihapus"
                    
                    // Set click listener to show restore options
                    othersAdapter.setDeletedMode(true) { note ->
                        showDeletedNoteOptions(note)
                    }
                }
                "pinned" -> {
                    val allNotes = database.noteDao().getAllNotesSync()
                    val pinnedNotes = allNotes.filter { it.isPinned }
                    pinnedAdapter.submitList(pinnedNotes)
                    othersAdapter.submitList(emptyList())
                    
                    sectionPin.visibility = View.VISIBLE
                    cardPinned.visibility = if (pinnedNotes.isEmpty()) View.GONE else View.VISIBLE
                    tvEmptyPin.visibility = if (pinnedNotes.isEmpty()) View.VISIBLE else View.GONE
                    
                    findViewById<LinearLayout>(R.id.sectionOthers).visibility = View.GONE
                    findViewById<TextView>(R.id.tvOthersTitle).text = "Lainnya"
                    tvEmptyOthers.text = "Tidak ada catatan lain"
                    
                    tvNoteCount.text = "${pinnedNotes.size} Catatan"
                    
                    othersAdapter.setDeletedMode(false, null)
                }
                "default" -> {
                    val allNotes = database.noteDao().getAllNotesSync()
                    val defaultNotes = allNotes.filter { !it.isPinned }
                    pinnedAdapter.submitList(emptyList())
                    othersAdapter.submitList(defaultNotes)
                    
                    sectionPin.visibility = View.GONE
                    
                    findViewById<LinearLayout>(R.id.sectionOthers).visibility = View.VISIBLE
                    findViewById<TextView>(R.id.tvOthersTitle).text = "Lainnya"
                    cardOthers.visibility = if (defaultNotes.isEmpty()) View.GONE else View.VISIBLE
                    tvEmptyOthers.visibility = if (defaultNotes.isEmpty()) View.VISIBLE else View.GONE
                    tvEmptyOthers.text = "Tidak ada catatan lain"
                    
                    tvNoteCount.text = "${defaultNotes.size} Catatan"
                    
                    othersAdapter.setDeletedMode(false, null)
                }
                else -> {
                    // Show all with sections
                    val allNotes = database.noteDao().getAllNotesSync()
                    val pinnedNotes = allNotes.filter { it.isPinned }
                    val otherNotes = allNotes.filter { !it.isPinned }
                    
                    pinnedAdapter.submitList(pinnedNotes)
                    othersAdapter.submitList(otherNotes)
                    
                    sectionPin.visibility = View.VISIBLE
                    cardPinned.visibility = if (pinnedNotes.isEmpty()) View.GONE else View.VISIBLE
                    tvEmptyPin.visibility = if (pinnedNotes.isEmpty()) View.VISIBLE else View.GONE
                    
                    findViewById<LinearLayout>(R.id.sectionOthers).visibility = View.VISIBLE
                    findViewById<TextView>(R.id.tvOthersTitle).text = "Lainnya"
                    cardOthers.visibility = if (otherNotes.isEmpty()) View.GONE else View.VISIBLE
                    tvEmptyOthers.visibility = if (otherNotes.isEmpty()) View.VISIBLE else View.GONE
                    tvEmptyOthers.text = "Tidak ada catatan lain"
                    
                    tvNoteCount.text = "${allNotes.size} Catatan"
                    
                    othersAdapter.setDeletedMode(false, null)
                }
            }
        }
    }
}
package com.alphacoms.catatin.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alphacoms.catatin.R
import com.alphacoms.catatin.data.AppDatabase
import com.alphacoms.catatin.data.Note
import com.alphacoms.catatin.utils.AudioProcessor
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

class VoiceNoteActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var etTitle: TextInputEditText
    private lateinit var etContent: TextInputEditText
    private lateinit var btnVoiceInput: MaterialButton
    private lateinit var btnImportAudio: MaterialButton
    private lateinit var btnSaveNote: MaterialButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var audioProcessor: AudioProcessor

    private val audioFilePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { processAudioFile(it) }
    }

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            openAudioFilePicker()
        } else {
            showPermissionDeniedDialog()
        }
    }

    private val speechRecognizerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                val spokenText = matches[0]
                val currentText = etContent.text.toString()
                etContent.setText(if (currentText.isEmpty()) spokenText else "$currentText $spokenText")
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_note)

        initViews()
        setupDatabase()
        setupAudioProcessor()
        setupRecyclerView()
        setupClickListeners()
        loadNotes()
    }

    private fun initViews() {
        etTitle = findViewById(R.id.etTitle)
        etContent = findViewById(R.id.etContent)
        btnVoiceInput = findViewById(R.id.btnVoiceInput)
        btnImportAudio = findViewById(R.id.btnImportAudio)
        btnSaveNote = findViewById(R.id.btnSaveNote)
        recyclerView = findViewById(R.id.recyclerViewNotes)
        progressIndicator = findViewById(R.id.progressIndicator)
    }

    private fun setupDatabase() {
        database = AppDatabase.getDatabase(this)
    }

    private fun setupAudioProcessor() {
        audioProcessor = AudioProcessor(this)
    }

    private fun setupRecyclerView() {
        notesAdapter = NotesAdapter { note ->
            deleteNote(note)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = notesAdapter
    }

    private fun setupClickListeners() {
        btnVoiceInput.setOnClickListener {
            checkPermissionAndStartVoiceRecognition()
        }

        btnImportAudio.setOnClickListener {
            checkStoragePermissionAndImportAudio()
        }

        btnSaveNote.setOnClickListener {
            saveNote()
        }
    }

    private fun checkPermissionAndStartVoiceRecognition() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                startVoiceRecognition()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun startVoiceRecognition() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Speech recognition tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Mulai berbicara...")
        }

        try {
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveNote() {
        val title = etTitle.text.toString().trim()
        val content = etContent.text.toString().trim()

        if (title.isEmpty()) {
            etTitle.error = "Judul tidak boleh kosong"
            return
        }

        if (content.isEmpty()) {
            etContent.error = "Konten tidak boleh kosong"
            return
        }

        val note = Note(
            title = title,
            content = content,
            createdAt = Date(),
            isVoiceNote = true
        )

        lifecycleScope.launch {
            try {
                database.noteDao().insertNote(note)
                runOnUiThread {
                    Toast.makeText(this@VoiceNoteActivity, "Catatan berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    clearInputs()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@VoiceNoteActivity, "Error menyimpan catatan: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deleteNote(note: Note) {
        lifecycleScope.launch {
            try {
                database.noteDao().deleteNote(note)
                runOnUiThread {
                    Toast.makeText(this@VoiceNoteActivity, "Catatan dihapus", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@VoiceNoteActivity, "Error menghapus catatan: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun clearInputs() {
        etTitle.setText("")
        etContent.setText("")
    }

    private fun loadNotes() {
        database.noteDao().getVoiceNotes().observe(this) { notes ->
            notesAdapter.submitList(notes)
        }
    }

    // === AUDIO FILE IMPORT FUNCTIONS ===
    
    private fun checkStoragePermissionAndImportAudio() {
        val permissions = mutableListOf<String>()
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) 
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
            }
        }

        if (permissions.isEmpty()) {
            openAudioFilePicker()
        } else {
            storagePermissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun openAudioFilePicker() {
        try {
            audioFilePickerLauncher.launch("audio/*")
        } catch (e: Exception) {
            Toast.makeText(this, "Error membuka file picker: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processAudioFile(uri: Uri) {
        lifecycleScope.launch {
            try {
                // Show loading
                progressIndicator.visibility = android.view.View.VISIBLE
                btnImportAudio.isEnabled = false
                btnVoiceInput.isEnabled = false

                // Validate file
                val (isValid, message) = audioProcessor.validateAudioFile(uri)
                if (!isValid) {
                    showError("File tidak valid", message)
                    return@launch
                }

                // Show file info
                showAudioFileInfo(uri)

                // Transcribe audio
                val result = audioProcessor.transcribeAudioFile(uri)
                
                if (result.success) {
                    // Set title if empty
                    if (etTitle.text.toString().trim().isEmpty()) {
                        val fileName = getDisplayName(uri)
                        etTitle.setText("Audio: ${fileName.substringBeforeLast(".")}")
                    }
                    
                    // Set transcribed content
                    val currentText = etContent.text.toString()
                    val newText = if (currentText.isEmpty()) {
                        result.text
                    } else {
                        "$currentText\n\n--- Audio File ---\n${result.text}"
                    }
                    etContent.setText(newText)

                    Toast.makeText(this@VoiceNoteActivity, 
                        "Audio berhasil diproses! Confidence: ${(result.confidence * 100).toInt()}%", 
                        Toast.LENGTH_LONG).show()
                } else {
                    showError("Gagal memproses audio", result.error)
                }

            } catch (e: Exception) {
                showError("Error", "Gagal memproses file audio: ${e.message}")
            } finally {
                // Hide loading
                progressIndicator.visibility = android.view.View.GONE
                btnImportAudio.isEnabled = true
                btnVoiceInput.isEnabled = true
            }
        }
    }

    private fun showAudioFileInfo(uri: Uri) {
        val fileName = getDisplayName(uri)
        val fileSize = audioProcessor.getAudioFileSize(uri)
        val duration = audioProcessor.getAudioDuration(uri)
        
        val fileSizeMB = fileSize / (1024.0 * 1024.0)
        val durationSeconds = duration / 1000

        val message = """
            File: $fileName
            Size: ${"%.1f".format(fileSizeMB)} MB
            Duration: ${durationSeconds}s
            
            Memproses audio file...
        """.trimIndent()

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun getDisplayName(uri: Uri): String {
        return try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) cursor.getString(nameIndex) else "audio_file"
                } else "audio_file"
            } ?: "audio_file"
        } catch (e: Exception) {
            "audio_file"
        }
    }

    private fun showError(title: String, message: String) {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("Aplikasi memerlukan izin akses storage untuk import file audio. Silakan berikan izin di pengaturan.")
            .setPositiveButton("Buka Pengaturan") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", packageName, null)
                startActivity(intent)
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
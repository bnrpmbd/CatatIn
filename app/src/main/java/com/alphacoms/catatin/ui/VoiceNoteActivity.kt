package com.alphacoms.catatin.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alphacoms.catatin.R
import com.alphacoms.catatin.data.AppDatabase
import com.alphacoms.catatin.data.Note
import com.alphacoms.catatin.data.ToDoViewModel
import com.alphacoms.catatin.data.ToDoViewModelFactory
import com.alphacoms.catatin.data.ToDoRepository
import com.alphacoms.catatin.ui.NotesAdapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

class VoiceNoteActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var etTitle: TextInputEditText
    private lateinit var etContent: TextInputEditText
    private lateinit var btnVoiceInput: MaterialButton
    private lateinit var btnSaveNote: MaterialButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var notesAdapter: NotesAdapter

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

    private val viewModel: ToDoViewModel by viewModels {
        val dao = AppDatabase.getDatabase(this).todoDao()
        val repo = ToDoRepository(dao)
        ToDoViewModelFactory(repo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_note)

        // Enable action bar back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Voice Note"

        initViews()
        setupDatabase()
        setupRecyclerView()
        setupClickListeners()
        loadNotes()
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initViews() {
        etTitle = findViewById(R.id.etTitle)
        etContent = findViewById(R.id.etContent)
        btnVoiceInput = findViewById(R.id.btnVoiceInput)
        btnSaveNote = findViewById(R.id.btnSaveNote)
        recyclerView = findViewById(R.id.recyclerViewNotes)
    }

    private fun setupDatabase() {
        database = AppDatabase.getDatabase(this)
    }

    private fun setupRecyclerView() {
        notesAdapter = NotesAdapter(
            onItemClick = { note -> editNote(note) },
            onDeleteClick = { note -> deleteNote(note) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = notesAdapter
    }

    private fun setupClickListeners() {
        btnVoiceInput.setOnClickListener {
            checkPermissionAndStartVoiceRecognition()
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
                database.noteDao().insert(note)
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
                database.noteDao().delete(note)
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

    private fun editNote(note: Note) {
        etTitle.setText(note.title)
        etContent.setText(note.content)
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
}
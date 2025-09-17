package com.alphacoms.catatin.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alphacoms.catatin.R
import com.alphacoms.catatin.data.AppDatabase
import com.alphacoms.catatin.data.Note
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.util.Date

class NotesActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var fabAddNote: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        initViews()
        setupDatabase()
        setupRecyclerView()
        setupClickListeners()
        loadNotes()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewNotes)
        fabAddNote = findViewById(R.id.fabAddNote)
    }

    private fun setupDatabase() {
        database = AppDatabase.getDatabase(this)
    }

    private fun setupRecyclerView() {
        notesAdapter = NotesAdapter(
            onItemClick = { note -> showAddNoteDialog(note) },
            onDeleteClick = { note -> deleteNote(note) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = notesAdapter
    }

    private fun setupClickListeners() {
        fabAddNote.setOnClickListener {
            showAddNoteDialog()
        }
    }

    private fun showAddNoteDialog(note: Note? = null) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_note, null)
        val etTitle = dialogView.findViewById<TextInputEditText>(R.id.etNoteTitle)
        val etContent = dialogView.findViewById<TextInputEditText>(R.id.etNoteContent)

        if (note != null) {
            etTitle.setText(note.title)
            etContent.setText(note.content)
        }

        AlertDialog.Builder(this)
            .setTitle(if (note == null) "Tambah Catatan" else "Edit Catatan")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val title = etTitle.text.toString().trim()
                val content = etContent.text.toString().trim()

                if (title.isNotEmpty() && content.isNotEmpty()) {
                    if (note == null) {
                        addNote(title, content)
                    } else {
                        updateNote(note.copy(title = title, content = content))
                    }
                } else {
                    Toast.makeText(this, "Silakan isi semua field", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun addNote(title: String, content: String) {
        val note = Note(
            title = title,
            content = content,
            createdAt = Date(),
            isVoiceNote = false
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
            .setMessage("Apakah Anda yakin ingin menghapus catatan ini?")
            .setPositiveButton("Hapus") { _, _ ->
                lifecycleScope.launch {
                    database.noteDao().delete(note)
                    loadNotes()
                    Toast.makeText(this@NotesActivity, "Catatan berhasil dihapus", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun loadNotes() {
        lifecycleScope.launch {
            val notes = database.noteDao().getAllNotesSync()
            notesAdapter.submitList(notes)
        }
    }
}
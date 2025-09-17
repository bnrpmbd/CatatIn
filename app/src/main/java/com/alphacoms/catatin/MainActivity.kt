package com.alphacoms.catatin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.alphacoms.catatin.ui.NotesActivity
import com.alphacoms.catatin.ui.VoiceNoteActivity
import com.alphacoms.catatin.ui.ToDoListActivity
import com.alphacoms.catatin.ui.FinanceActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        findViewById<CardView>(R.id.cardNotes).setOnClickListener {
            startActivity(Intent(this, NotesActivity::class.java))
        }

        findViewById<CardView>(R.id.cardVoiceNote).setOnClickListener {
            startActivity(Intent(this, VoiceNoteActivity::class.java))
        }

        findViewById<CardView>(R.id.cardTodoList).setOnClickListener {
            startActivity(Intent(this, ToDoListActivity::class.java))
        }

        findViewById<CardView>(R.id.cardFinance).setOnClickListener {
            startActivity(Intent(this, FinanceActivity::class.java))
        }
    }
}
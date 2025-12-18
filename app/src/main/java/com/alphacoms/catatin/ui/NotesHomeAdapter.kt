package com.alphacoms.catatin.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alphacoms.catatin.R
import com.alphacoms.catatin.data.Note

class NotesHomeAdapter(
    private val onItemClick: (Note) -> Unit
) : ListAdapter<Note, NotesHomeAdapter.NoteViewHolder>(NoteDiffCallback()) {

    // Tape colors: blue, yellow, pink, green
    private val tapeColors = listOf(
        Color.parseColor("#8080DEEA"),  // Light blue
        Color.parseColor("#80FFE082"),  // Yellow
        Color.parseColor("#80F8BBD9"),  // Pink  
        Color.parseColor("#80A5D6A7")   // Green
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note_home, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvNoteTitle)
        private val tvContent: TextView = itemView.findViewById(R.id.tvNoteContent)
        private val viewTape: View = itemView.findViewById(R.id.viewTape)

        fun bind(note: Note, position: Int) {
            tvTitle.text = note.title
            tvContent.text = note.content
            
            // Alternate tape colors
            viewTape.setBackgroundColor(tapeColors[position % tapeColors.size])
            
            itemView.setOnClickListener {
                onItemClick(note)
            }
        }
    }

    class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }
}

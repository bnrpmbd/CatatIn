package com.alphacoms.catatin.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alphacoms.catatin.R
import com.alphacoms.catatin.data.Note
import java.text.SimpleDateFormat
import java.util.Locale

class NotesAdapter(
    private val onItemClick: (Note) -> Unit,
    private val onDeleteClick: (Note) -> Unit,
    private val onPinClick: ((Note) -> Unit)? = null
) : ListAdapter<Note, NotesAdapter.NoteViewHolder>(NoteDiffCallback()) {

    private var isDeletedMode = false
    private var onDeletedItemClick: ((Note) -> Unit)? = null
    
    fun setDeletedMode(enabled: Boolean, callback: ((Note) -> Unit)?) {
        isDeletedMode = enabled
        onDeletedItemClick = callback
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvNoteTitle)
        private val tvContent: TextView = itemView.findViewById(R.id.tvNoteContent)
        private val tvDate: TextView = itemView.findViewById(R.id.tvNoteDate)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteNote)
        private val btnPin: ImageButton = itemView.findViewById(R.id.btnPinNote)

        fun bind(note: Note) {
            tvTitle.text = note.title
            tvContent.text = note.content
            
            if (isDeletedMode && note.deletedAt != null) {
                // Show when it was deleted and days remaining
                val daysLeft = calculateDaysLeft(note.deletedAt)
                tvDate.text = "Dihapus ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(note.deletedAt)} • $daysLeft hari lagi"
                
                // Hide pin and delete buttons in deleted mode
                btnPin.visibility = View.GONE
                btnDelete.visibility = View.GONE
                
                // Click to show restore options
                itemView.setOnClickListener {
                    onDeletedItemClick?.invoke(note)
                }
            } else {
                tvDate.text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(note.createdAt)
                
                btnPin.visibility = View.VISIBLE
                btnDelete.visibility = View.VISIBLE
                
                // Update pin icon based on state
                if (note.isPinned) {
                    btnPin.setImageResource(android.R.drawable.btn_star_big_on)
                } else {
                    btnPin.setImageResource(android.R.drawable.btn_star_big_off)
                }
                
                itemView.setOnClickListener {
                    onItemClick(note)
                }
                
                btnDelete.setOnClickListener {
                    onDeleteClick(note)
                }
                
                btnPin.setOnClickListener {
                    onPinClick?.invoke(note)
                }
            }
        }
        
        private fun calculateDaysLeft(deletedAt: java.util.Date?): Int {
            if (deletedAt == null) return 30
            val now = System.currentTimeMillis()
            val deleteTime = deletedAt.time
            val daysPassed = ((now - deleteTime) / (1000 * 60 * 60 * 24)).toInt()
            return maxOf(0, 30 - daysPassed)
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
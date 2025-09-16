package com.alphacoms.catatin.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alphacoms.catatin.R
import com.alphacoms.catatin.data.Priority
import com.alphacoms.catatin.data.ToDo
import java.text.SimpleDateFormat
import java.util.Locale

class ToDoAdapter(
    private val onItemClick: (ToDo) -> Unit,
    private val onStatusChange: (ToDo, Boolean) -> Unit,
    private val onDeleteClick: (ToDo) -> Unit
) : ListAdapter<ToDo, ToDoAdapter.ToDoViewHolder>(ToDoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo, parent, false)
        return ToDoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ToDoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cbCompleted: CheckBox = itemView.findViewById(R.id.cbTodoCompleted)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTodoTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvTodoDescription)
        private val tvPriority: TextView = itemView.findViewById(R.id.tvTodoPriority)
        private val tvDueDate: TextView = itemView.findViewById(R.id.tvTodoDueDate)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteTodo)

        fun bind(todo: ToDo) {
            cbCompleted.isChecked = todo.isCompleted
            tvTitle.text = todo.title
            tvDescription.text = todo.description
            
            // Set priority with color
            tvPriority.text = todo.priority.name
            tvPriority.setBackgroundColor(getPriorityColor(todo.priority))
            
            // Set due date
            tvDueDate.text = todo.dueDate?.let {
                "Due: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)}"
            } ?: "No due date"

            // Set listeners
            cbCompleted.setOnCheckedChangeListener { _, isChecked ->
                onStatusChange(todo, isChecked)
            }

            itemView.setOnClickListener {
                onItemClick(todo)
            }

            btnDelete.setOnClickListener {
                onDeleteClick(todo)
            }

            // Style completed items
            if (todo.isCompleted) {
                tvTitle.alpha = 0.6f
                tvDescription.alpha = 0.6f
            } else {
                tvTitle.alpha = 1.0f
                tvDescription.alpha = 1.0f
            }
        }

        private fun getPriorityColor(priority: Priority): Int {
            return when (priority) {
                Priority.URGENT -> android.graphics.Color.parseColor("#F44336") // Red
                Priority.HIGH -> android.graphics.Color.parseColor("#FF9800") // Orange
                Priority.NORMAL -> android.graphics.Color.parseColor("#4CAF50") // Green
                Priority.LOW -> android.graphics.Color.parseColor("#9E9E9E") // Grey
            }
        }
    }

    class ToDoDiffCallback : DiffUtil.ItemCallback<ToDo>() {
        override fun areItemsTheSame(oldItem: ToDo, newItem: ToDo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ToDo, newItem: ToDo): Boolean {
            return oldItem == newItem
        }
    }
}
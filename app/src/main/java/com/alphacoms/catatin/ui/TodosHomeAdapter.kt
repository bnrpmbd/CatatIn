package com.alphacoms.catatin.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alphacoms.catatin.R
import com.alphacoms.catatin.data.Priority
import com.alphacoms.catatin.data.ToDo
import java.text.SimpleDateFormat
import java.util.Locale

class TodosHomeAdapter(
    private val onItemClick: (ToDo) -> Unit
) : ListAdapter<ToDo, TodosHomeAdapter.TodoViewHolder>(TodoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo_home, parent, false)
        return TodoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cbStatus: CheckBox = itemView.findViewById(R.id.cbTodoStatus)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTodoTitle)
        private val tvDueDate: TextView = itemView.findViewById(R.id.tvTodoDueDate)
        private val viewPriority: View = itemView.findViewById(R.id.viewPriority)

        fun bind(todo: ToDo) {
            tvTitle.text = todo.title
            cbStatus.isChecked = todo.isCompleted
            
            // Format due date
            todo.dueDate?.let { date ->
                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                tvDueDate.text = dateFormat.format(date)
                tvDueDate.visibility = View.VISIBLE
            } ?: run {
                tvDueDate.visibility = View.GONE
            }
            
            // Set priority color
            val priorityColor = when (todo.priority) {
                Priority.HIGH, Priority.URGENT -> Color.parseColor("#F44336")
                Priority.MEDIUM, Priority.NORMAL -> Color.parseColor("#FF9800")
                Priority.LOW -> Color.parseColor("#4CAF50")
            }
            viewPriority.background.setTint(priorityColor)
            
            itemView.setOnClickListener {
                onItemClick(todo)
            }
        }
    }

    class TodoDiffCallback : DiffUtil.ItemCallback<ToDo>() {
        override fun areItemsTheSame(oldItem: ToDo, newItem: ToDo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ToDo, newItem: ToDo): Boolean {
            return oldItem == newItem
        }
    }
}

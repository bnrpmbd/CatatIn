package com.alphacoms.catatin.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "todos")
data class ToDo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Date,
    val dueDate: Date? = null,
    val priority: Priority = Priority.NORMAL
)

enum class Priority {
    LOW, NORMAL, HIGH, URGENT
}
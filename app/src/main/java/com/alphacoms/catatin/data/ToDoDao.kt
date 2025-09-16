package com.alphacoms.catatin.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ToDoDao {
    @Query("SELECT * FROM todos ORDER BY priority DESC, createdAt DESC")
    fun getAllTodos(): LiveData<List<ToDo>>

    @Query("SELECT * FROM todos WHERE isCompleted = 0 ORDER BY priority DESC, createdAt DESC")
    fun getActiveTodos(): LiveData<List<ToDo>>

    @Query("SELECT * FROM todos WHERE isCompleted = 1 ORDER BY createdAt DESC")
    fun getCompletedTodos(): LiveData<List<ToDo>>

    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getToDoById(id: Long): ToDo?

    @Insert
    suspend fun insertToDo(todo: ToDo): Long

    @Update
    suspend fun updateToDo(todo: ToDo)

    @Delete
    suspend fun deleteToDo(todo: ToDo)

    @Query("UPDATE todos SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateToDoStatus(id: Long, isCompleted: Boolean)
}
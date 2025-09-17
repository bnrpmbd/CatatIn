package com.alphacoms.catatin.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    fun getAllNotes(): LiveData<List<Note>>

    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    suspend fun getAllNotesSync(): List<Note>

    @Query("SELECT * FROM notes WHERE isVoiceNote = 1 ORDER BY createdAt DESC")
    fun getVoiceNotes(): LiveData<List<Note>>

    @Query("SELECT * FROM notes WHERE isVoiceNote = 1 ORDER BY createdAt DESC")
    suspend fun getVoiceNotesSync(): List<Note>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Long): Note?

    @Insert
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)
}
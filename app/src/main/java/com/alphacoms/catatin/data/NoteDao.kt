package com.alphacoms.catatin.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NoteDao {
    // Get active notes (not deleted)
    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllNotes(): LiveData<List<Note>>

    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY createdAt DESC")
    suspend fun getAllNotesSync(): List<Note>

    @Query("SELECT * FROM notes WHERE isVoiceNote = 1 AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getVoiceNotes(): LiveData<List<Note>>

    @Query("SELECT * FROM notes WHERE isVoiceNote = 1 AND isDeleted = 0 ORDER BY createdAt DESC")
    suspend fun getVoiceNotesSync(): List<Note>
    
    // Get deleted notes (trash)
    @Query("SELECT * FROM notes WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    suspend fun getDeletedNotesSync(): List<Note>

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
    
    // Soft delete - move to trash
    @Query("UPDATE notes SET isDeleted = 1, deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDelete(id: Long, deletedAt: Long)
    
    // Restore from trash
    @Query("UPDATE notes SET isDeleted = 0, deletedAt = NULL WHERE id = :id")
    suspend fun restoreNote(id: Long)
    
    // Permanently delete notes older than 30 days
    @Query("DELETE FROM notes WHERE isDeleted = 1 AND deletedAt < :threshold")
    suspend fun deleteOldTrash(threshold: Long)
}
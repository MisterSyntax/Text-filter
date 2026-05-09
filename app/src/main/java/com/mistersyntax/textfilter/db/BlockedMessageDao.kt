package com.mistersyntax.textfilter.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedMessageDao {
    @Insert
    suspend fun insert(message: BlockedMessage): Long

    @Query("SELECT * FROM blocked_messages ORDER BY receivedAt DESC")
    fun observeAll(): Flow<List<BlockedMessage>>

    @Query("SELECT COUNT(*) FROM blocked_messages")
    fun observeCount(): Flow<Int>

    @Query("DELETE FROM blocked_messages WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM blocked_messages")
    suspend fun deleteAll()
}

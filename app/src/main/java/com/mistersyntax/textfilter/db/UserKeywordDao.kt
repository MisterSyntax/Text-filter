package com.mistersyntax.textfilter.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserKeywordDao {
    @Insert
    suspend fun insert(keyword: UserKeyword): Long

    @Update
    suspend fun update(keyword: UserKeyword)

    @Query("SELECT * FROM user_keywords ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<UserKeyword>>

    @Query("SELECT * FROM user_keywords WHERE enabled = 1")
    suspend fun getAllEnabled(): List<UserKeyword>

    @Query("DELETE FROM user_keywords WHERE id = :id")
    suspend fun delete(id: Long)
}

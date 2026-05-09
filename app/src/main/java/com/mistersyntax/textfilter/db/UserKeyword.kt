package com.mistersyntax.textfilter.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_keywords")
data class UserKeyword(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val keyword: String,
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
)

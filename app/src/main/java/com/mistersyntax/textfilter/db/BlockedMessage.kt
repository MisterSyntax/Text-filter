package com.mistersyntax.textfilter.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_messages")
data class BlockedMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String,
    val body: String,
    val score: Float,
    val matchedRules: String,   // comma-separated rule names
    val receivedAt: Long = System.currentTimeMillis(),
    val wasSilentlyDropped: Boolean,
)

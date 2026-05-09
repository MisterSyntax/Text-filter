package com.mistersyntax.textfilter.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [BlockedMessage::class], version = 1, exportSchema = false)
abstract class SpamDatabase : RoomDatabase() {
    abstract fun blockedMessageDao(): BlockedMessageDao

    companion object {
        @Volatile private var instance: SpamDatabase? = null

        fun get(context: Context): SpamDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    SpamDatabase::class.java,
                    "spam.db",
                ).build().also { instance = it }
            }
    }
}

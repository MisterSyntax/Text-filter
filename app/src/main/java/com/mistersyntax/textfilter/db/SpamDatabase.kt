package com.mistersyntax.textfilter.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [BlockedMessage::class, UserKeyword::class],
    version = 2,
    exportSchema = false,
)
abstract class SpamDatabase : RoomDatabase() {
    abstract fun blockedMessageDao(): BlockedMessageDao
    abstract fun userKeywordDao(): UserKeywordDao

    companion object {
        @Volatile private var instance: SpamDatabase? = null

        fun get(context: Context): SpamDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    SpamDatabase::class.java,
                    "spam.db",
                ).addMigrations(MIGRATION_1_2).build().also { instance = it }
            }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `user_keywords` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `keyword` TEXT NOT NULL,
                        `enabled` INTEGER NOT NULL DEFAULT 1,
                        `createdAt` INTEGER NOT NULL
                    )"""
                )
            }
        }
    }
}

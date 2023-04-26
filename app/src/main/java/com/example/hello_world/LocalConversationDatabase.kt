package com.example.hello_world

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [LocalConversationEntity::class, LocalConversationMessageEntity::class],
    version = 2,
    exportSchema = false
)
abstract class LocalConversationDatabase : RoomDatabase() {
    abstract fun conversationDao(): LocalConversationDao

    companion object {
        @Volatile
        private var INSTANCE: LocalConversationDatabase? = null

        fun getInstance(context: Context): LocalConversationDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LocalConversationDatabase::class.java,
                    "conversation_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
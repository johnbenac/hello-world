package com.example.hello_world.data.local.conversation.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.hello_world.data.local.configpack.dao.LocalConfigPackDao
import com.example.hello_world.data.local.configpack.entities.LocalConfigPackEntity
import com.example.hello_world.data.local.conversation.entities.LocalConversationEntity
import com.example.hello_world.data.local.conversation.entities.LocalConversationMessageEntity
import com.example.hello_world.data.local.conversation.dao.LocalConversationDao


@Database(
    entities = [LocalConversationEntity::class, LocalConversationMessageEntity::class, LocalConfigPackEntity::class],
    version = 3,
    exportSchema = false
)
abstract class LocalConversationDatabase : RoomDatabase() {
    abstract fun conversationDao(): LocalConversationDao
    abstract fun configPackDao(): LocalConfigPackDao

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
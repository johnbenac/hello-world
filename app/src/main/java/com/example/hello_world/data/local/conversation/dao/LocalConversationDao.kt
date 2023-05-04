package com.example.hello_world.data.local.conversation.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.hello_world.data.local.conversation.entities.LocalConversationEntity
import com.example.hello_world.data.local.conversation.entities.LocalConversationMessageEntity

@Dao
interface LocalConversationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: LocalConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: LocalConversationMessageEntity)

    @Query("SELECT * FROM conversations WHERE id = :conversationId")
    suspend fun getConversation(conversationId: String): LocalConversationEntity?

    @Query("SELECT * FROM conversation_messages WHERE conversationId = :conversationId")
    suspend fun getMessages(conversationId: String): List<LocalConversationMessageEntity>

    @Transaction
    suspend fun saveConversation(conversation: LocalConversationEntity, messages: List<LocalConversationMessageEntity>) {
        insertConversation(conversation)
        messages.forEach { insertMessage(it) }
    }

    @Query("SELECT * FROM conversations")
    suspend fun getAllConversations(): List<LocalConversationEntity>

    @Query("DELETE FROM conversations WHERE id = :conversationId")
    suspend fun deleteConversation(conversationId: String)

    @Query("DELETE FROM conversation_messages WHERE conversationId = :conversationId")
    suspend fun deleteMessages(conversationId: String)
}
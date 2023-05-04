package com.example.hello_world.data.repository

import com.example.hello_world.models.Conversation
import java.util.UUID

interface IConversationRepository {
    suspend fun saveConversation(conversation: Conversation)
    suspend fun loadConversation(conversationId: UUID): Conversation?
    suspend fun deleteConversation(conversationId: UUID)
    suspend fun loadAllConversations(): List<Conversation>
}


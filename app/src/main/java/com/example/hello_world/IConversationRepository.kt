package com.example.hello_world

import com.example.hello_world.Conversation
import java.util.UUID

interface IConversationRepository {
    suspend fun saveConversation(conversation: Conversation)
    suspend fun loadConversation(conversationId: UUID): Conversation?
    suspend fun deleteConversation(conversationId: UUID)
    suspend fun loadAllConversations(): List<Conversation>
}
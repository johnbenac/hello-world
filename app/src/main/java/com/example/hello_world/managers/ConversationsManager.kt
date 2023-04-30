package com.example.hello_world.managers

import com.example.hello_world.data.repository.IConversationRepository
import com.example.hello_world.models.Conversation
import java.util.UUID

class ConversationsManager(private val conversationRepository: IConversationRepository) {

    suspend fun saveConversation(conversation: Conversation) {
        conversationRepository.saveConversation(conversation)
    }

    suspend fun loadConversation(conversationId: UUID): Conversation? {
        return conversationRepository.loadConversation(conversationId)
    }

    suspend fun deleteConversation(conversationId: UUID) {
        conversationRepository.deleteConversation(conversationId)
    }

    suspend fun loadAllConversations(): List<Conversation> {
        return conversationRepository.loadAllConversations()
    }
}
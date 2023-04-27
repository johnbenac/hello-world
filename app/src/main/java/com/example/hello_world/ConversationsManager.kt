package com.example.hello_world

import com.example.hello_world.Conversation
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
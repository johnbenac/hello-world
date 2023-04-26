package com.example.hello_world

import Conversation
import java.util.UUID

class LocalRoomConversationRepository : IConversationRepository {
    override fun saveConversation(conversation: Conversation) {
        // TODO: Implement saving conversation
    }

    override fun loadConversation(conversationId: UUID): Conversation? {
        // TODO: Implement loading conversation by ID
        return null
    }

    override fun deleteConversation(conversationId: UUID) {
        // TODO: Implement deleting conversation
    }
}
package com.example.hello_world

import Conversation
import android.content.Context
import java.util.UUID

class LocalRoomConversationRepository(context: Context) : IConversationRepository {
    private val conversationDao = LocalConversationDatabase.getInstance(context).conversationDao()

    override suspend fun saveConversation(conversation: Conversation) {
        // TODO: Implement saving conversation
    }

    override suspend fun loadConversation(conversationId: UUID): Conversation? {
        // TODO: Implement loading conversation by ID
        return null
    }

    override suspend fun deleteConversation(conversationId: UUID) {
        // TODO: Implement deleting conversation
    }
}
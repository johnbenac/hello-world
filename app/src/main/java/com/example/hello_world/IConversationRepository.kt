package com.example.hello_world

import Conversation
import java.util.UUID

interface IConversationRepository {
    suspend fun saveConversation(conversation: Conversation)
    suspend fun loadConversation(conversationId: UUID): Conversation?
    suspend fun deleteConversation(conversationId: UUID)
    // TODO: implementing audio export and compilation
}
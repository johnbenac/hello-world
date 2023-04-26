package com.example.hello_world

import Conversation
import java.util.UUID

interface IConversationRepository {
    fun saveConversation(conversation: Conversation)
    fun loadConversation(conversationId: UUID): Conversation?
    fun deleteConversation(conversationId: UUID)
}
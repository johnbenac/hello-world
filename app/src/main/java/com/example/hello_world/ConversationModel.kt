package com.example.hello_world

import Conversation
import ConversationMessage
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConversationModel(private val conversationRepository: IConversationRepository) {
    var conversation: Conversation = Conversation(
        profile = Profile(
            name = "Jake",
            systemMessage = "You are an AI assistant named Jake.",
            maxLength = 100,
            temperature = 0.9,
            frequencyPenalty = 0.0,
            presencePenalty = 0.1,
            model = "gpt-3.5-turbo"
        )
    )
    fun addMessage(message: ConversationMessage) {
        conversation.messages.add(message)
    }
    fun updateMessage(index: Int, updatedMessage: ConversationMessage) {
        conversation.messages[index] = updatedMessage
    }

    fun deleteMessage(index: Int) {
        conversation.messages.removeAt(index)
    }

    fun saveConversation() {
        // TODO: Save the entire conversation to the repository
        CoroutineScope(Dispatchers.IO).launch {
            conversationRepository.saveConversation(conversation)
        }
    }

    fun loadConversation(conversationId: UUID) {
        // TODO: Load the conversation with the given ID from the repository
        CoroutineScope(Dispatchers.IO).launch {
            val loadedConversation = conversationRepository.loadConversation(conversationId)
            if (loadedConversation != null) {
                // TODO: Update the conversation state with the loaded conversation
            }
        }
    }


    suspend fun deleteConversation(conversationId: UUID) {
        conversationRepository.deleteConversation(conversationId)
    }
}
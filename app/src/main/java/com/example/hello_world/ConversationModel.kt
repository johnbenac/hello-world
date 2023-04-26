package com.example.hello_world

import Conversation
import ConversationMessage

class ConversationModel {
    val conversation: Conversation = Conversation(
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

    // TODO: Implement methods for saving and retrieving conversations
}
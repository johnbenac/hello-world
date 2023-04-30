package com.example.hello_world.managers

import com.example.hello_world.models.Conversation
import com.example.hello_world.models.ConversationMessage

class ConversationManager(var conversation: Conversation) {

    fun addMessage(message: ConversationMessage) {
        conversation.messages.add(message)
    }

    fun updateMessage(index: Int, updatedMessage: ConversationMessage) {
        conversation.messages[index] = updatedMessage
    }

    fun deleteMessage(index: Int) {
        conversation.messages.removeAt(index)
    }
}
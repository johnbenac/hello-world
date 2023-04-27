package com.example.hello_world

import com.example.hello_world.Conversation
import ConversationMessage

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
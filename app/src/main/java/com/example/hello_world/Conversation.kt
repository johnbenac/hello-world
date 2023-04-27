package com.example.hello_world
import ConversationMessage
import Profile
import java.util.UUID

data class Conversation(
    val id: UUID = UUID.randomUUID(),
    val messages: MutableList<ConversationMessage> = mutableListOf(),
    val profile: Profile,
    val createdAt: Long = System.currentTimeMillis(),
    val title: String = "Untitled Conversation",
    val dateStarted: Long = System.currentTimeMillis(),
    val dateLastSaved: Long = System.currentTimeMillis(),
    val messageCount: Int = 0
)
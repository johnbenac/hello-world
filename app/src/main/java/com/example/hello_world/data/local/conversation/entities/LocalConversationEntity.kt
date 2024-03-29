package com.example.hello_world.data.local.conversation.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class LocalConversationEntity(
    @PrimaryKey
    val id: String,
    val profileJson: String,
    val createdAt: Long,
    val title: String?,
    val dateStarted: Long,
    val dateLastSaved: Long,
    val messageCount: Int
)
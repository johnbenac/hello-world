package com.example.hello_world

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class LocalConversationEntity(
    @PrimaryKey
    val id: String,
    val profileJson: String,
    val createdAt: Long,
    val title: String?
)
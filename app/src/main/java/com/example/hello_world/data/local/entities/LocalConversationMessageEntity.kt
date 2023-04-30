package com.example.hello_world.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.hello_world.data.local.entities.LocalConversationEntity

@Entity(
    tableName = "conversation_messages",
    foreignKeys = [
        ForeignKey(
            entity = LocalConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LocalConversationMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val conversationId: String,
    val sender: String,
    val message: String,
    val audioFilePath: String
)
package com.example.hello_world

import Conversation
import ConversationMessage
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory


import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalRoomConversationRepository(context: Context) : IConversationRepository {
    private val conversationDao = LocalConversationDatabase.getInstance(context).conversationDao()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    override suspend fun saveConversation(conversation: Conversation) {
        Log.d("LocalRoomRepo", "Saving conversation with ID: ${conversation.id}")
        val profileJson = moshi.adapter(Profile::class.java).toJson(conversation.profile)
        withContext(Dispatchers.IO) {
            val conversationEntity = LocalConversationEntity(
                id = conversation.id.toString(),
                profileJson = profileJson,
                createdAt = conversation.createdAt,
                title = conversation.title
            )
            val messageEntities = conversation.messages.map { message ->
                LocalConversationMessageEntity(
                    conversationId = conversation.id.toString(),
                    sender = message.sender,
                    message = message.message,
                    audioFilePath = message.audioFilePath.value
                )
            }
            conversationDao.saveConversation(conversationEntity, messageEntities)
        }
    }

    override suspend fun loadConversation(conversationId: UUID): Conversation? {
        Log.d("LocalRoomRepo", "Loading conversation with ID: $conversationId")
        return withContext(Dispatchers.IO) {
            val conversationEntity = conversationDao.getConversation(conversationId.toString())
            val messageEntities = conversationDao.getMessages(conversationId.toString())

            if (conversationEntity != null) {
                val profile = moshi.adapter(Profile::class.java).fromJson(conversationEntity.profileJson)
                profile?.let { // Add this line
                    val messages = messageEntities.map { entity ->
                        ConversationMessage(
                            sender = entity.sender,
                            message = entity.message,
                            audioFilePath = mutableStateOf(entity.audioFilePath)
                        )
                    }.toMutableList()

                    Conversation(
                        id = UUID.fromString(conversationEntity.id),
                        messages = messages,
                        profile = it, // Modify this line
                        createdAt = conversationEntity.createdAt,
                        title = conversationEntity.title
                    )
                } // Add this line
            } else {
                null
            }
        }
    }

    override suspend fun deleteConversation(conversationId: UUID) {
        Log.d("LocalRoomRepo", "Deleting conversation with ID: $conversationId")
        withContext(Dispatchers.IO) {
            conversationDao.deleteMessages(conversationId.toString())
            conversationDao.deleteConversation(conversationId.toString())
        }
    }
}
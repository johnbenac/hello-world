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
                title = conversation.title,
                dateStarted = conversation.dateStarted,
                dateLastSaved = conversation.dateLastSaved,
                messageCount = conversation.messages.size
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
            val profile = moshi.adapter(Profile::class.java).fromJson(conversationEntity?.profileJson)
            if (conversationEntity != null && profile != null) {
                val profile = moshi.adapter(Profile::class.java).fromJson(conversationEntity.profileJson)
                profile?.let {
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
                        profile = it,
                        createdAt = conversationEntity.createdAt,
                        title = conversationEntity.title.orEmpty(),
                        dateStarted = conversationEntity.dateStarted,
                        dateLastSaved = conversationEntity.dateLastSaved,
                        messageCount = conversationEntity.messageCount
                    )
                }
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

    override suspend fun loadAllConversations(): List<Conversation> {
        return withContext(Dispatchers.IO) {
            val conversationEntities = conversationDao.getAllConversations()
            conversationEntities.map { entity ->
                val profile = moshi.adapter(Profile::class.java).fromJson(entity.profileJson)
                profile?.let {
                    Conversation(
                        id = UUID.fromString(entity.id),
                        messages = mutableListOf(), // We don't need messages for the saved conversations list
                        profile = it,
                        createdAt = entity.createdAt,
                        title = entity.title.orEmpty(),
                        dateStarted = entity.dateStarted,
                        dateLastSaved = entity.dateLastSaved,
                        messageCount = entity.messageCount
                    )
                }
            }.filterNotNull()
        }
    }
}
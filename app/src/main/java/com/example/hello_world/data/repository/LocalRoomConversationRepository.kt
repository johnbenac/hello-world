package com.example.hello_world.data.repository

import android.content.ContentValues
import com.example.hello_world.models.ConversationMessage
import com.example.hello_world.models.ConfigPack
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.example.hello_world.data.local.conversation.database.LocalConversationDatabase
import com.example.hello_world.data.local.conversation.entities.LocalConversationEntity
import com.example.hello_world.data.local.conversation.entities.LocalConversationMessageEntity
import com.example.hello_world.models.Conversation
import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.Date


import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale


class LocalRoomConversationRepository(private val context: Context) : IConversationRepository {
    private val conversationDao = LocalConversationDatabase.getInstance(context).conversationDao()

    private val moshi = Moshi.Builder()
        .add(UUIDJsonAdapter())
        .add(MutableStateStringJsonAdapter())
        .add(KotlinJsonAdapterFactory())
        .build()
    override suspend fun saveConversation(conversation: Conversation) {
        Log.d("LocalRoomRepo", "Saving conversation with ID: ${conversation.id}")
        val configPackJson = moshi.adapter(ConfigPack::class.java).toJson(conversation.configPack)
        withContext(Dispatchers.IO) {
            val conversationEntity = LocalConversationEntity(
                id = conversation.id.toString(),
                profileJson = configPackJson,
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
            val configPack = moshi.adapter(ConfigPack::class.java).fromJson(conversationEntity?.profileJson)
            if (conversationEntity != null && configPack != null) {
                val configPack = moshi.adapter(ConfigPack::class.java).fromJson(conversationEntity.profileJson)
                configPack?.let {
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
                        configPack = it,
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
                val configPack = moshi.adapter(ConfigPack::class.java).fromJson(entity.profileJson)
                configPack?.let {
                    Conversation(
                        id = UUID.fromString(entity.id),
                        messages = mutableListOf(), // We don't need messages for the saved conversations list
                        configPack = it,
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
//    suspend fun saveExportedFile(context: Context, fileName: String, jsonString: String): Uri? {
//        val contentValues = ContentValues().apply {
//            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
//            put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
//            val downloadsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
//            put(MediaStore.MediaColumns.DATA, "$downloadsPath/$fileName")
//        }
//
//        val contentResolver = context.contentResolver
//        val uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
//
//        if (uri != null) {
//            withContext(Dispatchers.IO) {
//                contentResolver.openOutputStream(uri)?.use { outputStream ->
//                    outputStream.write(jsonString.toByteArray())
//                }
//            }
//
//            Log.d("LocalRoomRepo", "File saved successfully with URI: $uri")
//        } else {
//            Log.d("LocalRoomRepo", "Failed to save file, URI is null")
//        }
//
//        return uri
//    }
//    override suspend fun exportConversations(): String {
//        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
//        // Load all conversations with messages
//        val conversations = withContext(Dispatchers.IO) {
//            val conversationEntities = conversationDao.getAllConversations()
//            conversationEntities.mapNotNull { entity ->
//                val configPack = moshi.adapter(ConfigPack::class.java).fromJson(entity.profileJson)
//                val messages = conversationDao.getMessages(entity.id).map { messageEntity ->
//                    ConversationMessage(
//                        sender = messageEntity.sender,
//                        message = messageEntity.message,
//                        audioFilePath = mutableStateOf(messageEntity.audioFilePath)
//                    )
//                }
//                configPack?.let {
//                    Conversation(
//                        id = UUID.fromString(entity.id),
//                        messages = messages.toMutableList(),
//                        configPack = it,
//                        createdAt = entity.createdAt,
//                        title = entity.title.orEmpty(),
//                        dateStarted = entity.dateStarted,
//                        dateLastSaved = entity.dateLastSaved,
//                        messageCount = entity.messageCount
//                    )
//                }
//            }
//        }
//
//        // Copy audio files to external storage
//        Log.d("LocalRoomRepo", "Copying audio files to external storage")
//        val audioFolderPath = copyAudioFilesToExternal(conversations, context)
//
//        // Update audio file paths to external storage paths
//        val updatedConversations = conversations.map { conversation ->
//            conversation.copy(
//                messages = conversation.messages.map { message ->
//                    val externalAudioFile = File(audioFolderPath, File(message.audioFilePath.value).name)
//                    val newAudioFilePath = externalAudioFile.absolutePath
//                    message.copy(audioFilePath = mutableStateOf(newAudioFilePath))
//                }.toMutableList()
//            )
//        }
//
//        // Create ExportData object with updated conversations
//        val exportData = ExportData(conversations = updatedConversations)
//
//        // Serialize ExportData object to JSON
//        Log.d("LocalRoomRepo", "Serializing ExportData object to JSON")
//        val jsonString = moshi.adapter(ExportData::class.java).toJson(exportData)
//
//        // Save the jsonString to a file
//        Log.d("LocalRoomRepo", "Saving jsonString to a file")
//        val exportFileName = "exported_conversations_$timestamp.json"
//        val exportFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), exportFileName)
//        withContext(Dispatchers.IO) {
//            exportFile.writeText(jsonString)
//        }
//
//        val uri = saveExportedFile(context, "exported_conversations.json", jsonString)
//
//        if (uri != null) {
//            // Show a Toast message for a successful export
//            withContext(Dispatchers.Main) {
//                Toast.makeText(context, "Export successful. File saved to ${uri.path}", Toast.LENGTH_LONG).show()
//            }
//            Log.d("LocalRoomRepo", "Export successful. File saved to ${uri.path}")
//        } else {
//            // Show a Toast message for a failed export
//            withContext(Dispatchers.Main) {
//                Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
//            }
//            Log.d("LocalRoomRepo", "Export failed")
//        }
//
//        return jsonString
//    }

    // Implement the importConversations method
//    override suspend fun importConversations(json: String) {
//        val exportData = moshi.adapter(ExportData::class.java).fromJson(json) ?: return
//        exportData.conversations.forEach { importedConversation ->
//            // Copy audio files back to the app's data folder
//            val updatedMessages = importedConversation.messages.map { message ->
//                val externalAudioFile = File(message.audioFilePath.value)
//                if (externalAudioFile.exists()) {
//                    val internalAudioFile = File(context.filesDir, "ConversationAudio/${externalAudioFile.name}")
//                    withContext(Dispatchers.IO) {
//                        externalAudioFile.copyTo(internalAudioFile, overwrite = true)
//                    }
//                    message.copy(audioFilePath = mutableStateOf(internalAudioFile.absolutePath))
//                } else {
//                    message
//                }
//            }
//            saveConversation(importedConversation.copy(messages = updatedMessages.toMutableList()))
//        }
//    }
//
//    suspend fun copyAudioFilesToExternal(conversations: List<Conversation>, context: Context): String {
//        val externalFolderPath = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath
//        val audioFolder = File(externalFolderPath, "ConversationAudio")
//        if (!audioFolder.exists()) {
//            audioFolder.mkdirs()
//        }
//
//        conversations.forEach { conversation ->
//            conversation.messages.forEach { message ->
//                val audioFilePath = message.audioFilePath.value
//                if (audioFilePath.isNotEmpty()) {
//                    val sourceFile = File(audioFilePath)
//                    if (sourceFile.exists()) {
//                        val destinationFile = File(audioFolder, sourceFile.name)
//                        if (!destinationFile.exists()) {
//                            withContext(Dispatchers.IO) {
//                                sourceFile.copyTo(destinationFile, overwrite = true)
//                            }
//                        } else {
//                            Log.d("LocalRoomRepo", "Destination file already exists, skipping: ${destinationFile.absolutePath}")
//                        }
//                    } else {
//                        Log.w("LocalRoomRepo", "Source file not found: $audioFilePath")
//                    }
//                }
//            }
//        }
//
//        return audioFolder.absolutePath
//    }
//
//
//
}
//
//
//data class ExportData(
//    val conversations: List<Conversation>
//)

class UUIDJsonAdapter {
    @ToJson
    fun toJson(uuid: UUID): String {
        return uuid.toString()
    }

    @FromJson
    fun fromJson(uuidString: String): UUID {
        return UUID.fromString(uuidString)
    }
}

class MutableStateStringJsonAdapter {
    @ToJson
    fun toJson(state: MutableState<String>): String {
        return state.value
    }

    @FromJson
    fun fromJson(string: String): MutableState<String> {
        return mutableStateOf(string)
    }
}

package com.example.hello_world.services.local_backup






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

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
//import com.example.hello_world.data.repository.ExportData
import com.example.hello_world.data.repository.IConversationRepository
import com.example.hello_world.data.repository.LocalRoomConversationRepository
import com.example.hello_world.data.repository.MutableStateStringJsonAdapter
import com.example.hello_world.data.repository.UUIDJsonAdapter
import com.example.hello_world.models.ConfigPack

import com.example.hello_world.models.ConversationMessage
import com.example.hello_world.services.backup.ExportData
import com.example.hello_world.services.backup.IBackup

import kotlinx.coroutines.withContext

import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream


class LocalBackupHelper(private val context: Context) : IBackup {
    private val conversationRepository = LocalRoomConversationRepository(context)
    private val conversationDao = LocalConversationDatabase.getInstance(context).conversationDao()
    private val moshi = Moshi.Builder()
        .add(UUIDJsonAdapter())
        .add(MutableStateStringJsonAdapter())
        .add(KotlinJsonAdapterFactory())
        .build()
    // Add the exportConversations() method from LocalRoomConversationRepository.kt
    override suspend fun exportConversations(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        // Load all conversations with messages
        val conversations = withContext(Dispatchers.IO) {
            val conversationEntities = conversationDao.getAllConversations()
            conversationEntities.mapNotNull { entity ->
                val configPack = moshi.adapter(ConfigPack::class.java).fromJson(entity.profileJson)
                val messages = conversationDao.getMessages(entity.id).map { messageEntity ->
                    ConversationMessage(
                        sender = messageEntity.sender,
                        message = messageEntity.message,
                        audioFilePath = mutableStateOf(messageEntity.audioFilePath)
                    )
                }
                configPack?.let {
                    Conversation(
                        id = UUID.fromString(entity.id),
                        messages = messages.toMutableList(),
                        configPack = it,
                        createdAt = entity.createdAt,
                        title = entity.title.orEmpty(),
                        dateStarted = entity.dateStarted,
                        dateLastSaved = entity.dateLastSaved,
                        messageCount = entity.messageCount
                    )
                }
            }
        }

        // Copy audio files to external storage
        Log.d("LocalRoomRepo", "Copying audio files to external storage")
        val audioFolderPath = copyAudioFilesToExternal(conversations, context)

        // Update audio file paths to external storage paths
        val updatedConversations = conversations.map { conversation ->
            conversation.copy(
                messages = conversation.messages.map { message ->
                    val externalAudioFile = File(audioFolderPath, File(message.audioFilePath.value).name)
                    val newAudioFilePath = externalAudioFile.absolutePath
                    message.copy(audioFilePath = mutableStateOf(newAudioFilePath))
                }.toMutableList()
            )
        }

        // Create ExportData object with updated conversations
        val exportData = ExportData(conversations = updatedConversations)

        // Serialize ExportData object to JSON
        Log.d("LocalRoomRepo", "Serializing ExportData object to JSON")
        val jsonString = moshi.adapter(ExportData::class.java).toJson(exportData)

        // Save the jsonString to a file
        Log.d("LocalRoomRepo", "Saving jsonString to a file")
        val exportFileName = "exported_conversations_$timestamp.json"
        val exportFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), exportFileName)
        withContext(Dispatchers.IO) {
            exportFile.writeText(jsonString)
        }

        val uri = saveExportedFile(context, "exported_conversations.json", jsonString)

        if (uri != null) {
            // Show a Toast message for a successful export
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Export successful. File saved to ${uri.path}", Toast.LENGTH_LONG).show()
            }
            Log.d("LocalRoomRepo", "Export successful. File saved to ${uri.path}")
        } else {
            // Show a Toast message for a failed export
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
            }
            Log.d("LocalRoomRepo", "Export failed")
        }

        return jsonString
    }

    override suspend fun importConversations(json: String) {
        val exportData = moshi.adapter(ExportData::class.java).fromJson(json) ?: return
        exportData.conversations.forEach { importedConversation ->
            // Copy audio files back to the app's data folder
            val updatedMessages = importedConversation.messages.map { message ->
                val externalAudioFile = File(message.audioFilePath.value)
                if (externalAudioFile.exists()) {
                    val internalAudioFile = File(context.filesDir, "${externalAudioFile.name}")
                    withContext(Dispatchers.IO) {
                        externalAudioFile.copyTo(internalAudioFile, overwrite = true)
                    }
                    message.copy(audioFilePath = mutableStateOf(internalAudioFile.absolutePath))
                } else {
                    message
                }
            }
            conversationRepository.saveConversation(importedConversation.copy(messages = updatedMessages.toMutableList()))
        }
    }


    suspend fun copyAudioFilesToExternal(conversations: List<Conversation>, context: Context): String {
        val externalFolderPath = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath
        val audioFolder = File(externalFolderPath)
        if (!audioFolder.exists()) {
            audioFolder.mkdirs()
        }

        conversations.forEach { conversation ->
            conversation.messages.forEach { message ->
                val audioFilePath = message.audioFilePath.value
                if (audioFilePath.isNotEmpty()) {
                    val sourceFile = File(audioFilePath)
                    if (sourceFile.exists()) {
                        val destinationFile = File(audioFolder, sourceFile.name)
                        if (!destinationFile.exists()) {
                            withContext(Dispatchers.IO) {
                                sourceFile.copyTo(destinationFile, overwrite = true)
                            }
                        } else {
                            Log.d("LocalRoomRepo", "Destination file already exists, skipping: ${destinationFile.absolutePath}")
                        }
                    } else {
                        Log.w("LocalRoomRepo", "Source file not found: $audioFilePath")
                    }
                }
            }
        }

        return audioFolder.absolutePath
    }

    suspend fun saveExportedFile(context: Context, fileName: String, jsonString: String): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
            val downloadsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
            put(MediaStore.MediaColumns.DATA, "$downloadsPath/$fileName")
        }

        val contentResolver = context.contentResolver
        val uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

        if (uri != null) {
            withContext(Dispatchers.IO) {
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray())
                }
            }

            Log.d("LocalRoomRepo", "File saved successfully with URI: $uri")
        } else {
            Log.d("LocalRoomRepo", "Failed to save file, URI is null")
        }

        return uri
    }


}



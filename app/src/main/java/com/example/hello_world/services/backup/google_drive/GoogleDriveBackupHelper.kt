package com.example.hello_world.services.backup.google_drive

import android.util.Log
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.mutableStateOf
import com.example.hello_world.data.local.conversation.database.LocalConversationDatabase
import com.example.hello_world.data.repository.IConversationRepository
import com.example.hello_world.data.repository.LocalRoomConversationRepository
import com.example.hello_world.data.repository.MutableStateStringJsonAdapter
import com.example.hello_world.data.repository.UUIDJsonAdapter
import com.example.hello_world.models.ConfigPack
import com.example.hello_world.models.Conversation
import com.example.hello_world.models.ConversationMessage
import com.example.hello_world.services.backup.ExportData
import com.example.hello_world.services.backup.IBackup
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.services.drive.Drive
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.http.InputStreamContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.model.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.FileList
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.ByteArrayOutputStream
import java.util.*


class GoogleDriveBackupHelper(
    private val activity: Activity,
) : IBackup {
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()
    private val authenticationResults = mutableListOf<CompletableDeferred<Boolean>>()
    private val conversationRepository = LocalRoomConversationRepository(activity)
//    val isAuthenticated: Boolean
//        get() = ::driveService.isInitialized
    private val conversationDao = LocalConversationDatabase.getInstance(activity).conversationDao()
    private val moshi = Moshi.Builder()
        .add(UUIDJsonAdapter())
        .add(MutableStateStringJsonAdapter())
        .add(KotlinJsonAdapterFactory())
        .build()
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var driveService: Drive
    fun authenticate(): Deferred<Boolean> = CoroutineScope(Dispatchers.IO).async {
        Log.d("GoogleDriveHelper", "authenticate() called")
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA), Scope(DriveScopes.DRIVE_FILE))
            .build()
        Log.d("GoogleDriveHelper", "gso is: $gso")
        googleSignInClient = GoogleSignIn.getClient(activity, gso)
        Log.d("GoogleDriveHelper", "googleSignInClient is: $googleSignInClient")
        val signInIntent = googleSignInClient.signInIntent
        Log.d("GoogleDriveHelper", "signInIntent is: $signInIntent")
        activity.startActivityForResult(signInIntent, RC_SIGN_IN)
        Log.d("GoogleDriveHelper", "activity.startActivityForResult(signInIntent, RC_SIGN_IN) is : activity.startActivityForResult($signInIntent, $RC_SIGN_IN)")
        val authenticationResult = CompletableDeferred<Boolean>()
        authenticationResults.add(authenticationResult)
        return@async authenticationResult.await()
    }

//    suspend fun isAuthenticated(): Boolean = withContext(Dispatchers.IO) {
//        return@withContext ::driveService.isInitialized
//    }


    fun handleSignInResult(resultCode: Int, data: Intent?) {
        Log.d("GoogleDriveHelper", "handleSignInResult() called with resultCode=$resultCode") // Add this log statement
        if (resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                // Signed in successfully, you can now access the Google Drive API using the account object
                Log.d("GoogleDriveHelper", "Signed in successfully: ${account?.displayName}, email: ${account?.email}")
                setupDriveService(account)
                createHelloWorldFolderAndFile()

                // Complete the authentication result with true (success)
                authenticationResults.forEach { it.complete(true) }
            } catch (e: ApiException) {
                Log.w("GoogleDriveHelper", "signInResult:failed code=" + e.statusCode)
                Log.w("GoogleDriveHelper", "signInResult:status=" + e.status)
                Log.w("GoogleDriveHelper", "signInResult:message=" + e.message)
            }
        } else {
            Log.w("GoogleDriveHelper", "signInResult:resultCode not OK, resultCode=$resultCode")
            // Complete the authentication result with false (failure)
            authenticationResults.forEach { it.complete(false) }
        }
        authenticationResults.clear()
    }


    companion object {
        const val RC_SIGN_IN = 9001
        const val REQUEST_AUTHORIZATION = 9002
    }

    override suspend fun exportConversations(): String {
        val conversationDataJson = getConversationDataAsJson()

        val folderId = findOrCreateSessionBackupsFolder()
        saveJsonToGoogleDrive(conversationDataJson, folderId)

        return "Backup successful"
    }

    override suspend fun importConversations(json: String) {
        val folderId = findOrCreateSessionBackupsFolder()
        val backupFiles = listBackupFilesInFolder(folderId)

        // Provide the user with an option to select a backup file they wish to restore
        val selectedFileId = getUserSelectedBackupFile(backupFiles)

        val jsonString = getJsonStringFromGoogleDrive(selectedFileId)
        updateConversationDataFromJson(jsonString)
    }

    private suspend fun findOrCreateSessionBackupsFolder(): String = withContext(Dispatchers.IO) {
        val query = "mimeType='application/vnd.google-apps.folder' and trashed=false and name='SessionBackups'"
        return@withContext try {
            val files: FileList = driveService.files().list().setQ(query).execute()
            if (files.files.isNotEmpty()) {
                files.files[0].id
            } else {
                createSessionBackupsFolder()
            }
        } catch (e: UserRecoverableAuthIOException) {
            activity.startActivityForResult(e.intent, REQUEST_AUTHORIZATION)
            ""
        }
    }

    private fun getUserSelectedBackupFile(backupFiles: List<File>): String {
        // Call the showBackupFilesDialog() method and return the selected file ID
        var selectedFileId = ""
        activity.runOnUiThread {
            selectedFileId = showBackupFilesDialog(backupFiles)
        }
        return selectedFileId
    }


    private fun setupDriveService(account: GoogleSignInAccount) {
        val credential = GoogleAccountCredential.usingOAuth2(activity, Collections.singleton("https://www.googleapis.com/auth/drive"))
        credential.selectedAccount = account.account
        driveService = Drive.Builder(AndroidHttp.newCompatibleTransport(), GsonFactory(), credential).setApplicationName("Hello World").build()
    }

    private fun createHelloWorldFolderAndFile() {
        CoroutineScope(Dispatchers.IO).launch {
            val folderId = findOrCreateHelloWorldFolder()
            val conversationDataJson = getConversationDataAsJson()
            createHelloWorldFile(conversationDataJson, folderId)


//            val conversationDataJson = getConversationDataAsJson()
//
//            val folderId = findOrCreateSessionBackupsFolder()
//            saveJsonToGoogleDrive(conversationDataJson, folderId)


        }
    }

    private suspend fun findOrCreateHelloWorldFolder(): String = withContext(Dispatchers.IO) {
        val query = "mimeType='application/vnd.google-apps.folder' and trashed=false and name='SessionBackups'"
        return@withContext try {
            val files: FileList = driveService.files().list().setQ(query).execute()
            if (files.files.isNotEmpty()) {
                files.files[0].id
            } else {
                createSessionBackupsFolder()
            }
        } catch (e: UserRecoverableAuthIOException) {
            // Start the activity to prompt the user for authorization
            activity.startActivityForResult(e.intent, REQUEST_AUTHORIZATION)
            ""
        }
    }

    private suspend fun createHelloWorldFolder(): String = withContext(Dispatchers.IO) {
        val fileMetadata = File()
        fileMetadata.name = "hello world"
        fileMetadata.mimeType = "application/vnd.google-apps.folder"
        val file = driveService.files().create(fileMetadata).setFields("id").execute()
        return@withContext file.id
    }

    private suspend fun createHelloWorldFile(jsonString: String, folderId: String) = withContext(Dispatchers.IO) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileMetadata = File()
        fileMetadata.name = "conversation_backup_$timestamp.json"
        fileMetadata.parents = Collections.singletonList(folderId)
        val fileContent = ByteArrayInputStream(jsonString.toByteArray(Charsets.UTF_8))
        val mediaContent = InputStreamContent("application/json", fileContent)
        driveService.files().create(fileMetadata, mediaContent).setFields("id").execute()
    }

    private suspend fun createSessionBackupsFolder(): String = withContext(Dispatchers.IO) {
        val fileMetadata = File()
        fileMetadata.name = "SessionBackups"
        fileMetadata.mimeType = "application/vnd.google-apps.folder"
        val file = driveService.files().create(fileMetadata).setFields("id").execute()
        return@withContext file.id
    }

    private suspend fun saveJsonToGoogleDrive(jsonString: String, folderId: String) = withContext(Dispatchers.IO) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileMetadata = File()
        fileMetadata.name = "conversation_backup_$timestamp.json"
        fileMetadata.parents = Collections.singletonList(folderId)
        val fileContent = ByteArrayInputStream(jsonString.toByteArray(Charsets.UTF_8))
        val mediaContent = InputStreamContent("application/json", fileContent)
        driveService.files().create(fileMetadata, mediaContent).setFields("id").execute()
    }

    private suspend fun listBackupFilesInFolder(folderId: String): List<File> = withContext(Dispatchers.IO) {
        val query = "mimeType='application/json' and trashed=false and parents in '$folderId'"
        return@withContext try {
            val files: FileList = driveService.files().list().setQ(query).execute()
            files.files
        } catch (e: UserRecoverableAuthIOException) {
            activity.startActivityForResult(e.intent, REQUEST_AUTHORIZATION)
            emptyList()
        }
    }

    private fun showBackupFilesDialog(backupFiles: List<File>): String {
        var selectedFileId = ""
        val fileNames = backupFiles.map { it.name }.toTypedArray()
        val checkedItem = -1 // No item is initially selected

        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Select a backup file to restore")
            .setSingleChoiceItems(fileNames, checkedItem) { dialog, which ->
                selectedFileId = backupFiles[which].id
            }
            .setPositiveButton("OK") { dialog, id ->
                // User clicked OK, so save the selected file ID and close the dialog
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, id ->
                // User cancelled the dialog, so clear the selected file ID and close the dialog
                selectedFileId = ""
                dialog.cancel()
            }
        builder.create().show()

        return selectedFileId
    }

    private suspend fun getJsonStringFromGoogleDrive(fileId: String): String = withContext(Dispatchers.IO) {
        val outputStream = ByteArrayOutputStream()
        driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
        return@withContext outputStream.toString(Charsets.UTF_8.name())
    }

    private suspend fun getConversationDataAsJson(): String {
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
        val exportData = ExportData(conversations = conversations)
        val jsonString = moshi.adapter(ExportData::class.java).toJson(exportData)
        return jsonString
    }

    private suspend fun updateConversationDataFromJson(jsonString: String) {
        val exportData = moshi.adapter(ExportData::class.java).fromJson(jsonString) ?: return
        exportData.conversations.forEach { importedConversation ->
            // Update the conversation data in the app
            withContext(Dispatchers.IO) {
                conversationRepository.saveConversation(importedConversation)
            }
        }
    }
}
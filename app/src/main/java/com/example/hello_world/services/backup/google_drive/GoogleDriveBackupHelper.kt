package com.example.hello_world.services.backup.google_drive

import android.util.Log
import android.app.Activity
import android.content.Intent
import com.example.hello_world.data.repository.IConversationRepository
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
import java.util.*


class GoogleDriveBackupHelper(
    private val activity: Activity,
) : IBackup {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var driveService: Drive
    fun authenticate() {
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
    }


    fun handleSignInResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                // Signed in successfully, you can now access the Google Drive API using the account object
                Log.d("GoogleDriveHelper", "Signed in successfully: ${account?.displayName}, email: ${account?.email}")
                setupDriveService(account)
                createHelloWorldFolderAndFile()
            } catch (e: ApiException) {
                Log.w("GoogleDriveHelper", "signInResult:failed code=" + e.statusCode)
                Log.w("GoogleDriveHelper", "signInResult:status=" + e.status)
                Log.w("GoogleDriveHelper", "signInResult:message=" + e.message)
            }
        } else {
            Log.w("GoogleDriveHelper", "signInResult:resultCode not OK, resultCode=$resultCode")
        }
    }

    companion object {
        const val RC_SIGN_IN = 9001
        const val REQUEST_AUTHORIZATION = 9002
    }

    fun createFolder() {
        Log.d("GoogleDriveHelper", "TODO: Create a dedicated folder in Google Drive")
    }

    override suspend fun exportConversations(): String {
        // TODO: Implement the logic to export conversations to Google Drive
        // You can reuse the logic from LocalBackupHelper.exportConversations() and modify it to work with Google Drive
        return ""
    }

    override suspend fun importConversations(json: String) {
        // TODO: Implement the logic to import conversations from Google Drive
        // You can reuse the logic from LocalBackupHelper.importConversations(json: String) and modify it to work with Google Drive

    }


    private fun setupDriveService(account: GoogleSignInAccount) {
        val credential = GoogleAccountCredential.usingOAuth2(activity, Collections.singleton("https://www.googleapis.com/auth/drive"))
        credential.selectedAccount = account.account
        driveService = Drive.Builder(AndroidHttp.newCompatibleTransport(), GsonFactory(), credential).setApplicationName("Hello World").build()
    }

    private fun createHelloWorldFolderAndFile() {
        CoroutineScope(Dispatchers.IO).launch {
            val folderId = findOrCreateHelloWorldFolder()
            createHelloWorldFile(folderId)
        }
    }

    private suspend fun findOrCreateHelloWorldFolder(): String = withContext(Dispatchers.IO) {
        val query = "mimeType='application/vnd.google-apps.folder' and trashed=false and name='hello world'"
        return@withContext try {
            val files: FileList = driveService.files().list().setQ(query).execute()
            if (files.files.isNotEmpty()) {
                files.files[0].id
            } else {
                createHelloWorldFolder()
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

    private suspend fun createHelloWorldFile(folderId: String) = withContext(Dispatchers.IO) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileMetadata = File()
        fileMetadata.name = "$timestamp.txt"
        fileMetadata.parents = Collections.singletonList(folderId)
        val content = "hello world"
        val fileContent = ByteArrayInputStream(content.toByteArray(Charsets.UTF_8))
        val mediaContent = InputStreamContent("text/plain", fileContent)
        driveService.files().create(fileMetadata, mediaContent).setFields("id").execute()
    }

}
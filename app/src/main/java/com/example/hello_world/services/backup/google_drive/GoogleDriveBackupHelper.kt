package com.example.hello_world.services.backup.google_drive

import android.util.Log
import android.app.Activity
import android.content.Intent
import com.example.hello_world.data.repository.IConversationRepository
import com.example.hello_world.services.backup.IBackup
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class GoogleDriveBackupHelper(
    private val activity: Activity,
) : IBackup {
    private lateinit var googleSignInClient: GoogleSignInClient
    fun authenticate() {
        Log.d("GoogleDriveHelper", "authenticate() called")
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
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


}
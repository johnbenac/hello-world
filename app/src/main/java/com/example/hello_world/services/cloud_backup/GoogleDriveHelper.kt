package com.example.hello_world.services.cloud_backup

import android.util.Log
import android.app.Activity
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class GoogleDriveHelper(private val activity: Activity) {
    private lateinit var googleSignInClient: GoogleSignInClient
    fun authenticate() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(activity, gso)

        val signInIntent = googleSignInClient.signInIntent
        activity.startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                // Signed in successfully, you can now access the Google Drive API using the account object
                Log.d("GoogleDriveHelper", "Signed in successfully: ${account?.displayName}")
            } catch (e: ApiException) {
                Log.w("GoogleDriveHelper", "signInResult:failed code=" + e.statusCode)
            }
        }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    fun createFolder() {
        Log.d("GoogleDriveHelper", "TODO: Create a dedicated folder in Google Drive")
    }

    fun backup() {
        Log.d("GoogleDriveHelper", "TODO: Upload JSON (for conversation text) and audio files (associated with specific message cards) to Google Drive")
    }

    fun restore() {
        Log.d("GoogleDriveHelper", "TODO: Restore JSON backup and associated audio files from Google Drive")
    }


}
package com.example.hello_world.ui.google_drive_backup.viewmodel

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hello_world.services.backup.google_drive.GoogleDriveBackupHelper
import com.example.hello_world.services.backup.google_drive.SignInActivity
import kotlinx.coroutines.launch

class GoogleDriveBackupViewModel(private val activity: Activity) : ViewModel() {
    private val googleDriveBackupHelper = GoogleDriveBackupHelper(activity)

    fun authenticate() {
        Log.d("GoogleDriveBackupVM", "authenticate() called")
        val signInIntent = Intent(activity, SignInActivity::class.java)
        signInIntent.putExtra("action", "authenticate")
        activity.startActivity(signInIntent)
    }

    fun restore() {
        Log.d("GoogleDriveBackupVM", "restore() called")
        val signInIntent = Intent(activity, SignInActivity::class.java)
        signInIntent.putExtra("action", "restore")
        activity.startActivity(signInIntent)
    }
}
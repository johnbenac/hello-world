package com.example.hello_world.ui.google_drive_backup.viewmodel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.hello_world.services.backup.google_drive.GoogleDriveBackupHelper

class GoogleDriveBackupViewModel(private val activity: Activity) : ViewModel() {
    private val googleDriveBackupHelper = GoogleDriveBackupHelper(activity)

    fun authenticate() {
        Log.d("GoogleDriveBackupVM", "authenticate() called")
        googleDriveBackupHelper.authenticate()
    }

    fun backup() {
        Log.d("GoogleDriveBackupVM", "backup() called")
        googleDriveBackupHelper.createFolder()
//        googleDriveBackupHelper.exportConversations() //Suspend function 'exportConversations' should be called only from a coroutine or another suspend function
    }

    fun restore() {
        Log.d("GoogleDriveBackupVM", "restore() called")
//        googleDriveBackupHelper.importConversations("") // Suspend function 'importConversations' should be called only from a coroutine or another suspend function

    }


}
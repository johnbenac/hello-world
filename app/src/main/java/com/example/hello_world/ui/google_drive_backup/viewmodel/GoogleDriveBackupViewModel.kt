package com.example.hello_world.ui.google_drive_backup.viewmodel

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.hello_world.services.cloud_backup.GoogleDriveHelper

class GoogleDriveBackupViewModel(private val activity: Activity) : ViewModel() {
    private val googleDriveHelper = GoogleDriveHelper(activity)

    fun authenticate() {
        Log.d("GoogleDriveBackupVM", "authenticate() called")
        googleDriveHelper.authenticate()
    }

    fun backup() {
        Log.d("GoogleDriveBackupVM", "backup() called")
        googleDriveHelper.createFolder()
        googleDriveHelper.backup()
    }

    fun restore() {
        Log.d("GoogleDriveBackupVM", "restore() called")
        googleDriveHelper.restore()
    }

//    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        Log.d("GoogleDriveBackupVM", "onActivityResult() called")
//        googleDriveHelper.onActivityResult(requestCode, resultCode, data)
//    }
}
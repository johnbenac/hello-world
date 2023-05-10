package com.example.hello_world.ui.google_drive_backup.viewmodel

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.example.hello_world.services.cloud_backup.GoogleDriveHelper

class GoogleDriveBackupViewModel(private val activity: Activity) : ViewModel() {
    private val googleDriveHelper = GoogleDriveHelper(activity)

    fun authenticate() {
        googleDriveHelper.authenticate()
    }

    fun backup() {
        googleDriveHelper.backup()
        googleDriveHelper.createFolder()
    }

    fun restore() {
        googleDriveHelper.restore()
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        googleDriveHelper.onActivityResult(requestCode, resultCode, data)
    }
}
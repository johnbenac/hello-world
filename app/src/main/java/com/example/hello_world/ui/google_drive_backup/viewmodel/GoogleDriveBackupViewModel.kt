package com.example.hello_world.ui.google_drive_backup.viewmodel

import androidx.lifecycle.ViewModel
import com.example.hello_world.services.cloud_backup.GoogleDriveHelper

class GoogleDriveBackupViewModel : ViewModel() {
    private val googleDriveHelper = GoogleDriveHelper()

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
}
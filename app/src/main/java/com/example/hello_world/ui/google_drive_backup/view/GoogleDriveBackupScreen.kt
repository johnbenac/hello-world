package com.example.hello_world.ui.google_drive_backup.view

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hello_world.ui.google_drive_backup.viewmodel.GoogleDriveBackupViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text

@Composable
fun GoogleDriveBackupScreen() {
    val viewModel = viewModel<GoogleDriveBackupViewModel>()

    Column {
        Button(onClick = { viewModel.authenticate() }) {
            Text("Authenticate with Google")
        }
        Button(onClick = { viewModel.createFolder() }) {
            Text("Create Folder")
        }
        Button(onClick = { viewModel.backup() }) {
            Text("Backup")
        }
        Button(onClick = { viewModel.restore() }) {
            Text("Restore")
        }
    }
}
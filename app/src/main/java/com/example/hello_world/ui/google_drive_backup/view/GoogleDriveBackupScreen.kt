package com.example.hello_world.ui.google_drive_backup.view

import android.app.Activity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hello_world.ui.google_drive_backup.viewmodel.GoogleDriveBackupViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Composable
fun GoogleDriveBackupScreen(activityResultRegistry: ActivityResultRegistry) {
    val context = LocalContext.current
    val activity = context as? Activity ?: return
    val viewModel = viewModel<GoogleDriveBackupViewModel>(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GoogleDriveBackupViewModel(activity) as T
        }
    })
    DisposableEffect(Unit) {
        val activityResult = activityResultRegistry.register("signInResult", ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            viewModel.onActivityResult(result.resultCode, result.resultCode, result.data)
        }
        onDispose {
            activityResult.unregister()
        }
    }

    Column {
        Button(onClick = { viewModel.authenticate() }) {
            Text("Authenticate with Google")
        }
        Button(onClick = { viewModel.backup() }) {
            Text("Backup")
        }
        Button(onClick = { viewModel.restore() }) {
            Text("Restore")
        }
    }
}
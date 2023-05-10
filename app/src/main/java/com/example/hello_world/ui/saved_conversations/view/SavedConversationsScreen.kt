package com.example.hello_world

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.hello_world.models.Conversation
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
//import androidx.compose.material.CardElevation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewModelScope
import com.example.hello_world.ui.saved_conversations.viewmodel.SavedConversationsViewModel
import kotlinx.coroutines.launch


@Composable
@ExperimentalMaterial3Api
@OptIn(ExperimentalMaterialApi::class)
fun SavedConversationsScreen(
    viewModel: SavedConversationsViewModel,
    onConversationSelected: (UUID) -> Unit,
    onBack: () -> Unit,
    onNewConversationClicked: () -> Unit
) {
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            Log.d("SavedConversationsScreen", "Permission granted, calling exportConversations()")
            // Call exportConversations() here
            viewModel.viewModelScope.launch {
                viewModel.exportConversations()
            }
        } else {
            Log.d("SavedConversationsScreen", "Permission denied")
            // Show a message to the user that the permission is required
//            Toast.makeText(context, "Permission is required to export conversations", Toast.LENGTH_SHORT).show()
        }
    }


    val context = LocalContext.current // Move this line outside the rememberLauncherForActivityResult block
    val filePickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val contentResolver = context.contentResolver
            val json = contentResolver.openInputStream(uri)?.bufferedReader().use { it?.readText() }
            if (json != null) {
                viewModel.viewModelScope.launch {
                    viewModel.importConversations(json)
                }
            }
        }
    }
    val savedConversations by viewModel.savedConversations.collectAsState(initial = emptyList<Conversation>())
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Conversations") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNewConversationClicked) {

                        Icon(Icons.Default.Add, contentDescription = "New Conversation")
                    }
                    IconButton(onClick = {
                        Log.d("SavedConversationsScreen", "Export button clicked, requesting WRITE_EXTERNAL_STORAGE permission")
                        permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        Log.d("SavedConversationsScreen", "Export button clicked, calling exportConversations()")
                        viewModel.viewModelScope.launch {
                            viewModel.exportConversations()
                        }
                    }) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Export Conversations")
                    }
                    IconButton(onClick = {
                        filePickerLauncher.launch("*/*")
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Import Conversations")
                    }
                }
            )
        }
    ) {
        Column {
            Spacer(modifier = Modifier.height(56.dp)) // Add Spacer with the same height as the TopAppBar

            if (savedConversations.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No saved conversations")
                }
            } else {
                LazyColumn(modifier = Modifier.padding(8.dp)) {
                    items(savedConversations.size) { index ->
                        val conversation = savedConversations[index]
                        ConversationCard(
                            conversation = conversation,
                            onClick = {
                                Log.d("SavedConversationsScreen", "Selected conversation ID: ${conversation.id}")
                                onConversationSelected(conversation.id)
                            },
                            onDeleteClicked = { viewModel.deleteConversation(conversation.id) }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun CardElevation(
    modifier: Modifier = Modifier,
    elevation: Dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.graphicsLayer(
            shadowElevation = elevation.value,
            shape = RoundedCornerShape(4.dp),
            clip = true
        ),
        content = content
    )
}

@Composable
fun ConversationCard(
    conversation: Conversation,
    onClick: () -> Unit,
    onDeleteClicked: () -> Unit
) {
    val showDeleteDialog = remember { mutableStateOf(false) }

    if (showDeleteDialog.value) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog.value = false },
            title = { Text("Delete Conversation") },
            text = { Text("Are you sure you want to delete this conversation? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClicked()
                        showDeleteDialog.value = false
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog.value = false }
                ) {
                    Text("No")
                }
            }
        )
    }

    CardElevation(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(conversation.title)
            Text("Date started: ${formatDate(conversation.dateStarted)}")
            Text("Date last saved: ${formatDate(conversation.dateLastSaved)}")
            Text("Message count: ${conversation.messageCount}")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { showDeleteDialog.value = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

fun formatDate(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy, HH:mm", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}
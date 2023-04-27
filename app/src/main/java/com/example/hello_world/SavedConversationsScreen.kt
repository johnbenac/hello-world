package com.example.hello_world

import com.example.hello_world.Conversation
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButtonDefaults.elevation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
//import androidx.compose.material.CardElevation
import androidx.compose.material3.CardElevation
import androidx.compose.runtime.remember
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.graphicsLayer

@Composable
@ExperimentalMaterial3Api
@OptIn(ExperimentalMaterialApi::class)
fun SavedConversationsScreen(
    viewModel: SavedConversationsViewModel,
    onConversationSelected: (UUID) -> Unit,
    onBack: () -> Unit
) {
    val savedConversations by viewModel.savedConversations.collectAsState(initial = emptyList<Conversation>())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Conversations") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) {
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
                        onClick = { onConversationSelected(conversation.id) },
                        onDeleteClicked = { viewModel.deleteConversation(conversation.id) }
                    )
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
                IconButton(onClick = onDeleteClicked) {
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
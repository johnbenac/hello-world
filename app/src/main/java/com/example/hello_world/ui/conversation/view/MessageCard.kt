package com.example.hello_world

import com.example.hello_world.models.ConversationMessage
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.hello_world.services.media_playback.MediaPlaybackManager
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


@Composable
@ExperimentalMaterial3Api
@OptIn(ExperimentalMaterialApi::class)
fun MessageCard(
    message: ConversationMessage,
    onPlayAudio: (String) -> Unit,
    onCardClicked: () -> Unit,
    mediaPlaybackManager: MediaPlaybackManager,
    context: Context,
    onDeleteClicked: () -> Unit,
    onEditClicked: (ConversationMessage, String) -> Unit,
    onShareClicked: (String, Uri?) -> Unit // Add this parameter
) {
    val isPlayingState = remember { mutableStateOf(false) }
    var isPlaying: Boolean by object : ReadWriteProperty<Any?, Boolean> {
        override fun getValue(thisRef: Any?, property: KProperty<*>) = isPlayingState.value
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) { isPlayingState.value = value }
    }
    val isEditing = remember { mutableStateOf(false) }
    val editedMessage = remember { mutableStateOf(message.message) }
//    Log.d("MessageCard", "Message: $message")
    Card(
        modifier = Modifier
            .clickable { onCardClicked() }
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(text = message.sender, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            if (isEditing.value) {
                TextField(
                    value = editedMessage.value,
                    onValueChange = { editedMessage.value = it },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                SelectionContainer {
                    Text(text = message.message)
                }
            }
            if (isEditing.value) {
                Row {
                    Button(
                        onClick = {
                            onEditClicked(message, editedMessage.value)
                            isEditing.value = false
                        }
                    ) {
                        Text("Save")
                    }
                    Button(
                        onClick = {
                            isEditing.value = false
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp)) // Add a spacer to add some space between the message and the media controls
            Row { // Add this row
                IconButton(onClick = { isEditing.value = !isEditing.value }) {
                    Icon(Icons.Filled.Create, contentDescription = "Edit message")
                }
                IconButton(onClick = onDeleteClicked) {
                    Icon(Icons.Filled.Close, contentDescription = "Delete message")
                }
                IconButton(onClick = { onShareClicked(message.message, message.audioFilePath.value.toUriOrNull()) }) {
                    Icon(Icons.Filled.Share, contentDescription = "Share message")
                }
            }
            MediaControls( // Show the media controls
                onPlayPause = {
                    Log.d("MessageCard", "isPlaying: $isPlaying")
                    if (isPlaying) {
                        mediaPlaybackManager.pause()
                        mediaPlaybackManager.storePlaybackPosition()
                    } else {
                        if (mediaPlaybackManager.isPlaying()) {
                            mediaPlaybackManager.pause()
                            mediaPlaybackManager.resetPlaybackPosition()
                        }
                        mediaPlaybackManager.playAudio(message.audioFilePath.value, context)
                    }
                    isPlaying = !isPlaying
                },
                onSeekForward = { mediaPlaybackManager.seekForward() }, // Pass the seekForward callback
                onSeekBackward = { mediaPlaybackManager.seekBackward() } // Pass the seekBackward callback
            )
        }
    }
}

fun String.toUriOrNull(): Uri? {
    return if (this.isNotBlank()) {
        Uri.parse(this)
    } else {
        null
    }
}
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
    val isEditing = remember { mutableStateOf(false) }
    val editedMessage = remember { mutableStateOf(message.message) }
//    Log.d("MessageCard", "Message: $message")
    Card( // Create a card for the message
        modifier = Modifier // Set the modifier for the card
            .clickable { onCardClicked() } //the card is clickable!
            .padding(8.dp) // Add padding to the card
            .fillMaxWidth() // Make the card fill the width of the screen
    ) {
        Column( // Create a column for the message
            modifier = Modifier // Set the modifier for the column
                .padding(16.dp) // Add padding to the column
        ) {
            Text(text = message.sender, fontWeight = FontWeight.Bold) // Show the sender of the message
            Spacer(modifier = Modifier.height(4.dp)) // Add a spacer to add some space between the sender and the message
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
            if (isEditing.value) { // add this line
                Row { // add this line
                    Button( // add this line
                        onClick = { // add this line
                            onEditClicked(message, editedMessage.value) // add this line
                            isEditing.value = false // add this line
                        } // add this line
                    ) { // add this line
                        Text("Save") // add this line
                    } // add this line
                    Button( // add this line
                        onClick = { // add this line
                            isEditing.value = false // add this line
                        } // add this line
                    ) { // add this line
                        Text("Cancel") // add this line
                    } // add this line
                } // add this line
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
                onPlayPause = { // When the play/pause button is pressed
                    if (mediaPlaybackManager.isPlaying()) {
                        Log.d("MessageCard", "Pausing audio from file: ${message.audioFilePath.value}")
                        mediaPlaybackManager.pause()
                    } else {
                        Log.d("MessageCard", "Resuming audio from file: ${message.audioFilePath.value}")
                        mediaPlaybackManager.playAudio(message.audioFilePath.value, context)
                    }
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
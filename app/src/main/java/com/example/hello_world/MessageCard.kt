
package com.example.hello_world

import ConversationMessage
import android.content.Context
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

@Composable
fun MessageCard( // Composable for the message card
    message: ConversationMessage, // The message to show
    onPlayAudio: (String) -> Unit, // Function to call when the play audio button is pressed
    onCardClicked: () -> Unit, // this is what it does if you click on the card
    mediaPlaybackManager: MediaPlaybackManager,
    context: Context,
    onDeleteClicked: () -> Unit,
    onEditClicked: () -> Unit
) {
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
            Text(text = message.message) // Show the message
            Spacer(modifier = Modifier.height(8.dp)) // Add a spacer to add some space between the message and the media controls
            Row { // Add this row
                IconButton(onClick = onEditClicked) {
                    Icon(Icons.Filled.Create, contentDescription = "Edit message")
                }
                IconButton(onClick = onDeleteClicked) {
                    Icon(Icons.Filled.Close, contentDescription = "Delete message")
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
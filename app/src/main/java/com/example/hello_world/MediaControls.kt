package com.example.hello_world


import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


@Composable
fun MediaControls(
    onPlayPause: () -> Unit, // Function to call when the play/pause button is pressed
    onSeekForward: () -> Unit, // Function to call when the seek forward button is pressed
    onSeekBackward: () -> Unit // Function to call when the seek backward button is pressed
) {
    var isPlaying by remember { mutableStateOf(false) } // Add the state variable isPlaying

    Row {
        IconButton(onClick = {
            onPlayPause() // Call the onPlayPause function
            isPlaying = !isPlaying // Toggle the isPlaying state
        }) {
            if (isPlaying) {
                Icon(Icons.Filled.AccountBox, contentDescription = "Pause") // Show the pause icon
            } else {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Play") // Show the play icon
            }
        }
        IconButton(onClick = onSeekBackward) { // Create a button for the seek backward button
            Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Seek Backward") // Show the seek backward icon
        }
        IconButton(onClick = onSeekForward) { // Create a button for the seek forward button
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Seek Forward") // Show the seek forward icon
        }

    }
}
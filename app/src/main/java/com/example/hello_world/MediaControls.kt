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


@Composable
fun MediaControls( // Composable for the media controls
    onPlay: () -> Unit, // Function to call when the play button is pressed
    onPause: () -> Unit, // Function to call when the pause button is pressed
    onSeekForward: () -> Unit, // Function to call when the seek forward button is pressed
    onSeekBackward: () -> Unit // Function to call when the seek backward button is pressed
) {
    Row {
        IconButton(onClick = onPlay) { // Create a button for the play button
            Icon(Icons.Filled.PlayArrow, contentDescription = "Play") // Show the play icon
        }
        IconButton(onClick = onPause) { // Create a button for the pause button
            Icon(Icons.Filled.AccountBox, contentDescription = "Pause") // Show the pause icon
        }
        IconButton(onClick = onSeekForward) { // Create a button for the seek forward button
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Seek Forward") // Show the seek forward icon
        }
        IconButton(onClick = onSeekBackward) { // Create a button for the seek backward button
            Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Seek Backward") // Show the seek backward icon
        }
    }
}
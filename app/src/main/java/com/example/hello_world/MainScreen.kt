package com.example.hello_world

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun MainScreen( // Composable for the main screen. This is the main screen of the app
    mainViewModel: MainViewModel, // The main view model
    settingsViewModel: SettingsViewModel, // The settings view model
    onSettingsClicked: () -> Unit, // Function to call when the settings button is pressed
    textToSpeechServiceState: MutableState<TextToSpeechService>,
    mediaPlaybackManager: MediaPlaybackManager
) {
    val context = LocalContext.current // Get the current context
    val scrollToBottomClicked = remember { mutableStateOf(false) }
    BoxWithConstraints( // Create a box with constraints to get the maximum height of the screen
        modifier = Modifier // Set the modifier for the box
            .fillMaxSize() // Make the box fill the entire screen
            .padding(16.dp) // Add padding to the box
    ) {

        val lazyListState = rememberLazyListState() // Create a lazy list state for the lazy column

        val messages = mainViewModel.conversationMessages // Get the conversation messages
        Log.d("MainScreen", "Number of messages: ${messages.size}")
        LaunchedEffect(Unit) {
            if (scrollToBottomClicked.value) {
                Log.d("MainScreen", "LaunchedEffect triggered")
                val targetIndex = messages.size - 1
                Log.d("MainScreen", "Target index for scrolling: $targetIndex")
                try {
                    lazyListState.animateScrollToItem(targetIndex)
                    Log.d("MainScreen", "animateScrollToItem to item number $targetIndex")
                } catch (e: Exception) {
                    Log.e("MainScreen", "Error while animating scroll to item", e)
                }
                scrollToBottomClicked.value = false
            }
        }
        val maxHeight = constraints.maxHeight // Get the maximum height of the screen
        Column(modifier = Modifier.fillMaxSize()) { // Create a column for the main screen
            LazyColumn( // Create a lazy column for the messages
                modifier = Modifier // Set the modifier for the lazy column
                    .weight(1f) // Make the lazy column fill the entire screen
                    .height(((maxHeight.dp - 64.dp).coerceAtLeast(0.dp))) // Set the height of the lazy column to the maximum height of the screen minus the height of the buttons
            ) {
                items(messages) { message -> // For each message in the conversation messages
                    MessageCard(
                        message = message,
                        onPlayAudio = { audioFilePath ->
                            mainViewModel.mediaPlaybackManager.playAudio(audioFilePath, context)
                        },
                        onCardClicked = {
                            // Implement the functionality that should happen when the card is clicked
                            Log.d("MainScreen", "Card with index ${messages.indexOf(message)} clicked")
                        },mainViewModel.mediaPlaybackManager,context
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp)) // Add a spacer to add some space between the messages and the buttons
            Text( // Show the listening status
                text = if (mainViewModel.isListening) "Listening..." else "Not Listening",  // Show "Listening..." if the app is listening and "Not Listening" if the app is not listening
                modifier = Modifier.align(Alignment.CenterHorizontally) // Align the text to the center horizontally
            )
            Spacer(modifier = Modifier.height(16.dp)) // Add a spacer to add some space between the listening status and the buttons
            Button(
                onClick = { // When the start listening button is pressed
                    if (textToSpeechServiceState.value is AndroidTextToSpeechService) { // If the text to speech service is the Android text to speech service
                        textToSpeechServiceState.value = ElevenLabsTextToSpeechService("82b94d982c1018cb379c0acb629d473c", "TxGEqnHWrfWFTfGW9XjX", context, mediaPlaybackManager) { mainViewModel.startListening() }  // Set the text to speech service to the Eleven Labs text to speech service
                    } else { // If the text to speech service is not the Android text to speech service
                        textToSpeechServiceState.value = AndroidTextToSpeechService(context, mediaPlaybackManager) { mainViewModel.startListening() } // Set the text to speech service to the Android text to speech service
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally) // Align the button to the center horizontally
            ) {
                Text(if (textToSpeechServiceState.value is AndroidTextToSpeechService) "Use Eleven Labs TTS" else "Use Google TTS") // Show "Use Eleven Labs TTS" if the text to speech service is the Android text to speech service and "Use Google TTS" if the text to speech service is not the Android text to speech service
            }
            Button( // Create a button for the start listening button
                onClick = { // When the start listening button is pressed
                    if (mainViewModel.isListening) {  // If the app is listening
                        Log.d("MainScreen", "Stop Listening button clicked")  // Log that the stop listening button was clicked
                        mainViewModel.stopListening() // Stop listening
                    } else {
                        Log.d("MainScreen", "Start Listening button clicked") // Log that the start listening button was clicked
                        mainViewModel.startListening() // Start listening
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally) // Align the button to the center horizontally
            ) {
                Text(if (mainViewModel.isListening) "Stop Listening" else "Start Listening")  // Show "Stop Listening" if the app is listening and "Start Listening" if the app is not listening
            }
            Button( // Create a button for the settings button
                onClick = onSettingsClicked, // When the settings button is pressed
                modifier = Modifier.align(Alignment.CenterHorizontally) // Align the button to the center horizontally
            ) {
                Text("Settings") // Show "Settings"
            }
            Button(
                onClick = {
                    scrollToBottomClicked.value = true
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Scroll to Bottom")
            }
        }
    }
}
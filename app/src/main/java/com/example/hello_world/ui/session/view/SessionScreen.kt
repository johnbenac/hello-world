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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.example.hello_world.services.media_playback.MediaPlaybackManager
import com.example.hello_world.services.text_to_speech.AndroidTextToSpeechService
import com.example.hello_world.services.text_to_speech.ElevenLabsTextToSpeechService
import com.example.hello_world.services.text_to_speech.TextToSpeechService
import com.example.hello_world.ui.session.viewmodel.SessionViewModel
import com.example.hello_world.ui.settings.viewmodel.SettingsViewModel


@Composable
@ExperimentalMaterial3Api
@OptIn(ExperimentalMaterialApi::class)
fun SessionScreen(
    sessionViewModel: SessionViewModel,
    settingsViewModel: SettingsViewModel,
    onSettingsClicked: () -> Unit,
    textToSpeechServiceState: MutableState<TextToSpeechService>,
    mediaPlaybackManager: MediaPlaybackManager,
    navController: NavController,
    snackbarHostState: SnackbarHostState
) {
    LaunchedEffect(sessionViewModel) {
        sessionViewModel.conversationId?.let {
            sessionViewModel.loadConversation(it)
        }
    }
    val context = LocalContext.current // Get the current context
    val scrollToBottomClicked = remember { mutableStateOf(false) } // Create a mutable state for the scroll to bottom button
    val conversationTextState = remember { mutableStateOf("") }
    BoxWithConstraints( // Create a box with constraints to get the maximum height of the screen
        modifier = Modifier // Set the modifier for the box
            .fillMaxSize() // Make the box fill the entire screen
            .padding(16.dp) // Add padding to the box
    ) {

        val lazyListState = rememberLazyListState() // Create a lazy list state for the lazy column

        val messages = sessionViewModel.conversationMessages // Get the conversation messages
        Log.d("SessionScreen", "Number of messages in session screen: ${messages.size}")
        LaunchedEffect(Unit) {
            if (scrollToBottomClicked.value) {
                val targetIndex = messages.size - 1
                try {
                    lazyListState.animateScrollToItem(targetIndex)
                } catch (e: Exception) {
                    Log.e("SessionScreen", "Error while animating scroll to item", e)
                }
                scrollToBottomClicked.value = false
            }
            Log.d("SessionScreen", "Current messages in session screen: $messages")
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
                            sessionViewModel.mediaPlaybackManager.playAudio(audioFilePath, context)
                        },
                        onCardClicked = {
                            Log.d("SessionScreen", "Card with index ${messages.indexOf(message)} clicked")
                        },
                        mediaPlaybackManager = mediaPlaybackManager,
                        context = context,
                        onDeleteClicked = {
                            // Log the delete action and message index
                            Log.d("SessionScreen", "Delete button clicked for message at index ${messages.indexOf(message)}")
                            // Call the deleteMessage method from MainViewModel
                            sessionViewModel.deleteMessage(messages.indexOf(message))
                        },
                        onEditClicked = { message, editedMessage ->
                            val index = messages.indexOf(message)
                            sessionViewModel.updateMessage(index, message.copy(message = editedMessage))
                            // Log the edit action and message index
                            Log.d("SessionScreen", "Edit button clicked for message at index ${messages.indexOf(message)}")
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp)) // Add a spacer to add some space between the messages and the buttons
            Text( // Show the listening status
                text = if (sessionViewModel.isListening) "Listening..." else "Not Listening",  // Show "Listening..." if the app is listening and "Not Listening" if the app is not listening
                modifier = Modifier.align(Alignment.CenterHorizontally) // Align the text to the center horizontally
            )
            Spacer(modifier = Modifier.height(16.dp)) // Add a spacer to add some space between the listening status and the buttons
            Button(
                onClick = { // When the start listening button is pressed
                    if (textToSpeechServiceState.value is AndroidTextToSpeechService) { // If the text to speech service is the Android text to speech service
                        textToSpeechServiceState.value = ElevenLabsTextToSpeechService("82b94d982c1018cb379c0acb629d473c", "TxGEqnHWrfWFTfGW9XjX", context, mediaPlaybackManager) { sessionViewModel.startListening() }  // Set the text to speech service to the Eleven Labs text to speech service
                    } else { // If the text to speech service is not the Android text to speech service
                        textToSpeechServiceState.value = AndroidTextToSpeechService(context, mediaPlaybackManager) { sessionViewModel.startListening() } // Set the text to speech service to the Android text to speech service
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally) // Align the button to the center horizontally
            ) {
                Text(if (textToSpeechServiceState.value is AndroidTextToSpeechService) "Use Eleven Labs TTS" else "Use Google TTS") // Show "Use Eleven Labs TTS" if the text to speech service is the Android text to speech service and "Use Google TTS" if the text to speech service is not the Android text to speech service
            }
            Button( // Create a button for the start listening button
                onClick = { // When the start listening button is pressed
                    if (sessionViewModel.isListening) {  // If the app is listening
                        Log.d("SessionScreen", "Stop Listening button clicked")  // Log that the stop listening button was clicked
                        sessionViewModel.stopListening() // Stop listening
                    } else {
                        Log.d("SessionScreen", "Start Listening button clicked") // Log that the start listening button was clicked
                        sessionViewModel.startListening() // Start listening
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally) // Align the button to the center horizontally
            ) {
                Text(if (sessionViewModel.isListening) "Stop Listening" else "Start Listening")  // Show "Stop Listening" if the app is listening and "Start Listening" if the app is not listening
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
            Button(
                onClick = { navController.navigate("sessions") },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Saved Conversations")
            }
            Button(
                onClick = { sessionViewModel.saveCurrentConversation() },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Save Conversation")
            }
            Button(
                onClick = {
                    val conversationText = sessionViewModel.conversationMessages.joinToString("\n") { it.message }
                    conversationTextState.value = conversationText
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Share Conversation Text")
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
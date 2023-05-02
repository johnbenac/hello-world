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
import androidx.compose.runtime.DisposableEffect
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
    DisposableEffect(Unit) {

        Log.d("SessionScreen", "sessionViewModel.conversationId: ${sessionViewModel.conversationId}")
        sessionViewModel.conversationId?.let {
            Log.d("SessionScreen", "Before `sessionViewModel.loadConversation(it)` within the sessionViewModel.conversationId?.let {/.../} block ")
            sessionViewModel.loadConversation(it)
            Log.d("SessionScreen", "After `sessionViewModel.loadConversation(it)` within the sessionViewModel.conversationId?.let {/.../} block ")
        }
        onDispose { }
    }
    val context = LocalContext.current
    val scrollToBottomClicked = remember { mutableStateOf(false) } // Create a mutable state for the scroll to bottom button
    val conversationTextState = remember { mutableStateOf("") }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        val lazyListState = rememberLazyListState()

        val messages = sessionViewModel.conversationMessages
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
        val maxHeight = constraints.maxHeight
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .height(((maxHeight.dp - 64.dp).coerceAtLeast(0.dp))) // Set the height of the lazy column to the maximum height of the screen minus the height of the buttons
            ) {
                items(messages) { message ->
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
                            Log.d("SessionScreen", "Delete button clicked for message at index ${messages.indexOf(message)}")
                            sessionViewModel.deleteMessage(messages.indexOf(message))
                        },
                        onEditClicked = { message, editedMessage ->
                            val index = messages.indexOf(message)
                            sessionViewModel.updateMessage(index, message.copy(message = editedMessage))
                            Log.d("SessionScreen", "Edit button clicked for message at index ${messages.indexOf(message)}")
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (sessionViewModel.isListening) "Listening..." else "Not Listening",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (textToSpeechServiceState.value is AndroidTextToSpeechService) {
                        textToSpeechServiceState.value = ElevenLabsTextToSpeechService(
                            apiKey = "82b94d982c1018cb379c0acb629d473c",
                            voiceId = "TxGEqnHWrfWFTfGW9XjX",
                            context = context,
                            mediaPlaybackManager = mediaPlaybackManager,
                            onPlaybackFinished = { sessionViewModel.startListening() },
                            snackbarHostState = snackbarHostState // Pass snackbarHostState here
                        )
                    } else {
                        textToSpeechServiceState.value = AndroidTextToSpeechService(
                            context = context,
                            mediaPlaybackManager = mediaPlaybackManager,
                            onPlaybackFinished = { sessionViewModel.startListening() }
                        )
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(if (textToSpeechServiceState.value is AndroidTextToSpeechService) "Use Eleven Labs TTS" else "Use Google TTS")
            }
            Button(
                onClick = {
                    if (sessionViewModel.isListening) {
                        Log.d("SessionScreen", "Start/Stop Listening button clicked when sessionViewModel.isListening is true, before the sessionViewModel.startListening() line in the lambda function, isListening: ${sessionViewModel.isListening}, instance: ${sessionViewModel}, memory location: ${System.identityHashCode(sessionViewModel)}")
                        sessionViewModel.stopListening()
                        Log.d("SessionScreen", "Start/Stop Listening button clicked when sessionViewModel.isListening is true, after the sessionViewModel.startListening() line in the lambda function, isListening: ${sessionViewModel.isListening}, instance: ${sessionViewModel}, memory location: ${System.identityHashCode(sessionViewModel)}")
                    } else {
                        Log.d("SessionScreen", "Start/Stop Listening button clicked when sessionViewModel.isListening is false, before the sessionViewModel.startListening() line in the lambda function, isListening: ${sessionViewModel.isListening}, instance: ${sessionViewModel}, memory location: ${System.identityHashCode(sessionViewModel)}")
                        sessionViewModel.startListening()
                        Log.d("SessionScreen", "Start/Stop Listening button clicked when sessionViewModel.isListening is false, after the sessionViewModel.startListening() line in the lambda function, isListening: ${sessionViewModel.isListening}, instance: ${sessionViewModel}, memory location: ${System.identityHashCode(sessionViewModel)}")
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(if (sessionViewModel.isListening) "Stop Listening" else "Start Listening")
            }
            Button(
                onClick = onSettingsClicked,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Settings")
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
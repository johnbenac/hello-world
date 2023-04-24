package com.example.hello_world
import ConversationMessage
import EditSettingsScreen
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.MutableState


class MainActivity : AppCompatActivity() {
    private var textToSpeechService: TextToSpeechService? = null // Create a text to speech service
    private lateinit var voiceTriggerDetector: VoiceTriggerDetector // Create a voice trigger detector
    private lateinit var openAiApiService: OpenAiApiService // Create an OpenAI API service
    private val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 1 // Create a request code for requesting audio permission
    private val settingsViewModel = SettingsViewModel() // Create a settings view model
    private lateinit var mainViewModel: MainViewModel // Create an main view model

    override fun onCreate(savedInstanceState: Bundle?) { // Called when the activity is starting
        Log.d("MainActivity", "log: MainActivity opened") // Log that the main activity was opened
        super.onCreate(savedInstanceState) // Call the super class onCreate to complete the creation of activity like the view hierarchy
        requestAudioPermission() // Request audio permission
        val textToSpeechServiceState = mutableStateOf<TextToSpeechService>(AndroidTextToSpeechService(this)) // Create the text to speech service, AndroidTextToSpeechService is the default implementation
        openAiApiService = OpenAiApiService("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", settingsViewModel) // Create the OpenAI API service
        mainViewModel = MainViewModel(textToSpeechServiceState, this, settingsViewModel, openAiApiService) // Create the main view model
        voiceTriggerDetector = mainViewModel.voiceTriggerDetector // Create the voice trigger detector
        setContent { // Set the content of the activity to be the UI defined in the composable function
            val navController = rememberNavController() // Create a nav controller
            NavHost(navController, startDestination = "main") { // Create a nav host
                composable("main") { // Create a composable for the main screen
                    MainScreen(mainViewModel, settingsViewModel, { navController.navigate("settings") }, textToSpeechServiceState) // Show the main screen
                } 
                composable("settings") { // Create a composable for the settings screen
                    SettingsScreen(settingsViewModel, { navController.popBackStack() }, navController) // Show the settings screen
                }
                composable("edit-settings") { // Create a composable for the edit settings screen
                    EditSettingsScreen(settingsViewModel, { navController.popBackStack() }, { navController.popBackStack() }) // Show the edit settings screen
                }
            }
        }
    }

    private fun requestAudioPermission() { // Request audio permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) { // Check if the permission is already granted
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_PERMISSION_REQUEST_CODE) // Request the permission
        }
    }

    override fun onResume() { // When the activity is resumed
        super.onResume() // Call the super class onResume to resume the app
        voiceTriggerDetector.startListening() // Start listening for voice triggers
    }
    override fun onPause() { // When the activity is paused
        super.onPause() // Call the super class onPause to pause the app
        textToSpeechService?.stop() // Stop any ongoing speech
    }

    override fun onDestroy() { // When the activity is destroyed
        super.onDestroy() // Call the super class onDestroy to destroy the app
        textToSpeechService?.shutdown() // Shutdown the text to speech service
    }

    private val conversationMessages = mutableStateListOf<ConversationMessage>() // Create a mutable list of conversation messages
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) { // When the user responds to the permission request
        super.onRequestPermissionsResult(requestCode, permissions, grantResults) // Call the super class onRequestPermissionsResult to handle the permission request
        if (requestCode == RECORD_AUDIO_PERMISSION_REQUEST_CODE) { // Check if the request code is the same as the one we requested
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // Check if the permission was granted
                // Permission was granted
                // Continue with creating the app UI and setting up listeners
            } else {
                // Permission was denied
                // Show a message to the user and close the app
                Toast.makeText(this, "Permission to record audio is required to use this app.", Toast.LENGTH_LONG).show() // Show a toast message to the user
                finish() // Close the app
            }
        }
    }
}

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

@Composable
fun MessageCard( // Composable for the message card
    message: ConversationMessage, // The message to show
    onPlayAudio: (String) -> Unit // Function to call when the play audio button is pressed
) {
    Log.d("MessageCard", "Message: $message") 
    Card( // Create a card for the message
        modifier = Modifier // Set the modifier for the card
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
            MediaControls( // Show the media controls
                onPlay = { // When the play button is pressed
                    Log.d("MessageCard", "Playing audio from file: ${message.audioFilePath.value}") 
                    onPlayAudio(message.audioFilePath.value) // Call the onPlayAudio function with the audio file path
                },
                onPause = { /* Implement pause functionality in MainViewModel and pass the callback here */ }, // When the pause button is pressed
                onSeekForward = { /* Implement seek forward functionality in MainViewModel and pass the callback here */ }, // When the seek forward button is pressed
                onSeekBackward = { /* Implement seek backward functionality in MainViewModel and pass the callback here */ } // When the seek backward button is pressed
            )
        }
    }
}

@Composable
fun MainScreen( // Composable for the main screen. This is the main screen of the app
    mainViewModel: MainViewModel, // The main view model
    settingsViewModel: SettingsViewModel, // The settings view model
    onSettingsClicked: () -> Unit, // Function to call when the settings button is pressed
    textToSpeechServiceState: MutableState<TextToSpeechService>
) {
    val context = LocalContext.current // Get the current context
    BoxWithConstraints( // Create a box with constraints to get the maximum height of the screen
        modifier = Modifier // Set the modifier for the box
            .fillMaxSize() // Make the box fill the entire screen
            .padding(16.dp) // Add padding to the box
    ) {
        val maxHeight = constraints.maxHeight // Get the maximum height of the screen
        Column(modifier = Modifier.fillMaxSize()) { // Create a column for the main screen
            LazyColumn( // Create a lazy column for the messages
                modifier = Modifier // Set the modifier for the lazy column
                    .weight(1f) // Make the lazy column fill the entire screen
                    .height(((maxHeight.dp - 64.dp).coerceAtLeast(0.dp))) // Set the height of the lazy column to the maximum height of the screen minus the height of the buttons
            ) {
                items(mainViewModel.conversationMessages) { message -> // For each message in the conversation messages
                    MessageCard(message) { audioFilePath -> // Show the message card
                        mainViewModel.mediaPlaybackManager.playAudio(audioFilePath, context) // Play the audio file
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp)) // Add a spacer to add some space between the messages and the buttons
            Text( // Show the listening status
                text = if (mainViewModel.isListening) "Listening..." else "Not Listening",  // Show "Listening..." if the app is listening and "Not Listening" if the app is not listening
                modifier = Modifier.align(Alignment.CenterHorizontally) // Align the text to the center horizontally
            ) // Add this line to show the listening status
            Spacer(modifier = Modifier.height(16.dp)) // Add a spacer to add some space between the listening status and the buttons
            Button(
                onClick = { // When the start listening button is pressed
                    if (textToSpeechServiceState.value is AndroidTextToSpeechService) { // If the text to speech service is the Android text to speech service
                        textToSpeechServiceState.value = ElevenLabsTextToSpeechService("82b94d982c1018cb379c0acb629d473c", "TxGEqnHWrfWFTfGW9XjX", context)  // Set the text to speech service to the Eleven Labs text to speech service
                    } else { // If the text to speech service is not the Android text to speech service
                        textToSpeechServiceState.value = AndroidTextToSpeechService(context) // Set the text to speech service to the Android text to speech service
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally) // Align the button to the center horizontally
            ) {
                Text(if (textToSpeechServiceState.value is AndroidTextToSpeechService) "Use Eleven Labs TTS" else "Use Google TTS") // Show "Use Eleven Labs TTS" if the text to speech service is the Android text to speech service and "Use Google TTS" if the text to speech service is not the Android text to speech service
            }
            Button( // Create a button for the start listening button
                onClick = { // When the start listening button is pressed
                    if (mainViewModel.isListening) {  // If the app is listening
                        Log.d("MainActivity", "Stop Listening button clicked")  // Log that the stop listening button was clicked
                        mainViewModel.stopListening() // Stop listening
                    } else {
                        Log.d("MainActivity", "Start Listening button clicked") // Log that the start listening button was clicked
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
        }
    }
}

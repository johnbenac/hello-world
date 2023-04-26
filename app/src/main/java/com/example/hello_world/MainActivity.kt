package com.example.hello_world

import ConversationMessage
import EditSettingsScreen
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@ExperimentalMaterial3Api
class MainActivity : AppCompatActivity() {
    private var textToSpeechService: TextToSpeechService? = null // Create a text to speech service
    private lateinit var voiceTriggerDetector: VoiceTriggerDetector // Create a voice trigger detector
    private lateinit var openAiApiService: OpenAiApiService // Create an OpenAI API service
    private lateinit var mainViewModel: MainViewModel
    private val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 1 // Create a request code for requesting audio permission
    private val settingsViewModel = SettingsViewModel() // Create a settings view model
    private val mediaPlaybackManager = AndroidMediaPlaybackManager() // Create a media playback manager



    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MainActivity", "log: MainActivity opened")
        super.onCreate(savedInstanceState) // Call the super class onCreate to complete the creation of activity like the view hierarchy
        requestAudioPermission() // Request audio permission

        val conversationRepository = LocalRoomConversationRepository(this)
        openAiApiService = OpenAiApiService("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", settingsViewModel)
        mainViewModel = MainViewModel(null, this, settingsViewModel, openAiApiService, conversationRepository)
        val textToSpeechServiceState = mutableStateOf<TextToSpeechService>(AndroidTextToSpeechService(this, mediaPlaybackManager) { mainViewModel.startListening() })
        mainViewModel.textToSpeechServiceState = textToSpeechServiceState


        voiceTriggerDetector = mainViewModel.voiceTriggerDetector // Create the voice trigger detector
        setContent { // Set the content of the activity to be the UI defined in the composable function
            val navController = rememberNavController() // Create a nav controller
            NavHost(navController, startDestination = "main") { // Create a nav host
                composable("main") { // Create a composable for the main screen
                    MainScreen(mainViewModel, settingsViewModel, { navController.navigate("settings") }, textToSpeechServiceState, mediaPlaybackManager, navController) // Show the main screen
                } 
                composable("settings") { // Create a composable for the settings screen
                    SettingsScreen(settingsViewModel, { navController.popBackStack() }, navController) // Show the settings screen
                }
                composable("edit-settings") { // Create a composable for the edit settings screen
                    EditSettingsScreen(settingsViewModel, { navController.popBackStack() }, { navController.popBackStack() }) // Show the edit settings screen
                }
                composable("savedConversations") {
                    SavedConversationsScreen(
                        viewModel = SavedConversationsViewModel(conversationRepository),
                        onConversationSelected = { conversationId ->
                            mainViewModel.loadConversation(conversationId)
                            navController.popBackStack()
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }

    private fun requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_PERMISSION_REQUEST_CODE)
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
    internal fun shareConversationText(conversationText: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, conversationText)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, "Share conversation text"))
    }
}
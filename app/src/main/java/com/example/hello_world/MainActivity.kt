package com.example.hello_world

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
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hello_world.data.repository.LocalRoomConversationRepository
import com.example.hello_world.services.media_playback.AndroidMediaPlaybackManager
import com.example.hello_world.services.speech_to_text.VoiceTriggerDetector
import com.example.hello_world.services.text_to_speech.AndroidTextToSpeechService
import com.example.hello_world.services.text_to_speech.TextToSpeechService
import com.example.hello_world.ui.saved_conversations.viewmodel.SavedConversationsViewModel
import com.example.hello_world.ui.session.viewmodel.SessionViewModel
import com.example.hello_world.ui.settings.viewmodel.SettingsViewModel
import java.util.UUID

@ExperimentalMaterial3Api
class MainActivity : AppCompatActivity() {
    private var textToSpeechService: TextToSpeechService? = null
    private lateinit var voiceTriggerDetector: VoiceTriggerDetector
    private lateinit var openAiApiService: OpenAiApiService
    private lateinit var sessionViewModel: SessionViewModel
    private val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 1
    private val settingsViewModel = SettingsViewModel()
    private val mediaPlaybackManager = AndroidMediaPlaybackManager()



    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MainActivity", "log: MainActivity opened")
        super.onCreate(savedInstanceState)
        requestAudioPermission()
        val textToSpeechServiceState = mutableStateOf<TextToSpeechService>(
            AndroidTextToSpeechService(this, mediaPlaybackManager) { sessionViewModel.startListening() })
        val conversationRepository = LocalRoomConversationRepository(this)
        openAiApiService = OpenAiApiService("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", settingsViewModel)
        sessionViewModel = SessionViewModel(
            conversationId = null,
            context = this,
            settingsViewModel = settingsViewModel,
            openAiApiService = openAiApiService,
            conversationRepository = conversationRepository,
            textToSpeechServiceState = textToSpeechServiceState // Pass this argument
        )

        sessionViewModel.textToSpeechServiceState = textToSpeechServiceState


        voiceTriggerDetector = sessionViewModel.voiceTriggerDetector
        setContent {
            val navController = rememberNavController()
            NavHost(navController, startDestination = "home") {
                composable("home") {
                    HomeScreen(onSessionsClicked = { navController.navigate("sessions") }, onConfigPacksClicked = { navController.navigate("configPacks") })
                }
                composable("settings") {
                    SettingsScreen(settingsViewModel, { navController.popBackStack() }, navController)
                }
                composable("edit-settings") {
                    EditSettingsScreen(settingsViewModel, { navController.popBackStack() }, { navController.popBackStack() })
                }
                composable("session/{conversationId}") { backStackEntry ->
                    val conversationId = backStackEntry.arguments?.getString("conversationId")?.let { UUID.fromString(it) }
                    val currentContext = LocalContext.current // Get the current context
                    val sessionViewModel = remember(conversationId) {
                        SessionViewModel(
                            conversationId,
                            currentContext,
                            settingsViewModel,
                            openAiApiService,
                            conversationRepository,
                            textToSpeechServiceState // Pass this argument
                        )
                    }
                    sessionViewModel.textToSpeechServiceState = textToSpeechServiceState
                    SessionScreen(sessionViewModel, settingsViewModel, { navController.navigate("settings") }, textToSpeechServiceState, mediaPlaybackManager, navController)
                }
                composable("sessions") {
                    SavedConversationsScreen(
                        viewModel = SavedConversationsViewModel(conversationRepository),
                        onConversationSelected = { conversationId ->
                            navController.navigate("session/${conversationId.toString()}")
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("configPacks") {
                    Text("Placeholder for ConfigPacksScreen")
                }
            }
        }
    }

    private fun requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onResume() {
        super.onResume()
        voiceTriggerDetector.startListening()
    }
    override fun onPause() {
        super.onPause()
        textToSpeechService?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        textToSpeechService?.shutdown()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {

                Toast.makeText(this, "Permission to record audio is required to use this app.", Toast.LENGTH_LONG).show() // Show a toast message to the user
                finish()
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
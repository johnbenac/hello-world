package com.example.hello_world

import ConfigPackScreen
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.SnackbarHostState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hello_world.data.repository.LocalRoomConfigPackRepository
import com.example.hello_world.data.repository.LocalRoomConversationRepository
import com.example.hello_world.services.backup.google_drive.GoogleDriveBackupHelper
import com.example.hello_world.services.media_playback.AndroidMediaPlaybackManager
import com.example.hello_world.services.speech_to_text.VoiceTriggerDetector
import com.example.hello_world.services.text_to_speech.AndroidTextToSpeechService
import com.example.hello_world.services.text_to_speech.TextToSpeechService
import com.example.hello_world.ui.saved_conversations.viewmodel.SavedConversationsViewModel
import com.example.hello_world.ui.session.viewmodel.SessionViewModel
import com.example.hello_world.ui.session.viewmodel.SessionViewModelFactory
import com.example.hello_world.ui.ConfigPacks.viewmodel.ConfigPacksViewModel
import com.example.hello_world.ui.google_drive_backup.view.GoogleDriveBackupScreen
import kotlinx.coroutines.launch
import java.util.UUID


@ExperimentalMaterial3Api
class MainActivity : AppCompatActivity() {
    private var textToSpeechService: TextToSpeechService? = null
    private var voiceTriggerDetector: VoiceTriggerDetector? = null
    private lateinit var openAiApiService: OpenAiApiService


    private val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 1

    private val mediaPlaybackManager = AndroidMediaPlaybackManager()
    private lateinit var configPackRepository: LocalRoomConfigPackRepository
    private lateinit var conversationRepository: LocalRoomConversationRepository
    private lateinit var configPacksViewModel: ConfigPacksViewModel
    private lateinit var textToSpeechServiceState: MutableState<TextToSpeechService>
    private lateinit var snackbarHostState: SnackbarHostState

    val sessionViewModel: SessionViewModel by viewModels {
        SessionViewModelFactory(
            conversationId = null,
            context = this@MainActivity,
            configPacksViewModel = configPacksViewModel,
            openAiApiService = openAiApiService,
            conversationRepository = conversationRepository,
            textToSpeechServiceState = textToSpeechServiceState,
            snackbarHostState = snackbarHostState
        )
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestAudioPermission()

        configPackRepository = LocalRoomConfigPackRepository(this)
        conversationRepository = LocalRoomConversationRepository(this)
        configPacksViewModel = ConfigPacksViewModel(configPackRepository)
        textToSpeechServiceState = mutableStateOf(
            AndroidTextToSpeechService(this, mediaPlaybackManager) { sessionViewModel.beginListening() })
        snackbarHostState = SnackbarHostState()

        openAiApiService = OpenAiApiService("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", configPacksViewModel)
//        openAiApiService = OpenAiApiService("sk-SggwqYZZuvSwiFI5zkQmIRroRZ", settingsViewModel)





//        voiceTriggerDetector = sessionViewModel.listeningManager

        setContent {
            Log.d("MainActivity", "Current SessionViewModel instance: ${sessionViewModel}, memory location: ${System.identityHashCode(sessionViewModel)}")
            val navController = rememberNavController()

            NavHost(navController, startDestination = "home") {
                composable("home") {
                    HomeScreen(onSessionsClicked = { navController.navigate("sessions") }, onConfigPacksClicked = { navController.navigate("configPacks") })
                }
                composable("settings") {
                    ConfigPacksScreen(configPacksViewModel, { navController.popBackStack() }, navController)
                }
                composable("edit-settings") {
                    ConfigPackScreen(configPacksViewModel, { navController.popBackStack() }, { navController.popBackStack() })
                }
                composable("session/{conversationId}") { backStackEntry ->
                    val conversationId = backStackEntry.arguments?.getString("conversationId")?.let { UUID.fromString(it) }
                    SessionScreen(
                        sessionViewModel,
                        configPacksViewModel,
                        { navController.navigate("settings") },
                        textToSpeechServiceState,
                        mediaPlaybackManager = mediaPlaybackManager,
                        navController,
                        snackbarHostState
                    )
                }
                composable("sessions") {
                    val savedConversationsViewModel = remember { SavedConversationsViewModel(conversationRepository, this@MainActivity, activity = this@MainActivity) }
                    SavedConversationsScreen(
                        viewModel = savedConversationsViewModel,
                        onConversationSelected = { conversationId ->
                            Log.d("SessionScreen", "Selected conversation ID: ${conversationId}")
                            navController.navigate("session/${conversationId.toString()}")
                            sessionViewModel.loadConversationWithId(conversationId)
                            Log.d("SessionScreen", "Selected conversation ID after `navController.navigate(/.../) was called`: ${conversationId}")

                        },
                        onBack = { navController.popBackStack() },
                        onNewConversationClicked = {
                            savedConversationsViewModel.viewModelScope.launch {
                                val newConversationId = savedConversationsViewModel.createNewConversation()
                                navController.navigate("session/${newConversationId.toString()}")
                                sessionViewModel.loadConversationWithId(newConversationId)
                            }
                        },
                        onGoogleDriveBackupClicked = { navController.navigate("googleDriveBackup") }
                    )
                }
                composable("googleDriveBackup") {
                    GoogleDriveBackupScreen(activityResultRegistry)
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
        voiceTriggerDetector?.beginListening()
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

                Toast.makeText(this, "Permission to record audio is required to use this app.", Toast.LENGTH_LONG).show()
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
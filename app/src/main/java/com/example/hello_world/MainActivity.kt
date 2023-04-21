package com.example.hello_world
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.MutableState




// data class ConversationMessage(val sender: String, val message: String)

class MainActivity : AppCompatActivity() {
    private lateinit var textToSpeechService: TextToSpeechService
    private lateinit var voiceTriggerDetector: VoiceTriggerDetector
    private lateinit var openAiApiService: OpenAiApiService
    private val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 1
    private val settingsViewModel = SettingsViewModel()


    private lateinit var assistantViewModel: AssistantViewModel

    override fun onCreate(savedInstanceState: Bundle?) {

//        val textToSpeechServiceState = mutableStateOf<TextToSpeechService>(AndroidTextToSpeechService(this))
//        assistantViewModel = AssistantViewModel(textToSpeechServiceState, this, settingsViewModel, openAiApiService)
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "log: MainActivity opened")

        // Request audio recording permission
        requestAudioPermission()

        // Initialize the TextToSpeechService state
        val textToSpeechServiceState = mutableStateOf<TextToSpeechService>(AndroidTextToSpeechService(this))

        openAiApiService = OpenAiApiService("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", settingsViewModel)
        assistantViewModel = AssistantViewModel(textToSpeechServiceState, this, settingsViewModel, openAiApiService)



        voiceTriggerDetector = assistantViewModel.voiceTriggerDetector

        setContent {
            val navController = rememberNavController()
            NavHost(navController, startDestination = "assistant") {
                composable("assistant") {
                    AssistantScreen(assistantViewModel, settingsViewModel, { navController.navigate("settings") }, textToSpeechServiceState)
                }
                composable("settings") {
                    SettingsScreen(settingsViewModel, { navController.popBackStack() }, navController)
                }
                composable("edit-settings") {
                    EditSettingsScreen(settingsViewModel, { navController.popBackStack() }, { navController.popBackStack() })
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
        textToSpeechService.stop() // Stop any ongoing speech
    }

    override fun onDestroy() {
        super.onDestroy()
        textToSpeechService.shutdown()
    }

    private val conversationMessages = mutableStateListOf<ConversationMessage>()


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted
                // Continue with creating the app UI and setting up listeners

            } else {
                // Permission was denied
                // Show a message to the user and close the app
                Toast.makeText(this, "Permission to record audio is required to use this app.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
}

@Composable
fun ConversationScreen(messages: List<ConversationMessage>) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LazyColumn(state = listState) {
        items(messages) { message ->
            MessageCard(message)
        }
    }
}

@Composable
fun MessageCard(message: ConversationMessage) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(text = message.sender, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = message.message)
        }
    }
}

@Composable
fun AssistantScreen(
    assistantViewModel: AssistantViewModel,
    settingsViewModel: SettingsViewModel,
    onSettingsClicked: () -> Unit,
    textToSpeechServiceState: MutableState<TextToSpeechService>
) {
    val context = LocalContext.current // Get the current context

    // ...

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val maxHeight = constraints.maxHeight

        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .height(((maxHeight.dp - 64.dp).coerceAtLeast(0.dp)))
            ) {
                items(assistantViewModel.conversationMessages) { message -> // Use assistantViewModel.conversationMessages here
                    MessageCard(message)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (assistantViewModel.isListening) "Listening..." else "Not Listening", // Use assistantViewModel.isListening here
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) // Add this line to show the listening status

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (textToSpeechServiceState.value is AndroidTextToSpeechService) {
                        textToSpeechServiceState.value = ElevenLabsTextToSpeechService("82b94d982c1018cb379c0acb629d473c", "TxGEqnHWrfWFTfGW9XjX", context) // Pass the context here
                    } else {
                        textToSpeechServiceState.value = AndroidTextToSpeechService(context) // Pass the context here
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(if (textToSpeechServiceState.value is AndroidTextToSpeechService) "Use Eleven Labs TTS" else "Use Google TTS")
            }
            Button(
                onClick = {
                    if (assistantViewModel.isListening) { // Use assistantViewModel.isListening here
                        Log.d("MainActivity", "Stop Listening button clicked")
                        assistantViewModel.stopListening()
                    } else {
                        Log.d("MainActivity", "Start Listening button clicked")
                        assistantViewModel.startListening()
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(if (assistantViewModel.isListening) "Stop Listening" else "Start Listening") // Use assistantViewModel.isListening here
            }

            Button(
                onClick = onSettingsClicked,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Settings")
            }
        }
    }
}
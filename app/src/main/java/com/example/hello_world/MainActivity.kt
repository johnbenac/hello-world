package com.example.hello_world
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



// data class ConversationMessage(val sender: String, val message: String)

class MainActivity : AppCompatActivity() {
    private lateinit var textToSpeechService: TextToSpeechService
    private lateinit var assistantViewModel: AssistantViewModel
    private lateinit var voiceTriggerDetector: VoiceTriggerDetector
    private val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "log: MainActivity opened")
        // Request audio recording permission
        requestAudioPermission()

        textToSpeechService = AndroidTextToSpeechService(this)
        assistantViewModel = AssistantViewModel(textToSpeechService, this)
        voiceTriggerDetector = VoiceTriggerDetector(this, "Hey", assistantViewModel::onTriggerWordDetected)

        setContent {
            AssistantScreen(assistantViewModel)
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
        voiceTriggerDetector.stopListening()
        textToSpeechService.stop() // Stop any ongoing speech
    }

    override fun onDestroy() {
        super.onDestroy()
        textToSpeechService.shutdown()
    }

    private val conversationMessages = mutableStateListOf<ConversationMessage>()

//    private fun onAssistantResponse(response: String) {
//        // Add assistant message to the conversation state
//        conversationMessages.add(ConversationMessage("Assistant", response))
//        assistantViewModel.onAssistantResponse(response)
//    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted
                // Continue with creating the app UI and setting up listeners
                setContent {
                    AssistantScreen(assistantViewModel)
                }
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
    LazyColumn {
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
fun AssistantScreen(assistantViewModel: AssistantViewModel) {
    val conversationMessages = assistantViewModel.conversationMessages
    val isListening = assistantViewModel.isListening


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ConversationScreen(messages = conversationMessages)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (isListening) {
                    assistantViewModel.stopListening()
                } else {
                    assistantViewModel.startListening()
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(if (isListening) "Stop Listening" else "Start Listening")
        }
    }
}
package com.example.hello_world
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.hello_world.ui.theme.HelloworldTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel



// data class ConversationMessage(val sender: String, val message: String)

class MainActivity : AppCompatActivity() {
    private lateinit var textToSpeechService: TextToSpeechService
    private lateinit var assistantViewModel: AssistantViewModel
    private lateinit var voiceTriggerDetector: VoiceTriggerDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        textToSpeechService = AndroidTextToSpeechService(this)
        assistantViewModel = AssistantViewModel(textToSpeechService)
        voiceTriggerDetector = VoiceTriggerDetector(this, "Hey", assistantViewModel::onTriggerWordDetected)

        setContent {
            AssistantScreen(assistantViewModel)
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

    private fun onTriggerWordDetected() {
        assistantViewModel.onTriggerWordDetected()
        // Handle trigger word detection, for example, call textToSpeechService.speak("Response text")
        conversationMessages.add(ConversationMessage("User", "Trigger Word"))
    }

    private fun onAssistantResponse(response: String) {
        // Add assistant message to the conversation state
        conversationMessages.add(ConversationMessage("Assistant", response))
        assistantViewModel.onAssistantResponse(response)
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

// class AssistantViewModel(
//     private val voiceTriggerDetector: VoiceTriggerDetector,
//     private val textToSpeechService: TextToSpeechService
// ) : ViewModel() {

//     private val _conversationMessages = mutableStateListOf<ConversationMessage>()
//     val conversationMessages: List<ConversationMessage> get() = _conversationMessages

//     private val _isListening = mutableStateOf(false)
//     val isListening: Boolean get() = _isListening.value

//     fun startListening() {
//         voiceTriggerDetector.startListening()
//         _isListening.value = true
//     }

//     fun stopListening() {
//         voiceTriggerDetector.stopListening()
//         _isListening.value = false
//     }

//     fun onTriggerWordDetected() {
//         // Add user message to the conversation state
//         _conversationMessages.add(ConversationMessage("User", "Trigger Word"))

//         // Stop listening for the trigger word
//         stopListening()

//         // Handle trigger word detection, for example, call textToSpeechService.speak("Response text")
//         textToSpeechService.speak("Response text") {
//             // Restart the SpeechRecognizer to listen for the trigger word again
//             startListening()
//         }
//     }

//     fun onAssistantResponse(response: String) {
//         // Add assistant message to the conversation state
//         _conversationMessages.add(ConversationMessage("Assistant", response))
//     }
// }

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
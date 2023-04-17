package com.example.hello_world
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import android.util.Log
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class AssistantViewModel(
    private val textToSpeechService: TextToSpeechService,
    private val context: Context,
    private val openAiApiService: OpenAiApiService
) : ViewModel() {

    private val voiceTriggerDetector = VoiceTriggerDetector(context, "Hey", this::onTriggerWordDetected)

    private val _conversationMessages = mutableStateListOf<ConversationMessage>()
    val conversationMessages: List<ConversationMessage> get() = _conversationMessages

    private val _isListening = mutableStateOf(false)
    val isListening: Boolean get() = _isListening.value

    fun startListening() {
//        voiceTriggerDetector.startListening()
        voiceTriggerDetector.startListening()
        _isListening.value = true
    }

    private suspend fun sendUserMessageToOpenAi(userMessage: String) {
        val responseText = openAiApiService.sendMessage(userMessage)
        onAssistantResponse(responseText)
        textToSpeechService.speak(responseText) {
            Handler(Looper.getMainLooper()).post {
                voiceTriggerDetector.startListening()
            }
        }
    }

    fun stopListening() {
//        voiceTriggerDetector.stopListening()
        voiceTriggerDetector.stopListening()
        _isListening.value = false
    }

    fun onTriggerWordDetected() {
        // Add user message to the conversation state
        _conversationMessages.add(ConversationMessage("User", "Trigger Word"))
        Log.d("AssistantViewModel", "log: onTriggerWordDetected called")
    
        // Stop listening
        voiceTriggerDetector.stopListeningForever()
    
        // Handle trigger word detection, for example, call textToSpeechService.speak("Response text")
        textToSpeechService.speak("Response text") {
            // Run startListening() on the main thread
            Handler(Looper.getMainLooper()).post {
                voiceTriggerDetector.startListening()
            }
        }
    
        // Get the transcription of the message received after the trigger word
        val userMessage = "Transcription of the message received after the trigger word"
    
        // Send the user message to OpenAI API and process the response
        viewModelScope.launch {
            sendUserMessageToOpenAi(userMessage)
        }
    }

    fun onAssistantResponse(response: String) {
        // Add assistant message to the conversation state
        _conversationMessages.add(ConversationMessage("Assistant", response))
    }
}
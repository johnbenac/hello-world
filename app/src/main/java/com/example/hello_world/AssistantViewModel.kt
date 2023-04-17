package com.example.hello_world
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import android.util.Log


class AssistantViewModel(
    private val textToSpeechService: TextToSpeechService,
    private val context: Context
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
            // Start listening again after the response is spoken
            voiceTriggerDetector.startListening()
        }
    }

    fun onAssistantResponse(response: String) {
        // Add assistant message to the conversation state
        _conversationMessages.add(ConversationMessage("Assistant", response))
    }
}
package com.example.hello_world
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class AssistantViewModel(
    private val textToSpeechService: TextToSpeechService
) : ViewModel() {

    private val _conversationMessages = mutableStateListOf<ConversationMessage>()
    val conversationMessages: List<ConversationMessage> get() = _conversationMessages

    private val _isListening = mutableStateOf(false)
    val isListening: Boolean get() = _isListening.value

    fun startListening() {
//        voiceTriggerDetector.startListening()
        _isListening.value = true
    }

    fun stopListening() {
//        voiceTriggerDetector.stopListening()
        _isListening.value = false
    }

    fun onTriggerWordDetected() {
        // Add user message to the conversation state
        _conversationMessages.add(ConversationMessage("User", "Trigger Word"))

        // Handle trigger word detection, for example, call textToSpeechService.speak("Response text")
    }

    fun onAssistantResponse(response: String) {
        // Add assistant message to the conversation state
        _conversationMessages.add(ConversationMessage("Assistant", response))
    }
}
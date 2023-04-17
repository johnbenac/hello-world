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
    val latestPartialResult = mutableStateOf<String?>(null)  // Change this line


    private val mainHandler = Handler(Looper.getMainLooper())
    private val voiceTriggerDetector = VoiceTriggerDetector(context, "Hey", this::onTriggerWordDetected, mainHandler, this.latestPartialResult)

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
        // Add user message to the conversation state
        _conversationMessages.add(ConversationMessage("User", userMessage))

        val responseText = openAiApiService.sendMessage(_conversationMessages)
        onAssistantResponse(responseText)
        textToSpeechService.speak(responseText) {
            Handler(Looper.getMainLooper()).post {
                voiceTriggerDetector.startListening()
            }
        }
    }

    private fun onAssistantResponse(response: String) {
        // Add assistant message to the conversation state
        _conversationMessages.add(ConversationMessage("Assistant", response))
    }

    fun stopListening() {
//        voiceTriggerDetector.stopListening()
        voiceTriggerDetector.stopListening()
        _isListening.value = false
    }

    fun onTriggerWordDetected(userMessage: String) { // Add userMessage parameter
        // Add user message to the conversation state
        // _conversationMessages.add(ConversationMessage("User", "Trigger Word"))
        Log.d("AssistantViewModel", "log: onTriggerWordDetected called")
    
        // Stop listening
        voiceTriggerDetector.stopListening() // Replace stopListeningForever() with stopListening()
    
        // Send the user message to OpenAI API and process the response
        viewModelScope.launch {
            sendUserMessageToOpenAi(userMessage) // Pass the userMessage parameter here
        }
    }
}
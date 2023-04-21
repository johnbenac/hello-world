package com.example.hello_world
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import android.util.Log
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class AssistantViewModel(
    private val textToSpeechService: TextToSpeechService,
    private val context: Context,
    private val settingsViewModel: SettingsViewModel,
    private val openAiApiService: OpenAiApiService
) : ViewModel() {
//    private val openAiApiService = OpenAiApiService("your_api_key_here", settingsViewModel)
    val latestPartialResult = mutableStateOf<String?>(null) 
    val _isAssistantSpeaking = mutableStateOf(false)
    val isAssistantSpeaking: Boolean get() = _isAssistantSpeaking.value
//    val shouldListenAfterSpeaking = mutableStateOf(true)

    private val mainHandler = Handler(Looper.getMainLooper())
    val voiceTriggerDetector = VoiceTriggerDetector(context, "Hey", this::onTriggerWordDetected, mainHandler, this.latestPartialResult)

    private val _conversationMessages = mutableStateListOf<ConversationMessage>()
    val conversationMessages: List<ConversationMessage> get() = _conversationMessages

    private val _isListening = mutableStateOf(false)
    val isListening: Boolean get() = _isListening.value


    fun startListening() {
        voiceTriggerDetector.startListening()
        _isListening.value = true
//        Log.d("AssistantViewModel", "log: startListening called 1")
    }

    private suspend fun sendUserMessageToOpenAi(userMessage: String) {
        // Add user message to the conversation state
        _conversationMessages.add(ConversationMessage("User", userMessage))

        val responseText = openAiApiService.sendMessage(_conversationMessages)
        onAssistantResponse(responseText)
        textToSpeechService.speak(responseText, onFinish = {
            mainHandler.post {
                _isAssistantSpeaking.value = false
//                if (_isListening.value) {
                    startListening()
                    Log.d("AssistantViewModel", "log: startListening called 2")
//                }
            }
        }, onStart = {
            mainHandler.post {
                stopListening()
                Log.d("AssistantViewModel", "log: stopListening called 1")
            }
        })
        _isAssistantSpeaking.value = true
    }

    private fun startPeriodicListeningCheck() {
        mainHandler.postDelayed({
            if (_isListening.value && !_isAssistantSpeaking.value) {
//                Log.d("AssistantViewModel", "log: Periodic check - Restarting listening")
                startListening()
            }
            startPeriodicListeningCheck()
        }, 3000) // Check every 3 seconds
    }

    private fun onAssistantResponse(response: String) {
        // Add assistant message to the conversation state
        _conversationMessages.add(ConversationMessage("Assistant", response))
    }


    fun stopListening() {
        voiceTriggerDetector.stopListening()
        Log.d("AssistantViewModel", "log: stopListening called 2")
        _isListening.value = false
    }

    

    fun onTriggerWordDetected(userMessage: String) { // Add userMessage parameter
        // Add user message to the conversation state
        Log.d("AssistantViewModel", "log: onTriggerWordDetected called")
    
        // Stop listening
        voiceTriggerDetector.stopListening() // Replace stopListeningForever() with stopListening()
        Log.d("AssistantViewModel", "log: stopListening called 3")
    
        // Send the user message to OpenAI API and process the response
        viewModelScope.launch {
            sendUserMessageToOpenAi(userMessage) // Pass the userMessage parameter here
        }
    }

    init {
        startPeriodicListeningCheck()
    }
}
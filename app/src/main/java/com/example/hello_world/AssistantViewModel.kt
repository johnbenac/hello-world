package com.example.hello_world
import ConversationMessage
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import android.util.Log
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.MutableState
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class AssistantViewModel(
    private val textToSpeechServiceState: MutableState<TextToSpeechService>,
    private val context: Context,
    private val settingsViewModel: SettingsViewModel,
    private val openAiApiService: OpenAiApiService
) : ViewModel() {
//    private val audioFilePathState = mutableStateOf<String>("") // Add this line
    //    private val openAiApiService = OpenAiApiService("your_api_key_here", settingsViewModel)
    val latestPartialResult = mutableStateOf<String?>(null)
    val _isAssistantSpeaking = mutableStateOf(false)
    val mediaPlaybackManager: MediaPlaybackManager = AndroidMediaPlaybackManager()
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
        val audioFilePathState = mutableStateOf("")
        // Add user message to the conversation state
        _conversationMessages.add(ConversationMessage("User", userMessage, audioFilePathState))
        val responseText = openAiApiService.sendMessage(_conversationMessages)
        Log.d("AssistantViewModel", "Received response from OpenAI API: $responseText")
        Log.d("AssistantViewModel", "User message added with audioFilePathState: $audioFilePathState")
        onAssistantResponse(responseText, audioFilePathState)
        textToSpeechServiceState.value.speak(responseText.replace("\n", " "), onFinish = {
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
        }, audioFilePathState = _conversationMessages.last().audioFilePath)
        Log.d("AssistantViewModel", "Updated audioFilePathState: ${audioFilePathState.value}")
        _isAssistantSpeaking.value = true
    }
    private fun startPeriodicListeningCheck() {
        mainHandler.postDelayed({
            if (_isListening.value && _isAssistantSpeaking.value) {
//                Log.d("AssistantViewModel", "log: Periodic check - Restarting listening")
                startListening()
            }
            startPeriodicListeningCheck()
        }, 3000) // Check every 3 seconds
    }
    private fun onAssistantResponse(response: String, audioFilePathState: MutableState<String>) {
        val assistantAudioFilePathState = mutableStateOf("")
        Log.d("AssistantViewModel", "log: onAssistantResponse called")
        // Add assistant message to the conversation state
        _conversationMessages.add(ConversationMessage("Assistant", response, assistantAudioFilePathState))
        Log.d("AssistantViewModel", "Assistant message added with audioFilePathState: $assistantAudioFilePathState")
        Log.d("AssistantViewModel", "log: _conversationMessages added")
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

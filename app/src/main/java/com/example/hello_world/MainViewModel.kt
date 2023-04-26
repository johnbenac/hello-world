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
import kotlinx.coroutines.launch
import com.example.hello_world.ConversationModel


class MainViewModel( 
    private val textToSpeechServiceState: MutableState<TextToSpeechService>, 
    private val context: Context,
    private val settingsViewModel: SettingsViewModel,
    private val openAiApiService: OpenAiApiService
) : ViewModel() {
    val conversationModel = ConversationModel()
    val latestPartialResult = mutableStateOf<String?>(null)
    val _isAppSpeaking = mutableStateOf(false)
    val mediaPlaybackManager: MediaPlaybackManager = AndroidMediaPlaybackManager()
    val isAppSpeaking: Boolean get() = _isAppSpeaking.value

    private val mainHandler = Handler(Looper.getMainLooper())
    val voiceTriggerDetector = VoiceTriggerDetector(context, "Hey", this::onTriggerWordDetected, mainHandler, this.latestPartialResult)

    val conversationMessages: List<ConversationMessage> get() = conversationModel.conversation.messages
    private val _isListening = mutableStateOf(false)
    val isListening: Boolean get() = _isListening.value
    fun startListening() {
        voiceTriggerDetector.startListening()
        _isListening.value = true
        Log.d("MainViewModel", "log: from within the startListening() function, `voiceTriggerDetector.startListening()` and `_isListening.value = true` were just called.")
    }
    private suspend fun sendUserMessageToOpenAi(userMessage: String) {
        stopListening()
        val audioFilePathState = mutableStateOf("")
        // Add user message to the conversation state
        conversationModel.addMessage(ConversationMessage("User", userMessage, audioFilePathState))
        val responseText = openAiApiService.sendMessage(conversationModel.conversation.messages)
        Log.d("MainViewModel", "Received response from OpenAI API: $responseText")
//        Log.d("MainViewModel", "User message added with audioFilePathState: $audioFilePathState")
        conversationModel.addMessage(ConversationMessage("Assistant", responseText, audioFilePathState))
        textToSpeechServiceState.value.renderSpeech(responseText.replace("\n", " "), onFinish = {
            if (conversationModel.conversation.messages.isNotEmpty()) {
            mainHandler.post {
                _isAppSpeaking.value = false
//                if (_isListening.value) {
                startListening()
                Log.d("MainViewModel", "log: startListening called associated with onFinish")
//                }
            }
        }}, onStart = {
            mainHandler.post {
                stopListening()
                Log.d("MainViewModel", "log: stopListening called associated with onStart")
            }
        }, audioFilePathState = conversationModel.conversation.messages.last().audioFilePath)
//        Log.d("MainViewModel", "Updated audioFilePathState: ${audioFilePathState.value}")
        _isAppSpeaking.value = true
    }

    fun updateMessage(index: Int, updatedMessage: ConversationMessage) {
        conversationModel.updateMessage(index, updatedMessage)
    }

    fun deleteMessage(index: Int) {
        conversationModel.deleteMessage(index)
    }
    private fun startPeriodicListeningCheck() {
        mainHandler.postDelayed({
            if (_isListening.value && _isAppSpeaking.value) {
                Log.d("MainViewModel", "log: Periodic check - Restarting listening")
                startListening()
            }
            startPeriodicListeningCheck()
        }, 3000) // Check every 3 seconds
    }
//    private fun onAssistantResponse(response: String, audioFilePathState: MutableState<String>) {
//        val assistantAudioFilePathState = mutableStateOf("")
////        Log.d("MainViewModel", "log: onAssistantResponse called")
//        // Add assistant message to the conversation state
//        conversationModel.addMessage(ConversationMessage("Assistant", response, assistantAudioFilePathState))
////        Log.d("MainViewModel", "Assistant message added with audioFilePathState: $assistantAudioFilePathState")
////        Log.d("MainViewModel", "log: _conversationMessages added")
//    }
    fun stopListening() {
        voiceTriggerDetector.stopListening()
        Log.d("MainViewModel", "log: stopListening called 2")
        _isListening.value = false
    }

    fun onTriggerWordDetected(userMessage: String) { // Add userMessage parameter
        // Add user message to the conversation state
        Log.d("MainViewModel", "log: onTriggerWordDetected called")

        // Stop listening
        voiceTriggerDetector.stopListening() // Replace stopListeningForever() with stopListening()
        Log.d("MainViewModel", "log: from within the OnTriggerWordDetected function, `voiceTriggerDetector.stopListening()` was just called")

        // Send the user message to OpenAI API and process the response
        viewModelScope.launch {
            sendUserMessageToOpenAi(userMessage) // Pass the userMessage parameter here
        }
    }
    init {
        startPeriodicListeningCheck()
    }
}

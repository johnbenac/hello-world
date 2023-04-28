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
import com.example.hello_world.services.AndroidMediaPlaybackManager
import kotlinx.coroutines.launch
import java.util.UUID


class SessionViewModel(
    val conversationId: UUID?,
    val context: Context,
    val settingsViewModel: SettingsViewModel,
    val openAiApiService: OpenAiApiService,
    val conversationRepository: IConversationRepository,
    var textToSpeechServiceState: MutableState<TextToSpeechService>?
) : ViewModel() {


    val latestPartialResult = mutableStateOf<String?>(null)
    val _isAppSpeaking = mutableStateOf(false)
    val mediaPlaybackManager: MediaPlaybackManager = AndroidMediaPlaybackManager()
    private val conversationsManager = ConversationsManager(conversationRepository)
    private val conversationManager = ConversationManager(Conversation(configPack = ConfigPack.defaultConfigPack))
    val isAppSpeaking: Boolean get() = _isAppSpeaking.value
    val showSaveDialog = mutableStateOf(false)
    val saveDialogTitle = mutableStateOf("")

    private val mainHandler = Handler(Looper.getMainLooper())
    val voiceTriggerDetector = VoiceTriggerDetector(context, "Hey", this::onTriggerWordDetected, mainHandler, this.latestPartialResult)

    val conversationMessages = mutableStateListOf<ConversationMessage>().apply {
        addAll(conversationManager.conversation.messages)
    }
    private val _isListening = mutableStateOf(false)
    val isListening: Boolean get() = _isListening.value


    fun loadInitialConversation(conversationId: UUID? = null) {
        viewModelScope.launch {
            val loadedConversation = if (conversationId != null) {
                conversationsManager.loadConversation(conversationId)
            } else {
                null
            }
            if (loadedConversation != null) {
                conversationManager.conversation = loadedConversation
            } else {
                // Use the default profile for the initial conversation
                val initialConversation = Conversation(configPack = ConfigPack.defaultConfigPack)
                conversationManager.conversation = initialConversation
            }
        }
    }


    fun saveCurrentConversation() {
        viewModelScope.launch {
            conversationsManager.saveConversation(conversationManager.conversation)
        }
    }

    fun startListening() {
        voiceTriggerDetector.startListening()
        _isListening.value = true
        Log.d("MainViewModel", "log: from within the startListening() function, `voiceTriggerDetector.startListening()` and `_isListening.value = true` were just called.")
    }
    private suspend fun sendUserMessageToOpenAi(userMessage: String) {


        stopListening()
        val audioFilePathState = mutableStateOf("")
        // Add user message to the conversation state


        val userMessageObj = ConversationMessage("User", userMessage, audioFilePathState)
        conversationManager.addMessage(userMessageObj)
        conversationMessages.add(userMessageObj)


        val responseText = openAiApiService.sendMessage(conversationManager.conversation.messages)
        Log.d("MainViewModel", "Received response from OpenAI API: $responseText")
//        Log.d("MainViewModel", "User message added with audioFilePathState: $audioFilePathState")


        val assistantMessageObj = ConversationMessage("Assistant", responseText, audioFilePathState)
        conversationManager.addMessage(assistantMessageObj)
        conversationMessages.add(assistantMessageObj)

        textToSpeechServiceState?.value?.renderSpeech(responseText.replace("\n", " "), onFinish = {
            if (conversationManager.conversation.messages.isNotEmpty()) {
            mainHandler.post {
                _isAppSpeaking.value = false
                startListening()
                Log.d("MainViewModel", "log: startListening called associated with onFinish")
            }
        }}, onStart = {
            mainHandler.post {
                stopListening()
                Log.d("MainViewModel", "log: stopListening called associated with onStart")
            }
        }, audioFilePathState = conversationManager.conversation.messages.last().audioFilePath)
//        Log.d("MainViewModel", "Updated audioFilePathState: ${audioFilePathState.value}")
        _isAppSpeaking.value = true
    }

    fun updateMessage(index: Int, updatedMessage: ConversationMessage) {
        conversationManager.updateMessage(index, updatedMessage)
        conversationMessages[index] = updatedMessage
    }

    fun deleteMessage(index: Int) {
        viewModelScope.launch {
            conversationManager.deleteMessage(index)
            conversationMessages.removeAt(index)
        }
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

    fun loadConversation(conversationId: UUID) {
        viewModelScope.launch {
            val loadedConversation = conversationsManager.loadConversation(conversationId)
            if (loadedConversation != null) {
                conversationManager.conversation = loadedConversation
                conversationMessages.clear()
                conversationMessages.addAll(conversationManager.conversation.messages)
                Log.d("SessionViewModel", "Loaded conversation ID: ${loadedConversation.id}")
                Log.d("SessionViewModel", "Number of messages in loaded conversation: ${loadedConversation.messages.size}")
                Log.d("SessionViewModel", "Messages in loaded conversation: ${loadedConversation.messages}")
            }
        }
    }
    init {
        loadInitialConversation(conversationId)
        startPeriodicListeningCheck()
    }


    fun saveConversation() {
        showSaveDialog.value = true
    }

    fun onSaveDialogConfirmed() {
        if (saveDialogTitle.value.isNotBlank()) {
            viewModelScope.launch {
                val updatedConversation = conversationManager.conversation.copy(title = saveDialogTitle.value)
                conversationsManager.saveConversation(conversationManager.conversation)
                conversationManager.conversation = updatedConversation
            }
            showSaveDialog.value = false
            saveDialogTitle.value = ""
        }
    }

    fun onSaveDialogDismissed() {
        showSaveDialog.value = false
        saveDialogTitle.value = ""
    }
}
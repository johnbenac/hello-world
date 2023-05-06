package com.example.hello_world.ui.session.viewmodel
import com.example.hello_world.models.ConversationMessage
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import android.util.Log
import android.os.Handler
import android.os.Looper
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hello_world.managers.ConversationManager
import com.example.hello_world.managers.ConversationsManager
import com.example.hello_world.OpenAiApiService
import com.example.hello_world.ui.ConfigPacks.viewmodel.ConfigPacksViewModel
import com.example.hello_world.services.text_to_speech.TextToSpeechService
import com.example.hello_world.services.speech_to_text.VoiceTriggerDetector
import com.example.hello_world.data.repository.IConversationRepository
import com.example.hello_world.models.ConfigPack
import com.example.hello_world.models.Conversation
import com.example.hello_world.services.media_playback.AndroidMediaPlaybackManager
import com.example.hello_world.services.media_playback.MediaPlaybackManager
import kotlinx.coroutines.launch
import java.util.UUID
import com.example.hello_world.withExponentialBackoff


class SessionViewModel(
    var conversationId: UUID?,
    val context: Context,
    val configPacksViewModel: ConfigPacksViewModel,
    val openAiApiService: OpenAiApiService,
    val conversationRepository: IConversationRepository,
    var textToSpeechServiceState: MutableState<TextToSpeechService>?,
    private val snackbarHostState: SnackbarHostState
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
        Log.d("SessionViewModel", "fun loadInitialConversation from before the `viewModelScope.launch` block, loadInitialConversation called with conversationId: $conversationId")
        viewModelScope.launch {
            Log.d("SessionViewModel", "fun loadInitialConversation from within the `viewModelScope.launch` block, loadInitialConversation called with conversationId: $conversationId")
            val loadedConversation = if (conversationId != null) {
                conversationsManager.loadConversation(conversationId)
            } else {
                null
            }
            if (loadedConversation != null) {
                Log.d("SessionViewModel", "fun loadInitialConversation Loaded conversation: $loadedConversation")
                conversationManager.conversation = loadedConversation
                conversationMessages.clear()
                conversationMessages.addAll(conversationManager.conversation.messages)
            } else {
                // Use the default profile for the initial conversation
                val initialConversation = Conversation(configPack = ConfigPack.defaultConfigPack)
                conversationManager.conversation = initialConversation
                conversationMessages.clear()
                conversationMessages.addAll(conversationManager.conversation.messages)
            }
            Log.d("SessionViewModel", "fun loadInitialConversation conversationMessages after loadInitialConversation: $conversationMessages")
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
//        Log.d("SessionViewModel", "fun startListening startListening() called, isListening: $isListening, instance: $this, memory location: ${System.identityHashCode(this)}")
    }
    private suspend fun sendUserMessageToOpenAi(userMessage: String) {


        stopListening()
        val audioFilePathState = mutableStateOf("")
        // Add user message to the conversation state


        val userAudioFilePathState = mutableStateOf("")
        val userMessageObj = ConversationMessage("User", userMessage, userAudioFilePathState)
        conversationManager.addMessage(userMessageObj)
        conversationMessages.add(userMessageObj)


        val responseText = withExponentialBackoff(context, snackbarHostState, { // Pass snackbarHostState directly
            openAiApiService.sendMessage(conversationManager.conversation.messages)
        }, viewModelScope, onRetry = {
            viewModelScope.launch {
                sendUserMessageToOpenAi(userMessage)
            }
        }) ?: return
        Log.d("SessionViewModel", "Received response from OpenAI API: $responseText")

        val assistantAudioFilePathState = mutableStateOf("")
        val assistantMessageObj = ConversationMessage("Assistant", responseText.replace("\n", " "), assistantAudioFilePathState)
        conversationManager.addMessage(assistantMessageObj)
        conversationMessages.add(assistantMessageObj)
        autosaveConversation()

        textToSpeechServiceState?.value?.renderSpeech(responseText.replace("\n", " "), onFinish = {
            if (conversationManager.conversation.messages.isNotEmpty()) {
            mainHandler.post {
                _isAppSpeaking.value = false
                startListening()
                Log.d("SessionViewModel", "log: startListening called associated with onFinish")
            }
        }}, onStart = {
            mainHandler.post {
                stopListening()
                Log.d("SessionViewModel", "log: stopListening called associated with onStart")
            }
        }, audioFilePathState = assistantMessageObj.audioFilePath)
        _isAppSpeaking.value = true
        autosaveConversation()
    }

    fun updateMessage(index: Int, updatedMessage: ConversationMessage) {
        conversationManager.updateMessage(index, updatedMessage)
        conversationMessages[index] = updatedMessage
        autosaveConversation()
    }

    fun deleteMessage(index: Int) {
        viewModelScope.launch {
            conversationManager.deleteMessage(index)
            conversationMessages.removeAt(index)
            autosaveConversation()
        }
    }

    fun autosaveConversation() {
        viewModelScope.launch {
            conversationsManager.saveConversation(conversationManager.conversation)
            Log.d("SessionViewModel", "log: Autosaved conversation, instance: $this, memory location: ${System.identityHashCode(this)}")
        }
    }

    private fun startPeriodicListeningCheck() {
        mainHandler.postDelayed({
            if (_isListening.value && _isAppSpeaking.value) {
//                Log.d("SessionViewModel", "log: Periodic check - Restarting listening, isListening: $isListening, instance: $this, memory location: ${System.identityHashCode(this)}")
                startListening()
            }
            startPeriodicListeningCheck()
        }, 3000) // Check every 3 seconds
    }

    fun stopListening() {
        voiceTriggerDetector.stopListening()
        Log.d("SessionViewModel", "stopListening() called, isListening: $isListening, instance: $this, memory location: ${System.identityHashCode(this)}")
        _isListening.value = false
    }

    fun onTriggerWordDetected(userMessage: String) {
        // Add user message to the conversation state
        Log.d("SessionViewModel", "log: onTriggerWordDetected called")

        // Stop listening
        voiceTriggerDetector.stopListening()
        Log.d("SessionViewModel", "log: from within the OnTriggerWordDetected function, `voiceTriggerDetector.stopListening()` was just called, isListening: $isListening, instance: $this, memory location: ${System.identityHashCode(this)}")

        // Send the user message to OpenAI API and process the response
        viewModelScope.launch {
            sendUserMessageToOpenAi(userMessage) // Pass the userMessage parameter here
        }
        autosaveConversation()
    }

    fun loadConversation(conversationId: UUID) {
    Log.d("SessionViewModel", "fun loadConversation Loaded conversation ID: ${conversationId}")
        viewModelScope.launch {
            loadInitialConversation(conversationId)
        }
    }

    init {
        Log.d("SessionViewModel", "init about to run `loadInitialConversation(conversationId)`")
        loadInitialConversation(conversationId)
        startPeriodicListeningCheck()
    }

    fun loadConversationWithId(conversationId: UUID) {
        this.conversationId = conversationId
        loadInitialConversation()
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

class SessionViewModelFactory(
    private val conversationId: UUID?,
    private val context: Context,
    private val configPacksViewModel: ConfigPacksViewModel,
    private val openAiApiService: OpenAiApiService,
    private val conversationRepository: IConversationRepository,
    private val textToSpeechServiceState: MutableState<TextToSpeechService>,
    private val snackbarHostState: SnackbarHostState
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SessionViewModel::class.java)) {
            return SessionViewModel(
                conversationId,
                context,
                configPacksViewModel,
                openAiApiService,
                conversationRepository,
                textToSpeechServiceState,
                snackbarHostState
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
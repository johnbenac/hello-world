package com.example.hello_world.managers

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.example.hello_world.services.speech_to_text.VoiceTriggerDetector



enum class ListeningState {
    IDLE,
    LISTENING,
    PROCESSING
}

class ListeningManager(
    private val context: Context,
    private val triggerWord: String,
    private val onTriggerWordDetected: (String) -> Unit,
    private val mainHandler: Handler = Handler(Looper.getMainLooper()),
    private val latestPartialResult: MutableState<String?>
) {
    private val speechRecognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    private var keepListening: Boolean = true

    private val _listeningState = mutableStateOf(ListeningState.IDLE)
    val listeningState: ListeningState get() = _listeningState.value

    private val voiceTriggerDetector = VoiceTriggerDetector(
        context,
        triggerWord,
        this::handleTriggerWordDetected,
        mainHandler,
        latestPartialResult
    )

    init {
        speechRecognizer.setRecognitionListener(voiceTriggerDetector)
    }

    fun beginListening() {
        if (listeningState == ListeningState.IDLE) {
            _listeningState.value = ListeningState.LISTENING
            voiceTriggerDetector.beginListening()
            Log.d("ListeningManager", "log: within the beginListening function, voiceTriggerDetector.beginListening() was just invoked")
        }
    }

    fun endListening() {
        if (listeningState == ListeningState.LISTENING) {
            _listeningState.value = ListeningState.IDLE
            voiceTriggerDetector.endListening()
            Log.d("ListeningManager", "log: within the beginListening function, voiceTriggerDetector.endListening() was just invoked")
        }
    }

    private fun handleTriggerWordDetected(userMessage: String) {
        _listeningState.value = ListeningState.PROCESSING
        onTriggerWordDetected(userMessage)
        _listeningState.value = ListeningState.IDLE
    }
}
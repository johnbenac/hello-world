package com.example.hello_world
import android.content.Context
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.runtime.MutableState


class VoiceTriggerDetector(
    private val context: Context,
    private val triggerWord: String,
    private val onTriggerWordDetected: ((String) -> Unit),
    private val mainHandler: Handler = Handler(Looper.getMainLooper()),
    private val latestPartialResult: MutableState<String?> 
) : RecognitionListener {
    private val speechRecognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    private var keepListening: Boolean = true

    init {
        speechRecognizer.setRecognitionListener(this)
    }

    fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        speechRecognizer.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer.stopListening()
        Log.d("VoiceTriggerDetector", "log: within the stoplistening function, speechRecognizer.stopListening() was just called")
    }

    override fun onReadyForSpeech(params: Bundle) {
        // Handle when the SpeechRecognizer is ready to receive speech input
    }

    override fun onBeginningOfSpeech() {
        // Handle when the user starts speaking
    }

    override fun onRmsChanged(rmsdB: Float) {
        // Handle changes in the received sound level (RMS)
    }

    override fun onBufferReceived(buffer: ByteArray) {
        // Handle more sound data being available
    }

    override fun onEndOfSpeech() {
        // Handle when the user stops speaking
    }

    override fun onError(error: Int) {
        // Handle errors that may occur during speech recognition
    }

    override fun onResults(results: Bundle) {
        val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        Log.d("VoiceTriggerDetector", "Final Results: $matches")
        matches?.let { processResults(it) }
    
        // Restart listening if the trigger word is not detected and the flag is set to keep listening
        if (keepListening) {
            mainHandler.post { startListening() }
        }
    }

    override fun onPartialResults(partialResults: Bundle) {
        val matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
//        Log.d("VoiceTriggerDetector", "Partial Results: $matches")
        Toast.makeText(context, "Partial Results: $matches", Toast.LENGTH_SHORT).show()
    
        // Set the latest partial result
        latestPartialResult.value = matches?.firstOrNull()
    
        // Remove the startListening() call from here
    }

    override fun onEvent(eventType: Int, params: Bundle) {
        // Handle any events that may occur during speech recognition
    }



    private fun processResults(matches: ArrayList<String>) {
        for (result in matches) {
            if (result.contains(triggerWord, ignoreCase = true)) {
                // Trigger word detected, handle the event here
                Log.d("VoiceTriggerDetector", "log: Trigger word detected")
                val userMessage = result.replace(Regex("(?i)$triggerWord"), "").trim() // Use a regex to remove the trigger word and extra spaces
                onTriggerWordDetected(userMessage) // Pass the user message here
                break
            }
        }
    }
}
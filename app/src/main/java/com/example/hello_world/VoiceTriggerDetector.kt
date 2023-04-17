import android.content.Context
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.content.Intent
import android.os.Bundle

class VoiceTriggerDetector(private val context: Context,
private val triggerWord: String,
private val onTriggerWordDetected: (() -> Unit)
) : RecognitionListener {
    private val speechRecognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

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
        // Handle the final recognition results
        val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        matches?.let { processResults(it) }
    }

    override fun onPartialResults(partialResults: Bundle) {
        // Handle partial recognition results
        val matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        matches?.let { processResults(it) }
    }

    override fun onEvent(eventType: Int, params: Bundle) {
        // Handle any events that may occur during speech recognition
    }

    private fun processResults(matches: ArrayList<String>) {
        for (result in matches) {
            if (result.contains(triggerWord, ignoreCase = true)) {
                // Trigger word detected, handle the event here
                break
            }
        }
    }
}
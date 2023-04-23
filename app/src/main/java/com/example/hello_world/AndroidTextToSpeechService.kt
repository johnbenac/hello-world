package com.example.hello_world
import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.UUID
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.compose.runtime.MutableState
import java.io.File
import java.util.Locale



class AndroidTextToSpeechService(private val context: Context) : TextToSpeechService, TextToSpeech.OnInitListener {
    private var lastGeneratedAudioFilePath: String? = null
    private var textToSpeech: TextToSpeech = TextToSpeech(context, this)
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale.getDefault())
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Handle the case where the default language data or the language itself is not supported
            }
        } else {
            // Handle the case where TextToSpeech initialization failed
        }
    }
    //    private fun playSavedAudioFile(filePath: String, onStart: (() -> Unit)?, onFinish: (() -> Unit)?) {
//        val mediaPlayer = MediaPlayer().apply {
//            setDataSource(filePath)
//            setOnPreparedListener {
//                onStart?.invoke()
//                start()
//            }
//            setOnCompletionListener {
//                onFinish?.invoke()
//                release()
//            }
//            prepareAsync()
//        }
//    }
    override fun speak(text: String, onFinish: (() -> Unit)?, onStart: (() -> Unit)?, audioFilePathState: MutableState<String>): String {
        val utteranceId = UUID.randomUUID().toString()
        Log.d("AndroidTextToSpeechService", "synthesizeToFile called with utteranceId: $utteranceId")
        val filePath = File(context.getExternalFilesDir(null), "google_tts.mp3").absolutePath
        textToSpeech.synthesizeToFile(text, null, File(filePath), UUID.randomUUID().toString())
        textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) {
                onStart?.invoke()
                Log.d("AndroidTextToSpeechService", "log: onStart called")
            }
            override fun onDone(utteranceId: String) {
                Log.d("AndroidTextToSpeechService", "onDone called with utteranceId: $utteranceId")
                Log.d("AndroidTextToSpeechService", "Audio file generated: $filePath")
                audioFilePathState.value = filePath
                lastGeneratedAudioFilePath = filePath
//                Log.d("AndroidTextToSpeechService","about to attempt to play audio file")
//                playSavedAudioFile(filePath, onStart, onFinish) // Use filePath instead of File(context.cacheDir, "google_tts.mp3").absolutePath
//                Log.d("AndroidTextToSpeechService","just attempted to play audio file")
            }
            override fun onError(utteranceId: String) {
                Log.d("AndroidTextToSpeechService", "log: onError called")
            }
        })
//        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        lastGeneratedAudioFilePath = filePath
        return filePath
    }
    override fun getAudioFilePath(): String {
        return lastGeneratedAudioFilePath ?: ""
    }
    override fun stop() {
        textToSpeech.stop()
    }
    override fun shutdown() {
        textToSpeech.shutdown()
    }
}

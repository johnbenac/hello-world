package com.example.hello_world
import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.UUID
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class AndroidTextToSpeechService(private val context: Context) : TextToSpeechService, TextToSpeech.OnInitListener {
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

    override fun speak(text: String, onFinish: (() -> Unit)?, onStart: (() -> Unit)?) {
        val utteranceId = UUID.randomUUID().toString()

        textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) {
                onStart?.invoke()
                Log.d("AndroidTextToSpeechService", "log: onStart called")
            }

            override fun onDone(utteranceId: String) {
                onFinish?.invoke()
                Log.d("AndroidTextToSpeechService", "log: onDone called")
            }

            override fun onError(utteranceId: String) {
                Log.d("AndroidTextToSpeechService", "log: onError called")
            }
        })

        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }


    override fun stop() {
        textToSpeech.stop()
    }

    override fun shutdown() {
        textToSpeech.shutdown()
    }
}
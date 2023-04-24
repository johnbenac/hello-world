package com.example.hello_world

import android.media.MediaPlayer
import okhttp3.*
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
//import android.os.ParcelFileDescriptor
import java.io.File
import java.io.FileOutputStream
import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import java.util.UUID


class ElevenLabsTextToSpeechService(
    private val apiKey: String,
    private val voiceId: String,
    private val context: Context
) : TextToSpeechService {
    private var lastGeneratedAudioFilePath: String? = null
    private val client = OkHttpClient()
    override fun speak(text: String, onFinish: (() -> Unit)?, onStart: (() -> Unit)?, audioFilePathState: MutableState<String>): String {
        val fileName = "elevenlabs_tts_${UUID.randomUUID()}.mp3"
        val filePath = File(context.getExternalFilesDir(null), fileName).absolutePath
        val requestBody = createTtsRequestBody(text)
        val request = buildTtsRequest(requestBody)
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("ElevenLabsTextToSpeechService", "onFailure called")
                Log.e("ElevenLabsTextToSpeechService", "onFailure called: ${e.message}", e)
            }
            override fun onResponse(call: Call, response: Response) {
                Log.d("ElevenLabsTextToSpeechService", "onResponse called")
                handleTtsResponse(response, filePath, onStart, onFinish, audioFilePathState)
            }
        })
        lastGeneratedAudioFilePath = filePath
        return filePath
    }
    override fun getAudioFilePath(): String {
        return lastGeneratedAudioFilePath ?: ""
    }
    private fun createTtsRequestBody(text: String): RequestBody {
        val json = """
            {
                "text": "$text",
                "voice_settings": {
                    "stability": 0,
                    "similarity_boost": 0
                }
            }
        """.trimIndent()
        Log.d("ElevenLabsTextToSpeechService", "createTtsRequestBody called")
        return RequestBody.create("application/json".toMediaType(), json)
    }
    private fun buildTtsRequest(requestBody: RequestBody): Request {
        Log.d("ElevenLabsTextToSpeechService", "buildTtsRequest called")
        return Request.Builder()
            .url("https://api.elevenlabs.io/v1/text-to-speech/$voiceId")
            .addHeader("accept", "audio/mpeg")
            .addHeader("xi-api-key", apiKey)
            .post(requestBody)
            .build()
    }
    private fun handleTtsResponse(
        response: Response,
        filePath: String,
        onStart: (() -> Unit)?,
        onFinish: (() -> Unit)?,
        audioFilePathState: MutableState<String> 
    ) {
        Log.d("ElevenLabsTextToSpeechService", "handleTtsResponse called")
        if (response.isSuccessful) {
            response.body?.byteStream()?.let { inputStream ->
                FileOutputStream(File(filePath)).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                Log.d("ElevenLabsTextToSpeechService", "Audio file saved: $filePath")
                audioFilePathState.value = filePath
//                setupMediaPlayer(filePath, onStart, onFinish)
            }
        } else {
            // Handle the unsuccessful response
            // ...
        }
    }
    //    private fun setupMediaPlayer(filePath: String, onStart: (() -> Unit)?, onFinish: (() -> Unit)?) {
//        Log.d("ElevenLabsTextToSpeechService", "setupMediaPlayer called")
//        val mediaPlayer = MediaPlayer().apply {
//            setDataSource(filePath)
//            setOnPreparedListener {
//                onStart?.invoke()
//                Log.d("ElevenLabsTextToSpeechService", "mediaPlayer onPrepared")
//                start()
//                Log.d("ElevenLabsTextToSpeechService", "mediaPlayer started")
//            }
//            setOnCompletionListener {
//                onFinish?.invoke()
//                Log.d("ElevenLabsTextToSpeechService", "mediaPlayer onCompletion")
//                release()
//                Log.d("ElevenLabsTextToSpeechService", "mediaPlayer released")
//            }
//            prepareAsync()
//            Log.d("ElevenLabsTextToSpeechService", "mediaPlayer preparedAsync")
//        }
//    }
    override fun stop() {
        // Implement stop functionality if needed
    }
    override fun shutdown() {
        // Implement shutdown functionality if needed
    }
}

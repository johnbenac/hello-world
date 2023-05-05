package com.example.hello_world.services.text_to_speech

import android.content.Context
import android.util.Log
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.MutableState
import com.example.hello_world.services.media_playback.MediaPlaybackManager
import com.example.hello_world.withExponentialBackoff
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit

class ElevenLabsTextToSpeechService(
    private val apiKey: String,
    private val voiceId: String,
    private val context: Context,
    override val mediaPlaybackManager: MediaPlaybackManager,
    private val onPlaybackFinished: () -> Unit,
    private val snackbarHostState: SnackbarHostState
) : TextToSpeechService {
    private var lastGeneratedAudioFilePath: String? = null
    private val client = OkHttpClient.Builder()
        .readTimeout(600, TimeUnit.SECONDS)
        .writeTimeout(600, TimeUnit.SECONDS)
        .connectTimeout(600, TimeUnit.SECONDS)
        .build()

    override fun renderSpeech(text: String, onFinish: (() -> Unit)?, onStart: (() -> Unit)?, audioFilePathState: MutableState<String>): String {
        // Call the new renderSpeechInternal method
        renderSpeechInternal(GlobalScope, text, onFinish, onStart, audioFilePathState)
        return lastGeneratedAudioFilePath ?: ""
    }

    // New private method that accepts CoroutineScope
    private fun renderSpeechInternal(coroutineScope: CoroutineScope, text: String, onFinish: (() -> Unit)?, onStart: (() -> Unit)?, audioFilePathState: MutableState<String>): String {
        val fileName = "elevenlabs_tts_${UUID.randomUUID()}.mp3"
        val filePath = File(context.getExternalFilesDir(null), fileName).absolutePath

        // Wrap API call inside withExponentialBackoff function
        coroutineScope.launch {
            withExponentialBackoff(context, snackbarHostState, apiRequest = { // Use snackbarHostState directly
                val requestBody = createTtsRequestBody(text)
                val request = buildTtsRequest(requestBody)
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    throw IOException("Unexpected code $response - ${response.body?.string()}")
                }

                handleTtsResponse(response, filePath, onStart, onFinish, audioFilePathState)
            }, coroutineScope = coroutineScope)
        }

        lastGeneratedAudioFilePath = filePath
        return filePath
    }
    override fun getAudioFilePath(): String {
        return lastGeneratedAudioFilePath ?: ""
    }

    data class TtsRequestBody(
        val text: String,
        val voice_settings: Map<String, Int>
    )
    private fun createTtsRequestBody(text: String): RequestBody {
        val requestBodyData = TtsRequestBody(
            text = text,
            voice_settings = mapOf("stability" to 0, "similarity_boost" to 0)
        )

        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter = moshi.adapter(TtsRequestBody::class.java)
        val json = jsonAdapter.toJson(requestBodyData)

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
        onFinish: (() -> Unit)?, // Add this line
        audioFilePathState: MutableState<String>
    ) {
        Log.d("ElevenLabsTextToSpeechService", "handleTtsResponse called")
        if (response.isSuccessful) {
            response.body?.byteStream()?.let { inputStream ->
                FileOutputStream(File(filePath)).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                Log.d("ElevenLabsTextToSpeechService", "Audio file saved: $filePath")
                updateAudioFilePathState(filePath, audioFilePathState)
                mediaPlaybackManager.playAudio(filePath, context, onFinish = {
                    onFinish?.invoke()
                    onPlaybackFinished()
//                    voiceTriggerDetector.stopListening()
                    Log.d(
                        "ElevenLabsTextToSpeechService", "\nonFinish?.invoke()\n" +
                                "            onPlaybackFinished()\nwas just called"
                    )
                }) // Pass onFinish here
            }
        } else {
            // Handle the unsuccessful response
            // ...
        }
        mediaPlaybackManager.playAudio(filePath, context, onFinish = {
            onFinish?.invoke()
            onPlaybackFinished()
            Log.d(
                "ElevenLabsTextToSpeechService", "\nonFinish?.invoke()\n" +
                        "            onPlaybackFinished()\nwas just called"
            )
        }) // Pass onFinish here
    }

    override fun stop() {
        // Implement stop functionality if needed
    }
    override fun shutdown() {
        // Implement shutdown functionality if needed
    }
    private fun updateAudioFilePathState(audioFilePath: String, audioFilePathState: MutableState<String>) {
        if (audioFilePathState.value.isEmpty()) {
            audioFilePathState.value = audioFilePath
        }
    }
}
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

class ElevenLabsTextToSpeechService(
    private val apiKey: String,
    private val voiceId: String,
    private val context: Context
) : TextToSpeechService {
    private val client = OkHttpClient()

    override fun speak(text: String, onFinish: (() -> Unit)?, onStart: (() -> Unit)?) {
        val requestBody = RequestBody.create(
            "application/json".toMediaType(),
            """
        {
            "text": "$text",
            "voice_settings": {
                "stability": 0,
                "similarity_boost": 0
            }
        }
        """.trimIndent()
        )

        val request = Request.Builder()
            .url("https://api.elevenlabs.io/v1/text-to-speech/$voiceId")
            .addHeader("accept", "audio/mpeg")
            .addHeader("xi-api-key", apiKey)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle the error
                Log.d("ElevenLabsTextToSpeechService", "log: onFailure called")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.byteStream()?.let { inputStream ->
                        val tempFile = File.createTempFile("tts", "mp3", context.cacheDir).apply {
                            deleteOnExit()
                            Log.d("ElevenLabsTextToSpeechService", "log: tempFile created")
                        }
                        FileOutputStream(tempFile).use { outputStream ->
                            inputStream.copyTo(outputStream)
                            Log.d("ElevenLabsTextToSpeechService", "log: inputStream copied to outputStream")
                        }

                        val mediaPlayer = MediaPlayer().apply {
                            setDataSource(tempFile.absolutePath)
                            setOnPreparedListener {
                                onStart?.invoke()
                                start()
                                Log.d("ElevenLabsTextToSpeechService", "log: mediaPlayer started")
                            }
                            setOnCompletionListener {
                                onFinish?.invoke()
                                release()
                                Log.d("ElevenLabsTextToSpeechService", "log: mediaPlayer released")
                            }
                            prepareAsync()
                            Log.d("ElevenLabsTextToSpeechService", "log: mediaPlayer preparedAsync")
                        }
                    }
                } else {
                    // Handle the unsuccessful response
                    Log.d("ElevenLabsTextToSpeechService", "log: onResponse called, thinks it's an unsuccesful response")
                }
            }
        })
    }

    override fun stop() {
        // Implement stop functionality if needed
    }

    override fun shutdown() {
        // Implement shutdown functionality if needed
    }
}
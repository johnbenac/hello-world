package com.example.hello_world.services.text_to_speech

import androidx.compose.runtime.MutableState
import com.example.hello_world.services.media_playback.MediaPlaybackManager


interface TextToSpeechService {
    val mediaPlaybackManager: MediaPlaybackManager
    fun renderSpeech(text: String, onFinish: (() -> Unit)?, onStart: (() -> Unit)?, audioFilePathState: MutableState<String>): String
    fun stop()
    fun getAudioFilePath(): String
    fun shutdown()
}

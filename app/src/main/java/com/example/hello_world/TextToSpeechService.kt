package com.example.hello_world

import androidx.compose.runtime.MutableState

interface TextToSpeechService {
    fun speak(text: String, onFinish: (() -> Unit)?, onStart: (() -> Unit)?, audioFilePathState: MutableState<String>): String
    fun stop()
    fun getAudioFilePath(): String
    fun shutdown()
}
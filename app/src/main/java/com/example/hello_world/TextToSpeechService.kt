package com.example.hello_world

interface TextToSpeechService {
    fun speak(text: String, onFinish: (() -> Unit)? = null, onStart: (() -> Unit)? = null)
    fun stop()
    fun shutdown()
}
package com.example.hello_world

import android.content.Context


interface MediaPlaybackManager {
    fun playAudio(filePath: String, context: Context)
    fun isPlaying(): Boolean
    fun pause()
    // Add other media control methods as needed
}

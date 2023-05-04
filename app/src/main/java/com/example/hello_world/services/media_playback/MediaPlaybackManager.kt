package com.example.hello_world.services.media_playback

import android.content.Context


interface MediaPlaybackManager {
    fun playAudio(filePath: String, context: Context, onFinish: (() -> Unit)? = null)
    fun isPlaying(): Boolean
    fun pause()
    // Add other media control methods as needed
    fun seekForward()
    fun seekBackward()

    fun storePlaybackPosition()
    fun resetPlaybackPosition()
}

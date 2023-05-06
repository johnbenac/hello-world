
package com.example.hello_world.services.media_playback

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.widget.MediaController
import android.widget.Toast

class AndroidMediaPlaybackManager : MediaPlaybackManager {
    var mediaPlayer: MediaPlayer? = null
    private var mediaController: MediaController? = null
    private var currentFilePath: String? = null
    private var playbackPosition: Int = 0

    override fun seekForward() {
        val currentPosition = mediaPlayer?.currentPosition ?: 0
        val newPosition = currentPosition + 10000 // Skip forward by 10 seconds
        mediaPlayer?.seekTo(newPosition)
    }

    override fun seekBackward() {
        val currentPosition = mediaPlayer?.currentPosition ?: 0
        val newPosition = currentPosition - 10000 // Skip backward by 10 seconds
        mediaPlayer?.seekTo(newPosition)
    }
    override fun pause() {
        mediaPlayer?.apply {
            playbackPosition = currentPosition // Save the playback position
            Log.d("AndroidMediaPlaybackManager", "Pausing audio at position: $playbackPosition")
            pause()
        }
    }
    override fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }
    override fun playAudio(filePath: String, context: Context, onFinish: (() -> Unit)?) {
        if (filePath.isEmpty()) {
            Toast.makeText(context, "Audio file not loaded", Toast.LENGTH_SHORT).show()
            return
        }
        if (mediaPlayer != null && currentFilePath == filePath) {
            mediaPlayer?.apply {
                Log.d("AndroidMediaPlaybackManager", "Resuming audio at position: $playbackPosition")
                Log.d("AndroidMediaPlaybackManager", "memory address: $this")
                seekTo(playbackPosition) // Set the playback position
                start()
                Log.d("AndroidMediaPlaybackManager", "Resuming audio at position: $playbackPosition")
            }
        } else {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                Log.d("AndroidMediaPlaybackManager", "Playing audio from file: $filePath")
                setDataSource(filePath)
                setOnCompletionListener {
                    resetPlaybackPosition()
                    onFinish?.invoke()
                    seekTo(0)
                }
                prepare()
                start()

            }
            currentFilePath = filePath // Update the currentFilePath here
        }
        mediaController?.hide()
        mediaController = MediaController(context)
        mediaController?.setMediaPlayer(object : MediaController.MediaPlayerControl {
            private var isPaused = false
            override fun start() {
                if (isPaused) {
                    mediaPlayer?.start()
                    isPaused = false
                }
            }

            override fun pause() {
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.pause()
                    isPaused = true
                }
            }
            // Implement other required methods
            override fun getDuration(): Int = mediaPlayer?.duration ?: 0
            override fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0
            override fun getBufferPercentage(): Int = 0
            override fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false
            override fun seekTo(position: Int) {
                mediaPlayer?.seekTo(position)
            }
            override fun canPause(): Boolean {
                // Return true if your media player can pause, otherwise return false
                return true
            }
            override fun getAudioSessionId(): Int {
                // Return the audio session ID of your media player or 0 if not available
                return mediaPlayer?.audioSessionId ?: 0
            }
            override fun canSeekBackward(): Boolean {
                // Return true if your media player can seek backward, otherwise return false
                return true
            }
            override fun canSeekForward(): Boolean {
                // Return true if your media player can seek forward, otherwise return false
                return true
            }
        })
        mediaController?.show()
    }


    override fun storePlaybackPosition() {
        mediaPlayer?.apply {
            playbackPosition = currentPosition
            Log.d("AndroidMediaPlaybackManager", "Storing playback position: $playbackPosition")
        }
    }

    override fun resetPlaybackPosition() {
        playbackPosition = 0
        Log.d("AndroidMediaPlaybackManager", "Resetting playback position to: $playbackPosition")
    }
}


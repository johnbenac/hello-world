package com.example.hello_world

import MediaPlaybackManager
import android.content.Context
import android.media.MediaPlayer
import android.util.Log
//import android.media.session.MediaController
//import android.content.Context
//import android.media.MediaPlayer
import android.widget.MediaController

class AndroidMediaPlaybackManager : MediaPlaybackManager {
    private var mediaPlayer: MediaPlayer? = null
    private var mediaController: MediaController? = null

    override fun playAudio(filePath: String, context: Context) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            Log.d("AndroidMediaPlaybackManager", "Playing audio from file: $filePath")
            setDataSource(filePath)
            prepare()
            Log.d("AndroidMediaPlaybackManager", "MediaPlayer prepared") // Add this line
            start()
            Log.d("AndroidMediaPlaybackManager", "MediaPlayer started") // Add this line
        }
        mediaController?.hide()
        mediaController = MediaController(context)
        mediaController?.setMediaPlayer(object : MediaController.MediaPlayerControl {
            override fun start() {
                mediaPlayer?.start()
                Log.d("AndroidMediaPlaybackManager", "MediaPlayerControl start() called") // Add this line
            }

            override fun pause() {
                mediaPlayer?.pause()
                Log.d("AndroidMediaPlaybackManager", "MediaPlayerControl pause() called") // Add this line
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
}
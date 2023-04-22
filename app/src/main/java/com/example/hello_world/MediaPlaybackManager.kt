import android.content.Context

interface MediaPlaybackManager {
    fun playAudio(filePath: String, context: Context)
    // Add other media control methods as needed
}
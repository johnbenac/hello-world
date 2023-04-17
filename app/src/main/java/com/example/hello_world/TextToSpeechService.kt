interface TextToSpeechService {
    fun speak(text: String, onFinish: (() -> Unit)? = null)
    fun stop()
    fun shutdown()
}
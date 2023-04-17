package com.example.hello_world
import android.util.Log
import java.util.Locale
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

data class OpenAiMessage(val role: String, val content: String)

data class OpenAiApiRequest(
    val messages: List<OpenAiMessage>,
    val temperature: Double,
    val max_tokens: Int,
    val top_p: Int,
    val frequency_penalty: Double,
    val presence_penalty: Double,
    val model: String,
    val stream: Boolean
)

class OpenAiApiService(private val apiKey: String) {
    private val client = OkHttpClient()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    suspend fun sendMessage(conversationHistory: List<ConversationMessage>): String = suspendCancellableCoroutine { continuation ->
        val messages = mutableListOf(OpenAiMessage("system", "you are an ai assistant named jake"))

        conversationHistory.forEach { message ->
            messages.add(OpenAiMessage(message.sender.toLowerCase(Locale.ROOT), message.message))
        }
    
        val requestJson = moshi.adapter(OpenAiApiRequest::class.java).toJson(
            OpenAiApiRequest(
                messages = messages,
                temperature = 0.9,
                max_tokens = 100,
                top_p = 1,
                frequency_penalty = 0.0,
                presence_penalty = 0.1,
                model = "gpt-3.5-turbo",
                stream = false
            )
        )
        Log.d("OpenAiApiService", "API Request: $requestJson")
    
        val requestBody = requestJson.toRequestBody("application/json; charset=utf-8".toMediaType())
    
        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody)
            .build()
    
        val call = client.newCall(request)
    
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (continuation.isCancelled) return
                continuation.resumeWithException(e)
            }
    
            override fun onResponse(call: Call, response: Response) {
                if (continuation.isCancelled) return
            
                if (!response.isSuccessful) {
                    continuation.resumeWithException(IOException("Unexpected code $response"))
                } else {
                    val responseBody = response.body?.string()
                    Log.d("OpenAiApiService", "Received JSON: $responseBody") // Add this line to log the received JSON
                    val jsonAdapter = moshi.adapter(OpenAiApiResponse::class.java)
                    val apiResponse = jsonAdapter.fromJson(responseBody)
            
                    continuation.resumeWith(Result.success(apiResponse?.choices?.firstOrNull()?.message?.content ?: ""))
                }
            }
        })
    }
}
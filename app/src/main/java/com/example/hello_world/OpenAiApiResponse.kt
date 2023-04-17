package com.example.hello_world
import com.example.hello_world.OpenAiApiResponse

data class OpenAiApiResponse(val choices: List<OpenAiApiChoice>)

data class OpenAiApiChoice(val message: OpenAiApiMessage)

data class OpenAiApiMessage(val role: String, val content: String)
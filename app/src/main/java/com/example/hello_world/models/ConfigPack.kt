package com.example.hello_world.models

data class ConfigPack(
    val name: String,
    val systemMessage: String,
    val maxLength: Int,
    val temperature: Double,
    val frequencyPenalty: Double,
    val presencePenalty: Double,
    val model: String
) {
    companion object {
        val defaultConfigPack = ConfigPack(
            name = "Jake",
            systemMessage = "I am an AI assistant named Jake.",
            maxLength = 100,
            temperature = 0.9,
            frequencyPenalty = 0.0,
            presencePenalty = 0.1,
            model = "gpt-3.5-turbo"
        )
    }
}
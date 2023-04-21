package com.example.hello_world

data class Profile(
    val name: String,
    val systemMessage: String,
    val maxLength: Int,
    val temperature: Double,
    val frequencyPenalty: Double,
    val presencePenalty: Double,
    val model: String
)
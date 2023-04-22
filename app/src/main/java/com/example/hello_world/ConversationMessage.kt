package com.example.hello_world

import androidx.compose.runtime.MutableState

data class ConversationMessage(val sender: String, val message: String, val audioFilePath: MutableState<String>)
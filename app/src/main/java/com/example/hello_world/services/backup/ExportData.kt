package com.example.hello_world.services.backup

import com.example.hello_world.models.Conversation

data class ExportData(
    val conversations: List<Conversation>
)

package com.example.hello_world.services.backup

interface IBackup {
    suspend fun exportConversations(): String
    suspend fun importConversations(json: String)
}
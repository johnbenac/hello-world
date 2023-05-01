package com.example.hello_world.ui.saved_conversations.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hello_world.data.repository.IConversationRepository
import com.example.hello_world.managers.ConversationsManager
import com.example.hello_world.models.ConfigPack
import com.example.hello_world.models.Conversation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class SavedConversationsViewModel(
    private val conversationRepository: IConversationRepository
) : ViewModel() {
    private val _savedConversations = MutableStateFlow<List<Conversation>>(emptyList())
    val savedConversations: StateFlow<List<Conversation>> = _savedConversations
    private val conversationsManager = ConversationsManager(conversationRepository)

    init {
        viewModelScope.launch {
            _savedConversations.value = loadSavedConversations()
        }
    }

    suspend fun createNewConversation(): UUID {
        val newConversation = Conversation(configPack = ConfigPack.defaultConfigPack)
        conversationsManager.saveConversation(newConversation) // Modify this line
        return newConversation.id
    }

    private suspend fun loadSavedConversations(): List<Conversation> {
        // Replace the TODO with the actual implementation
        return conversationRepository.loadAllConversations()
    }

    // Implement methods for deleting saved conversations
    fun deleteConversation(conversationId: UUID) {
        viewModelScope.launch {
            conversationRepository.deleteConversation(conversationId)
            _savedConversations.value = loadSavedConversations()
        }
    }
}
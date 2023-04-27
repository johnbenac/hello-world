package com.example.hello_world

import com.example.hello_world.Conversation
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class SavedConversationsViewModel(
    private val conversationRepository: IConversationRepository
) : ViewModel() {
    private val _savedConversations = MutableStateFlow<List<Conversation>>(emptyList())
    val savedConversations: StateFlow<List<Conversation>> = _savedConversations

    init {
        viewModelScope.launch {
            _savedConversations.value = loadSavedConversations()
        }
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
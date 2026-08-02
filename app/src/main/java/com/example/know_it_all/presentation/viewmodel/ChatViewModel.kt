package com.example.know_it_all.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.know_it_all.data.repository.ChatMessage
import com.example.know_it_all.data.repository.FirebaseChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: FirebaseChatRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    fun observeMessages(swapId: String) {
        viewModelScope.launch {
            chatRepository.observeMessages(swapId).collect { msgs ->
                _messages.value = msgs
            }
        }
    }

    fun sendMessage(swapId: String, senderId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _isSending.value = true
            chatRepository.sendMessage(swapId, senderId, text)
            _isSending.value = false
        }
    }
}
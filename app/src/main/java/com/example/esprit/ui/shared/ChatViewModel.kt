package com.example.esprit.ui.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.Message
import com.example.esprit.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repo: MessageRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private var peerId: String = ""

    private var pollingStarted = false

    fun init(peerId: String) {
        this.peerId = peerId

        // Charge une première fois
        viewModelScope.launch {
            loadConversation()
        }

        // Polling sécurisé
        if (!pollingStarted) {
            pollingStarted = true
            viewModelScope.launch {
                while (isActive) {
                    delay(2000)
                    loadConversation()
                }
            }
        }
    }

    private suspend fun loadConversation() {
        try {
            val msgs = repo.getConversation(peerId)
            _messages.value = msgs
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendMessage(content: String) {
        viewModelScope.launch {
            try {
                val sent = repo.sendMessage(peerId, content)

                // Ajoute le message immédiatement
                _messages.value = _messages.value + sent

                delay(200)
                loadConversation()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

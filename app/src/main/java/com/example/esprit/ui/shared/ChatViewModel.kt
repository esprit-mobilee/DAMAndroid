package com.example.esprit.ui.shared


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.ChatSummaryResponse
import com.example.esprit.model.Message
import com.example.esprit.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repo: MessageRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _summary = MutableStateFlow<ChatSummaryResponse?>(null)
    val summary: StateFlow<ChatSummaryResponse?> = _summary

    private var peerId: String = ""
    private var currentUserId: String = ""
    private var pollingStarted = false

    fun setCurrentUser(id: String) {
        currentUserId = id
        println("🔥 ViewModel → currentUserId SET = $currentUserId")
    }

    fun init(peerId: String, currentUserId: String) {
        this.peerId = peerId
        this.currentUserId = currentUserId

        println("🔥 ChatViewModel INIT → user=$currentUserId peer=$peerId")

        // Charger immédiatement les messages
        viewModelScope.launch { loadConversation() }

        // Démarrer le polling une seule fois
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
        if (currentUserId.isBlank()) {
            println("❌ loadConversation: userId EMPTY")
            return
        }

        try {
            val msgs = repo.getConversationForUser(currentUserId, peerId)
            _messages.value = msgs
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendMessage(content: String) {
        if (currentUserId.isBlank()) {
            println("❌ sendMessage: userId EMPTY")
            return
        }

        viewModelScope.launch {
            try {
                repo.sendMessageForUser(currentUserId, peerId, content)
                loadConversation()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ✅ RÉSUMÉ COMPLET IA (fonctionnel)
    fun summarizeAll() {
        println("⚡ summarizeAll() CLICKED → user=$currentUserId peer=$peerId")

        viewModelScope.launch {
            try {
                val res = repo.summarizeAllMessages(currentUserId, peerId)
                println("📌 Résultat IA → $res")
                _summary.value = res
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearSummary() {
        println("🧹 Clearing summary popup")
        _summary.value = null
    }

    fun startRecording() { println("🎙️ Recording (not implemented)") }
    fun stopRecording() { println("🛑 Recording stopped (not implemented)") }
}

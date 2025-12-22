package com.example.esprit.ui.shared

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.ConversationResponse
import com.example.esprit.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConversationsViewModel @Inject constructor(
    private val repo: MessageRepository
) : ViewModel() {

    var conversations = mutableStateListOf<ConversationResponse>()
        private set

    var loading by mutableStateOf(true)

    fun load(userId: String) {
        println("🔥 [VM] load() called with userId = $userId")
        loading = true

        viewModelScope.launch {
            try {
                println("➡️ [VM] Calling repository.getUserConversations($userId)")
                val data = repo.getUserConversations(userId)

                println("📩 [VM] Conversations received = ${data.size}")

                conversations.clear()
                conversations.addAll(data)

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                loading = false
            }
        }
    }
}

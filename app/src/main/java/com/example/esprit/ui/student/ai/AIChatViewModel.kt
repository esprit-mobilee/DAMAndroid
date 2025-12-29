<<<<<<< HEAD
package com.example.esprit.ui.student.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.InternshipOffer
import com.example.esprit.model.ai.AIMessage
import com.example.esprit.model.ai.ChatSession
import com.example.esprit.service.AIService
import com.example.esprit.service.ChatHistoryManager
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * UI State for AI Chat Screen
 */
data class AIChatUiState(
    val messages: List<AIMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentSessionId: String? = null
)

/**
 * ViewModel for AI Chat functionality
 * Manages conversation state and AI service interactions
 */
@HiltViewModel
class AIChatViewModel @Inject constructor(
    private val chatHistoryManager: ChatHistoryManager,
    private val repository: com.example.esprit.repository.InternshipOfferRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AIChatUiState())
    val uiState: StateFlow<AIChatUiState> = _uiState.asStateFlow()

    private val gson = Gson()

    /**
     * Load an existing chat session
     */
    fun loadSession(session: ChatSession) {
        _uiState.value = _uiState.value.copy(
            currentSessionId = session.id,
            messages = session.messages,
            error = null
        )
    }

    /**
     * Start a new conversation
     */
    fun startNewSession() {
        _uiState.value = AIChatUiState(
            currentSessionId = UUID.randomUUID().toString(),
            messages = emptyList()
        )
    }

    /**
     * Send a message to the AI
     * @param text User's message text
     * @param offers Optional explicit list of offers (if passed from UI)
     */
    fun sendMessage(text: String, offers: List<InternshipOffer> = emptyList()) {
        if (text.isBlank()) return

        viewModelScope.launch {
            // Add user message
            val userMessage = AIMessage(text = text, isUser = true)
            val updatedMessages = _uiState.value.messages + userMessage
            
            _uiState.value = _uiState.value.copy(
                messages = updatedMessages,
                isLoading = true,
                error = null
            )

            // Save session with user message
            saveCurrentSession()

            // Convert message history to API format
            val history = updatedMessages.map { msg ->
                mapOf(
                    "role" to if (msg.isUser) "user" else "assistant",
                    "content" to msg.text
                )
            }

            // Get offers for context: use passed offers OR fetch from repository
            val offersContext = if (offers.isNotEmpty()) {
                offers
            } else {
                try {
                    // Fetch all offers from repository to ensure AI has context
                    val resource = repository.getAllOffers()
                    if (resource is com.example.esprit.util.Resource.Success) {
                        resource.data ?: emptyList()
                    } else {
                        emptyList()
                    }
                } catch (e: Exception) {
                    emptyList()
                }
            }

            // Serialize internship offers to JSON context
            val context = if (offersContext.isNotEmpty()) {
                try {
                    gson.toJson(offersContext)
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }

            // Call AI service
            val result = AIService.sendMessageWithHistory(history, context)

            result.fold(
                onSuccess = { reply ->
                    // Add AI response
                    val aiMessage = AIMessage(text = reply, isUser = false)
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + aiMessage,
                        isLoading = false
                    )
                    saveCurrentSession()
                },
                onFailure = { error ->
                    // Handle error
                    val errorMessage = "Erreur IA : ${error.message ?: "Erreur inconnue"}"
                    val errorAiMessage = AIMessage(text = errorMessage, isUser = false)
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + errorAiMessage,
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Save the current session to history
     */
    private fun saveCurrentSession() {
        val currentState = _uiState.value
        if (currentState.messages.isEmpty()) return

        val sessionId = currentState.currentSessionId ?: UUID.randomUUID().toString()
        
        // Update session ID if it was null
        if (currentState.currentSessionId == null) {
            _uiState.value = currentState.copy(currentSessionId = sessionId)
        }

        val session = ChatSession(
            id = sessionId,
            date = System.currentTimeMillis(),
            messages = currentState.messages
        )

        chatHistoryManager.saveSession(session)
    }
}
=======
package com.example.esprit.ui.student.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.InternshipOffer
import com.example.esprit.model.ai.AIMessage
import com.example.esprit.model.ai.ChatSession
import com.example.esprit.service.AIService
import com.example.esprit.service.ChatHistoryManager
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * UI State for AI Chat Screen
 */
data class AIChatUiState(
    val messages: List<AIMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentSessionId: String? = null
)

/**
 * ViewModel for AI Chat functionality
 * Manages conversation state and AI service interactions
 */
@HiltViewModel
class AIChatViewModel @Inject constructor(
    private val chatHistoryManager: ChatHistoryManager,
    private val repository: com.example.esprit.repository.InternshipOfferRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AIChatUiState())
    val uiState: StateFlow<AIChatUiState> = _uiState.asStateFlow()

    private val gson = Gson()

    /**
     * Load an existing chat session
     */
    fun loadSession(session: ChatSession) {
        _uiState.value = _uiState.value.copy(
            currentSessionId = session.id,
            messages = session.messages,
            error = null
        )
    }

    /**
     * Start a new conversation
     */
    fun startNewSession() {
        _uiState.value = AIChatUiState(
            currentSessionId = UUID.randomUUID().toString(),
            messages = emptyList()
        )
    }

    /**
     * Send a message to the AI
     * @param text User's message text
     * @param offers Optional explicit list of offers (if passed from UI)
     */
    fun sendMessage(text: String, offers: List<InternshipOffer> = emptyList()) {
        if (text.isBlank()) return

        viewModelScope.launch {
            // Add user message
            val userMessage = AIMessage(text = text, isUser = true)
            val updatedMessages = _uiState.value.messages + userMessage
            
            _uiState.value = _uiState.value.copy(
                messages = updatedMessages,
                isLoading = true,
                error = null
            )

            // Save session with user message
            saveCurrentSession()

            // Convert message history to API format
            val history = updatedMessages.map { msg ->
                mapOf(
                    "role" to if (msg.isUser) "user" else "assistant",
                    "content" to msg.text
                )
            }

            // Get offers for context: use passed offers OR fetch from repository
            val offersContext = if (offers.isNotEmpty()) {
                offers
            } else {
                try {
                    // Fetch all offers from repository to ensure AI has context
                    val resource = repository.getAllOffers()
                    if (resource is com.example.esprit.util.Resource.Success) {
                        resource.data ?: emptyList()
                    } else {
                        emptyList()
                    }
                } catch (e: Exception) {
                    emptyList()
                }
            }

            // Serialize internship offers to JSON context
            val context = if (offersContext.isNotEmpty()) {
                try {
                    gson.toJson(offersContext)
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }

            // Call AI service
            val result = AIService.sendMessageWithHistory(history, context)

            result.fold(
                onSuccess = { reply ->
                    // Add AI response
                    val aiMessage = AIMessage(text = reply, isUser = false)
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + aiMessage,
                        isLoading = false
                    )
                    saveCurrentSession()
                },
                onFailure = { error ->
                    // Handle error
                    val errorMessage = "Erreur IA : ${error.message ?: "Erreur inconnue"}"
                    val errorAiMessage = AIMessage(text = errorMessage, isUser = false)
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + errorAiMessage,
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Save the current session to history
     */
    private fun saveCurrentSession() {
        val currentState = _uiState.value
        if (currentState.messages.isEmpty()) return

        val sessionId = currentState.currentSessionId ?: UUID.randomUUID().toString()
        
        // Update session ID if it was null
        if (currentState.currentSessionId == null) {
            _uiState.value = currentState.copy(currentSessionId = sessionId)
        }

        val session = ChatSession(
            id = sessionId,
            date = System.currentTimeMillis(),
            messages = currentState.messages
        )

        chatHistoryManager.saveSession(session)
    }
}
>>>>>>> origin/messaging-announcement

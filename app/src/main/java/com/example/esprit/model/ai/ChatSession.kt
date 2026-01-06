package com.example.esprit.model.ai

import java.util.UUID

/**
 * Represents a complete chat conversation session
 * @param id Unique identifier for the session
 * @param date Session creation timestamp (milliseconds since epoch)
 * @param messages List of all messages in this conversation
 */
data class ChatSession(
    val id: String = UUID.randomUUID().toString(),
    val date: Long = System.currentTimeMillis(),
    val messages: List<AIMessage> = emptyList()
) {
    /**
     * Generate a title from the first user message
     */
    val title: String
        get() {
            val firstUserMsg = messages.firstOrNull { it.isUser }
            return if (firstUserMsg != null) {
                val truncated = firstUserMsg.text.take(30)
                if (firstUserMsg.text.length > 30) "$truncated..." else truncated
            } else {
                "Nouvelle conversation"
            }
        }
}

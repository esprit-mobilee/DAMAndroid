<<<<<<< HEAD
package com.example.esprit.model.ai

import java.util.UUID

/**
 * Represents a single message in the AI chat conversation
 * @param id Unique identifier for the message
 * @param text The content of the message
 * @param isUser True if message is from user, false if from AI
 * @param timestamp When the message was created (milliseconds since epoch)
 */
data class AIMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
=======
package com.example.esprit.model.ai

import java.util.UUID

/**
 * Represents a single message in the AI chat conversation
 * @param id Unique identifier for the message
 * @param text The content of the message
 * @param isUser True if message is from user, false if from AI
 * @param timestamp When the message was created (milliseconds since epoch)
 */
data class AIMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
>>>>>>> origin/messaging-announcement

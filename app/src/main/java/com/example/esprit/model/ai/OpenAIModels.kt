<<<<<<< HEAD
package com.example.esprit.model.ai

import com.google.gson.annotations.SerializedName

/**
 * OpenAI API Request Models
 */
data class OpenAIChatRequest(
    val model: String,
    val messages: List<OpenAIMessage>
)

data class OpenAIMessage(
    val role: String, // "system", "user", or "assistant"
    val content: String
)

/**
 * OpenAI API Response Models
 */
data class OpenAIChatResponse(
    val id: String? = null,
    val choices: List<OpenAIChoice> = emptyList(),
    val error: OpenAIErrorResponse? = null
)

data class OpenAIChoice(
    val message: OpenAIMessage,
    val index: Int? = null,
    @SerializedName("finish_reason")
    val finishReason: String? = null
)

/**
 * OpenAI API Error Response
 */
data class OpenAIErrorResponse(
    val message: String,
    val type: String? = null,
    val code: String? = null
)

data class OpenAIError(
    val error: OpenAIErrorResponse
)
=======
package com.example.esprit.model.ai

import com.google.gson.annotations.SerializedName

/**
 * OpenAI API Request Models
 */
data class OpenAIChatRequest(
    val model: String,
    val messages: List<OpenAIMessage>
)

data class OpenAIMessage(
    val role: String, // "system", "user", or "assistant"
    val content: String
)

/**
 * OpenAI API Response Models
 */
data class OpenAIChatResponse(
    val id: String? = null,
    val choices: List<OpenAIChoice> = emptyList(),
    val error: OpenAIErrorResponse? = null
)

data class OpenAIChoice(
    val message: OpenAIMessage,
    val index: Int? = null,
    @SerializedName("finish_reason")
    val finishReason: String? = null
)

/**
 * OpenAI API Error Response
 */
data class OpenAIErrorResponse(
    val message: String,
    val type: String? = null,
    val code: String? = null
)

data class OpenAIError(
    val error: OpenAIErrorResponse
)
>>>>>>> origin/messaging-announcement

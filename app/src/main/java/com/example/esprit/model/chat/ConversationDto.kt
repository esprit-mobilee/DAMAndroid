package com.example.esprit.model.chat

import com.example.esprit.model.User
import com.google.gson.annotations.SerializedName

data class ConversationDto(
    @SerializedName("_id") val partnerId: String, // The group _id is the partner ID logic from aggregation
    val lastMessage: ConversationLastMessageDto, // Changed from MessageDto to handle specific projection
    val partner: com.google.gson.JsonElement? = null,
    val unreadCount: Int = 0
)

// DTO specifically for the lastMessage projection in getConversations
// The backend projection sends: content, createdAt, type, senderId (string/ID), readBy
// It does NOT send: _id (unless added), and senderId is NOT populated.
data class ConversationLastMessageDto(
    val content: String,
    val createdAt: String,
    val type: String = "TEXT",
    val senderId: com.google.gson.JsonElement? = null, // Can be String ID or Object
    val readBy: List<com.example.esprit.model.chat.ReactionDto>? = null // Using valid available DTO or generic
)

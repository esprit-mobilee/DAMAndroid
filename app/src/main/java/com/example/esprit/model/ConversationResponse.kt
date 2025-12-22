package com.example.esprit.model

data class ConversationResponse(
    val userId: String,
    val fullName: String?,
    val role: String?,
    val lastMessage: String?,
    val lastMessageTime: String?
)

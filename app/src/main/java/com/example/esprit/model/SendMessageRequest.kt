package com.example.esprit.model


data class SendMessageRequest(
    val senderId: String,
    val receiverId: String,
    val content: String,
    val type: String = "text"   // ✅ valeur par défaut
)

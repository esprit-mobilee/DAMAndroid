package com.example.esprit.model


data class Message(
    val id: String,
    val content: String,
    val senderId: String,
    val receiverId: String,
    val timestamp: String,
    val isMine: Boolean
)

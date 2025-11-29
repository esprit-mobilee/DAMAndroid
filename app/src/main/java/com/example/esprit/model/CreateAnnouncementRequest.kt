package com.example.esprit.model

data class CreateAnnouncementRequest(
    val title: String,
    val content: String,
    val audience: String,
    val senderId: String   // ✔ obligatoire pour ton backend
)

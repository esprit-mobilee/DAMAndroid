package com.example.esprit.model

data class SelectAnnouncementRequest(
    val audience: String,
    val instruction: String,
    val senderId: String,
    val selectedIndex: Int
)

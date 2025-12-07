package com.example.esprit.model

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: User,
    val message: String
)

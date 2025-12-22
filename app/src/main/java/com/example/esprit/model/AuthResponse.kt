package com.example.esprit.model

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserResponse,
    val message: String?
)

data class UserResponse(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val classGroup: String?,
    val studentId: String?
)

package com.example.esprit.repository

import com.example.esprit.model.AuthResponse
import com.example.esprit.model.User
import com.example.esprit.network.ApiService
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun login(identifiant: String, password: String): AuthResponse {
        return api.login(
            mapOf(
                "identifiant" to identifiant,  // 👈 match NestJS DTO
                "password" to password
            )
        )
    }

    suspend fun me(token: String): User =
        api.getMe("Bearer $token")
}

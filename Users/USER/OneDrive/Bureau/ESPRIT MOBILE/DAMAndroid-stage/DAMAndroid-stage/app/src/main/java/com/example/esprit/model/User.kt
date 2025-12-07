package com.example.esprit.model

data class User(
    val id: String,
    val name: String? = null, // kept for compat
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String?,
    val role: String,
    val roles: List<String>? = null,
    val classGroup: String? = null,
    val studentId: String? = null,
    val presidentOf: String? = null,
    val club: String? = null,
    val identifiant: String? = null,
    val isOnline: Boolean = false,
    val lastSeen: String? = null
)

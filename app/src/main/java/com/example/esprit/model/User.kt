package com.example.esprit.model

data class User(
    val id: String,
    val name: String? = null,           // ✅ added unified name (first + last combined)
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val role: String,
    val studentId: String? = null,
    val classGroup: String? = null
) {
    val fullName: String
        get() = when {
            !name.isNullOrBlank() -> name                     // ✅ backend now sends full name directly
            !firstName.isNullOrBlank() || !lastName.isNullOrBlank() ->
                listOfNotNull(firstName, lastName).joinToString(" ")
            else -> email ?: "Utilisateur ESPRIT"
        }
}

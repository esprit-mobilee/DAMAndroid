package com.example.esprit.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("_id")
    val id: String? = null,
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

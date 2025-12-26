package com.example.esprit.model

/**
 * Android model that can handle:
 *  - your NestJS user (with single `role`)
 *  - your app logic (with `roles: List<Role>`)
 */
data class User(
    val id: String? = null,
    val name: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,

    // 🔹 single role coming from NestJS (Utilisateurs schema)
    val role: String? = null,

    // 🔹 list of roles used by the app (ex: when we add PRESIDENT later)
    val roles: List<Role> = emptyList(),

    val studentId: String? = null,
    val classGroup: String? = null,
    val profileImageUrl: String? = null,
    
    // Club Branch additions
    val presidentOf: String? = null,
    val club: String? = null,
    val identifiant: String? = null,
    val isOnline: Boolean = false,
    val lastSeen: String? = null
) {
    val fullName: String
        get() = when {
            !name.isNullOrBlank() -> name
            !firstName.isNullOrBlank() || !lastName.isNullOrBlank() ->
                listOfNotNull(firstName, lastName).joinToString(" ")
            else -> email ?: "Utilisateur ESPRIT"
        }
}

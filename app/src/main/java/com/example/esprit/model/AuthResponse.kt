<<<<<<< HEAD
package com.example.esprit.model

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String? = null,
    val userId: String,
    val role: String,
    val email: String? = null,
    val message: String? = null
)
=======
package com.example.esprit.model

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String? = null,
    val userId: String,
    val role: String,
    val email: String? = null,
    val message: String? = null
)
>>>>>>> origin/messaging-announcement

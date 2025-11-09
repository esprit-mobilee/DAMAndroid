package com.example.esprit.repository

import com.example.esprit.model.User
import com.example.esprit.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val api: ApiService
) {

    /**
     * Récupère l'utilisateur connecté via /auth/me
     */
    suspend fun getMe(token: String): User {
        return api.getMe("Bearer $token")
    }

    /**
     * Met à jour le profil utilisateur (cas admin ou futur écran profil).
     * On n’envoie QUE les champs non nuls.
     */
    suspend fun updateUser(
        token: String,
        id: String,
        firstName: String? = null,
        lastName: String? = null,
        email: String? = null,
        avatar: String? = null,
        age: Int? = null,
        studentId: String? = null,
        classGroup: String? = null,   // 👈 NEW
    ): User {
        val body = mutableMapOf<String, Any>()

        firstName?.let { body["firstName"] = it }
        lastName?.let { body["lastName"] = it }
        email?.let { body["email"] = it }
        avatar?.let { body["avatar"] = it }
        age?.let { body["age"] = it }
        studentId?.let { body["studentId"] = it }
        classGroup?.let { body["classGroup"] = it }   // 👈 send it if not null

        return api.updateUser(
            token = "Bearer $token",
            id = id,
            body = body
        )
    }

    /**
     * Changer seulement le mot de passe de l'utilisateur connecté.
     * Doit correspondre à ton endpoint Nest:
     * POST /utilisateurs/me/password  (ou autre → adapte ici)
     */
    suspend fun changeMyPassword(
        token: String,
        oldPassword: String,
        newPassword: String
    ): Map<String, String> {
        return api.changeMyPassword(
            token = "Bearer $token",
            body = mapOf(
                "oldPassword" to oldPassword,
                "newPassword" to newPassword
            )
        )
    }
}

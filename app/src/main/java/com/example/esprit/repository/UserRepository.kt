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
     * Le token est ajouté automatiquement par AuthInterceptor.
     */
    suspend fun getMe(): User {
        return api.getMe()
    }

    /**
     * Récupère un utilisateur par son ID
     */
    suspend fun getUserById(userId: String): User {
        return api.getUserById(userId)
    }

    /**
     * Met à jour le profil utilisateur.
     * On n’envoie QUE les champs non nuls.
     */
    suspend fun updateUser(
        id: String,
        firstName: String? = null,
        lastName: String? = null,
        email: String? = null,
        avatar: String? = null,
        age: Int? = null,
        studentId: String? = null,
        classGroup: String? = null,
    ): User {
        val body = mutableMapOf<String, Any>()

        firstName?.let { body["firstName"] = it }
        lastName?.let { body["lastName"] = it }
        email?.let { body["email"] = it }
        avatar?.let { body["avatar"] = it }
        age?.let { body["age"] = it }
        studentId?.let { body["studentId"] = it }
        classGroup?.let { body["classGroup"] = it }

        return api.updateUser(
            id = id,
            body = body
        )
    }

    /**
     * Changer le mot de passe de l'utilisateur connecté.
     * Le token est ajouté automatiquement.
     */
    suspend fun changeMyPassword(
        oldPassword: String,
        newPassword: String
    ): Map<String, String> {
        return api.changeMyPassword(
            body = mapOf(
                "oldPassword" to oldPassword,
                "newPassword" to newPassword
            )
        )
    }
}

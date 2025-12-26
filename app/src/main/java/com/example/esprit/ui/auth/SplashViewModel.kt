package com.example.esprit.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.Role
import com.example.esprit.repository.AuthRepository
import com.example.esprit.util.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val dataStore: DataStoreManager,
    private val repo: AuthRepository
) : ViewModel() {

    fun checkAuth(onResult: (Boolean, Role?) -> Unit) {
        viewModelScope.launch {
            // Check Remember Me preference
            val rememberMe = dataStore.getRememberMe()
            
            if (!rememberMe) {
                // If user didn't check "Remember Me", clear token on startup
                dataStore.clearToken()
                onResult(false, null)
                return@launch
            }

            val token = dataStore.tokenFlow.first()
            if (token.isNullOrEmpty()) {
                onResult(false, null)
            } else {
                try {
                    val user = repo.me()

                    val finalRole = pickBestRole(
                        listRoles = user.roles,   // ex: [STUDENT, ADMIN]
                        stringRole = user.role    // ex: "ADMIN"
                    )

                    onResult(true, finalRole)
                } catch (_: Exception) {
                    onResult(false, null)
                }
            }
        }
    }

    /**
     * Choisit le "meilleur" rôle selon une priorité :
     * ADMIN > TEACHER > PARENT > PRESIDENT > STUDENT
     */
    private fun pickBestRole(
        listRoles: List<Role>?,
        stringRole: String?
    ): Role {
        // 1. si on a déjà une liste de Role (coté app)
        if (!listRoles.isNullOrEmpty()) {
            if (listRoles.contains(Role.ADMIN)) return Role.ADMIN
            if (listRoles.contains(Role.TEACHER)) return Role.TEACHER
            if (listRoles.contains(Role.PARENT)) return Role.PARENT
            if (listRoles.contains(Role.PRESIDENT)) return Role.STUDENT
            if (listRoles.contains(Role.STUDENT)) return Role.STUDENT
        }

        // 2. sinon, on regarde le string qui vient du backend Nest
        if (!stringRole.isNullOrBlank()) {
            return when (stringRole.uppercase()) {
                "ADMIN" -> Role.ADMIN
                "TEACHER" -> Role.TEACHER
                "PARENT" -> Role.PARENT
                "CLUB" -> Role.CLUB
                "PRESIDENT" -> Role.STUDENT
                "STUDENT", "USER" -> Role.STUDENT
                else -> Role.STUDENT
            }
        }

        // 3. fallback
        return Role.STUDENT
    }
}

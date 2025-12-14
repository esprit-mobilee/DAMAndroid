package com.example.esprit.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.Role
import com.example.esprit.repository.AuthRepository
import com.example.esprit.util.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val dataStore: DataStoreManager,
    private val authManager: com.example.esprit.util.AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(identifier: String, password: String, rememberMe: Boolean, onLoginSuccess: (Role) -> Unit) {
        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)

            try {
                // 1. Invalider le cache avant le login pour forcer le rechargement du nouveau token
                authManager.onLogin()
                
                // 2. login -> token
                val authRes = repo.login(identifier, password)

                // 3. store token
                dataStore.saveToken(authRes.accessToken)
                
                // 4. store remember preference
                dataStore.saveRememberMe(rememberMe)

                // 5. get /auth/me to know roles
                val me = repo.me()

                val finalRole = pickBestRole(
                    listRoles = me.roles,
                    stringRole = me.role
                )

                _uiState.value = LoginUiState(isLoading = false, error = null)
                onLoginSuccess(finalRole)

            } catch (e: Exception) {
                _uiState.value = LoginUiState(
                    isLoading = false,
                    error = e.message ?: "Erreur de connexion"
                )
            }
        }
    }

    private fun pickBestRole(
        listRoles: List<Role>?,
        stringRole: String?
    ): Role {
        if (!listRoles.isNullOrEmpty()) {
            if (listRoles.contains(Role.ADMIN)) return Role.ADMIN
            if (listRoles.contains(Role.TEACHER)) return Role.TEACHER
            if (listRoles.contains(Role.PARENT)) return Role.PARENT
            if (listRoles.contains(Role.PRESIDENT)) return Role.STUDENT
            if (listRoles.contains(Role.STUDENT)) return Role.STUDENT
            if (listRoles.contains(Role.CLUB)) return Role.CLUB

        }

        if (!stringRole.isNullOrBlank()) {
            return when (stringRole.uppercase()) {
                "ADMIN" -> Role.ADMIN
                "TEACHER" -> Role.TEACHER
                "PARENT" -> Role.PARENT
                "PRESIDENT" -> Role.STUDENT
                "CLUB" -> Role.CLUB
                "STUDENT", "USER" -> Role.STUDENT

                else -> Role.STUDENT
            }
        }

        return Role.STUDENT
    }
}

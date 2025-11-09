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
    private val dataStore: DataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(identifier: String, password: String, onSuccess: (Role) -> Unit) {
        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)
            try {
                // 1) appel backend
                val res = repo.login(identifier, password)

                // 2) on sauvegarde le token
                dataStore.saveToken(res.accessToken)

                // 3) sécuriser le champ role (peut être null ou vide)
                val backendRole = res.role?.uppercase() ?: "STUDENT"

                val role = when (backendRole) {
                    "STUDENT" -> Role.STUDENT
                    "TEACHER" -> Role.TEACHER
                    "PARENT"  -> Role.PARENT
                    "ADMIN"   -> Role.ADMIN
                    else      -> Role.STUDENT   // fallback
                }

                // 4) navigation
                onSuccess(role)

                // 5) reset UI
                _uiState.value = LoginUiState()
            } catch (e: Exception) {
                _uiState.value = LoginUiState(
                    error = e.message ?: "Erreur inconnue"
                )
            }
        }
    }
}

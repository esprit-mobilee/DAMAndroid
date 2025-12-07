package com.example.esprit.ui.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.User
import com.example.esprit.repository.UserRepository
import com.example.esprit.util.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val user: User? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepo: UserRepository,
    private val dataStore: DataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    /**
     * Appelé quand l’écran s’ouvre → /me
     */
    fun loadMe() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val token = dataStore.tokenFlow.first().orEmpty()
                if (token.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Token manquant"
                    )
                    return@launch
                }

                val me = userRepo.getMe(token)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    user = me
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Erreur inconnue"
                )
            }
        }
    }

    /**
     * Changer le mot de passe
     */
    fun changePassword(old: String, new: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                success = false
            )
            try {
                val token = dataStore.tokenFlow.first().orEmpty()
                if (token.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Token manquant"
                    )
                    return@launch
                }

                userRepo.changeMyPassword(token, old, new)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    success = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Erreur inconnue",
                    success = false
                )
            }
        }
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(success = false)
    }

    /**
     * Logout - Nettoie le token du DataStore
     */
    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                // Nettoyer le token
                dataStore.saveToken(null)
                android.util.Log.d("ProfileVM", "Token cleared successfully")
                onComplete()
            } catch (e: Exception) {
                android.util.Log.e("ProfileVM", "Error during logout: ${e.message}", e)
                // Même en cas d'erreur, on continue le logout
                onComplete()
            }
        }
    }
}

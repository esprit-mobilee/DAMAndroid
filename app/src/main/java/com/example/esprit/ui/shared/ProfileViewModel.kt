package com.example.esprit.ui.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.User
import com.example.esprit.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val user: User? = null,
    val aiProfile: com.example.esprit.model.AiProfile? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepo: UserRepository,
    private val aiProfileRepo: com.example.esprit.repository.AiProfileRepository,
    private val applicationRepo: com.example.esprit.repository.ApplicationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    /**
     * Appelé quand l’écran s’ouvre → /auth/me
     * Le token est injecté par l’intercepteur, donc pas besoin de DataStore ici.
     */
    fun loadMe() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Launch parallel loading
            launch {
                try {
                    val me = userRepo.getMe()
                    _uiState.value = _uiState.value.copy(
                        user = me
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Erreur inconnue"
                    )
                }
                _uiState.value = _uiState.value.copy(isLoading = false)
            }

            launch {
                aiProfileRepo.aiProfile.collect { profile ->
                    _uiState.value = _uiState.value.copy(aiProfile = profile)
                }
            }
            
            // Self-healing: Check if user has applications, if not clear local profile
            launch {
                try {
                    val me = userRepo.getMe() // Ensure we have the user ID
                    val userId = me.id
                    if (userId != null) {
                        val appsRes = applicationRepo.getApplicationsByUser(userId)
                        if (appsRes is com.example.esprit.util.Resource.Success) {
                            if (appsRes.data.isNullOrEmpty()) {
                                 android.util.Log.d("ProfileViewModel", "No applications found. Clearing AI Profile.")
                                aiProfileRepo.clearProfile()
                            }
                        }
                    } else {
                        android.util.Log.d("ProfileViewModel", "User ID is null, skipping self-healing.")
                    }
                } catch (e: Exception) {
                     android.util.Log.e("ProfileViewModel", "Self-healing check failed", e)
                }
            }
        }
    }

    /**
     * Changer le mot de passe de l’utilisateur connecté
     */
    fun changePassword(old: String, new: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                success = false
            )
            try {
                userRepo.changeMyPassword(old, new)
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
}

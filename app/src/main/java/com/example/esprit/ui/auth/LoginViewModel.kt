package com.example.esprit.ui.auth

import android.util.Log
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

    fun login(identifier: String, password: String, onLoginSuccess: (Role) -> Unit) {
        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)

            try {
                Log.d("LoginViewModel", "Starting login for: $identifier")
                
                val authRes = repo.login(identifier, password)
                Log.d("LoginViewModel", "Login API call successful")
                
                val user = authRes.user
                Log.d("LoginViewModel", "User: ${user.name}, Role: ${user.role}")
                
                val finalRole = pickBestRole(
                    stringRole = user.role,
                    isPresident = user.presidentOf != null
                )
                Log.d("LoginViewModel", "Final role determined: $finalRole")

                Log.d("LoginViewModel", "Saving token to DataStore...")
                dataStore.saveToken(authRes.accessToken, user.role)
                Log.d("LoginViewModel", "Token saved successfully")

                _uiState.value = LoginUiState(isLoading = false)
                Log.d("LoginViewModel", "Calling onLoginSuccess with role: $finalRole")
                
                onLoginSuccess(finalRole)
                Log.d("LoginViewModel", "Login process completed")

            } catch (e: Exception) {
                Log.e("LoginViewModel", "Login failed", e)
                _uiState.value = LoginUiState(
                    isLoading = false,
                    error = e.message ?: "Erreur de connexion"
                )
            }
        }
    }

    private fun pickBestRole(
        stringRole: String?,
        isPresident: Boolean
    ): Role {
        if (stringRole.equals("CLUB", ignoreCase = true)) return Role.CLUB
        if (stringRole.equals("ADMIN", ignoreCase = true)) return Role.ADMIN
        if (stringRole.equals("TEACHER", ignoreCase = true)) return Role.TEACHER
        if (stringRole.equals("PARENT", ignoreCase = true)) return Role.PARENT
        if (isPresident) return Role.PRESIDENT
        return Role.STUDENT
    }
}

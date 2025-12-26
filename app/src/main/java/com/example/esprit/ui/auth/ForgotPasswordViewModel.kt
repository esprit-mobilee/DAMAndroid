package com.example.esprit.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ResetStep {
    EMAIL, CODE, NEW_PASSWORD, SUCCESS
}

data class ForgotPasswordUiState(
    val step: ResetStep = ResetStep.EMAIL,
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val email: String = "",
    val code: String = ""
)

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val repo: com.example.esprit.repository.AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState

    fun sendCode(email: String) {
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Veuillez entrer votre email")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                repo.forgotPassword(email)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    step = ResetStep.CODE,
                    email = email,
                    message = "Un code de vérification a été envoyé à $email"
                )
            } catch (e: Exception) {
                 _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Erreur d'envoi"
                )
            }
        }
    }

    fun verifyCode(code: String) {
        if (code.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Veuillez entrer le code")
            return
        }

        val email = _uiState.value.email
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Erreur: Email manquant")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                repo.verifyCode(email, code)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    step = ResetStep.NEW_PASSWORD,
                    code = code,
                    message = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Code invalide ou expiré"
                )
            }
        }
    }

    fun resetPassword(pass1: String, pass2: String) {
        if (pass1.isBlank() || pass2.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Veuillez remplir les champs")
            return
        }
        if (pass1 != pass2) {
            _uiState.value = _uiState.value.copy(error = "Les mots de passe ne correspondent pas")
            return
        }

        val email = _uiState.value.email
        val code = _uiState.value.code
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                repo.resetPassword(email, code, pass1)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    step = ResetStep.SUCCESS,
                    message = "Votre mot de passe a été modifié avec succès"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erreur lors de la réinitialisation"
                )
            }
        }
    }
    
    fun resetState() {
        _uiState.value = ForgotPasswordUiState()
    }
}

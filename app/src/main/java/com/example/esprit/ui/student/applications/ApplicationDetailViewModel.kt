package com.example.esprit.ui.student.applications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.Application
import com.example.esprit.repository.ApplicationRepository
import com.example.esprit.repository.UserRepository
import com.example.esprit.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ApplicationDetailUiState(
    val isLoading: Boolean = true,
    val application: Application? = null,
    val error: String? = null,
    val userName: String? = null,
    val userEmail: String? = null
)

@HiltViewModel
class ApplicationDetailViewModel @Inject constructor(
    private val repository: ApplicationRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ApplicationDetailUiState())
    val uiState: StateFlow<ApplicationDetailUiState> = _uiState
    
    fun loadApplication(applicationId: String) {
        viewModelScope.launch {
            _uiState.value = ApplicationDetailUiState(isLoading = true)
            when (val res = repository.getApplicationById(applicationId)) {
                is Resource.Success -> {
                    val application = res.data
                    // Charger le nom et l'email de l'utilisateur
                    var userName: String? = null
                    var userEmail: String? = null
                    application?.userId?.let { userId ->
                        try {
                            val user = userRepository.getUserById(userId)
                            userName = user.fullName
                            userEmail = user.email
                        } catch (e: Exception) {
                            userName = userId // Fallback sur l'ID si erreur
                        }
                    }
                    _uiState.value = ApplicationDetailUiState(
                        isLoading = false,
                        application = application,
                        userName = userName,
                        userEmail = userEmail
                    )
                }
                is Resource.Error -> {
                    _uiState.value = ApplicationDetailUiState(
                        isLoading = false,
                        error = res.message
                    )
                }
                else -> {}
            }
        }
    }
    
    fun deleteApplication(applicationId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(error = null)
            when (val res = repository.deleteApplication(applicationId)) {
                is Resource.Success -> {
                    onSuccess()
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = res.message ?: "Erreur lors de la suppression"
                    )
                }
                else -> {}
            }
        }
    }
    
    fun updateApplication(
        applicationId: String,
        cvUrl: String?,
        coverLetter: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            when (repository.updateApplication(applicationId, cvUrl, coverLetter)) {
                is Resource.Success -> {
                    loadApplication(applicationId) // Recharger pour avoir les données à jour
                    onSuccess()
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(error = "Erreur lors de la mise à jour")
                }
                else -> {}
            }
        }
    }
}


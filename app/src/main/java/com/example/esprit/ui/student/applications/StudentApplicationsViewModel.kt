package com.example.esprit.ui.student.applications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.Application
import com.example.esprit.repository.ApplicationRepository
import com.example.esprit.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentApplicationsUiState(
    val isLoading: Boolean = false,
    val applications: List<Application> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class StudentApplicationsViewModel @Inject constructor(
    private val repository: ApplicationRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(StudentApplicationsUiState())
    val uiState: StateFlow<StudentApplicationsUiState> = _uiState
    
    fun loadApplications(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val res = repository.getApplicationsByUser(userId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        applications = res.data ?: emptyList(),
                        error = null
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = res.message
                    )
                }
                else -> {}
            }
        }
    }
    
    fun deleteApplication(applicationId: String, userId: String) {
        viewModelScope.launch {
            when (repository.deleteApplication(applicationId)) {
                is Resource.Success -> {
                    loadApplications(userId)
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(error = "Erreur lors de la suppression")
                }
                else -> {}
            }
        }
    }
}


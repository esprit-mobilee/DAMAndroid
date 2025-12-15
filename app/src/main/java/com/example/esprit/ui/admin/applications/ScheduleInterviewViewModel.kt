package com.example.esprit.ui.admin.applications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScheduleInterviewUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class ScheduleInterviewViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleInterviewUiState())
    val uiState: StateFlow<ScheduleInterviewUiState> = _uiState.asStateFlow()

    fun scheduleInterview(
        applicationId: String,
        studentEmail: String,
        scheduledAt: String,
        duration: Int,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = ScheduleInterviewUiState(isLoading = true)
            
            try {
                val body = mapOf(
                    "studentEmail" to studentEmail,
                    "scheduledAt" to scheduledAt,
                    "duration" to duration,
                    "notes" to notes
                )
                
                android.util.Log.d("InterviewVM", "Scheduling interview: $body")
                val response = apiService.scheduleInterview(applicationId, body)
                android.util.Log.d("InterviewVM", "Schedule success: $response")
                
                _uiState.value = ScheduleInterviewUiState(success = true)
                onSuccess()
            } catch (e: Exception) {
                android.util.Log.e("InterviewVM", "Schedule error", e)
                if (e is retrofit2.HttpException) {
                    android.util.Log.e("InterviewVM", "HTTP ${e.code()} Body: ${e.response()?.errorBody()?.string()}")
                }
                _uiState.value = ScheduleInterviewUiState(
                    error = e.message ?: "Erreur lors de la planification de l'entretien"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

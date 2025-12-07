package com.example.esprit.ui.student.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.club.ClubEventDto
import com.example.esprit.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentEventsUiState(
    val events: List<ClubEventDto> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class StudentEventsViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentEventsUiState())
    val uiState: StateFlow<StudentEventsUiState> = _uiState.asStateFlow()

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            try {
                val events = apiService.getEvents()
                _uiState.value = _uiState.value.copy(
                    events = events,
                    loading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = e.message ?: "Erreur lors du chargement des événements"
                )
            }
        }
    }

    fun joinEvent(eventId: String, name: String, email: String, message: String = "") {
        viewModelScope.launch {
            try {
                val body = mapOf(
                    "name" to name,
                    "email" to email,
                    "message" to message
                )
                apiService.joinEvent(eventId, body)
                // Reload events to get updated registration status
                loadEvents()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Erreur lors de l'inscription"
                )
            }
        }
    }
}

package com.example.esprit.ui.club

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.club.ClubEventDto
import com.example.esprit.repository.ClubEventsRepository
import com.example.esprit.repository.EventPayload
import com.example.esprit.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EventsUiState(
    val loading: Boolean = false,
    val events: List<ClubEventDto> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ClubEventsViewModel @Inject constructor(
    private val repo: ClubEventsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(EventsUiState(loading = true))
    val uiState: StateFlow<EventsUiState> = _uiState

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            when (val res = repo.list()) {
                is UiState.Success -> _uiState.value = EventsUiState(events = res.data)
                is UiState.Error -> _uiState.value = EventsUiState(error = res.message)
                UiState.Loading -> _uiState.value = EventsUiState(loading = true)
            }
        }
    }

    suspend fun toggleRegistration(id: String): UiState<ClubEventDto> = repo.toggleRegistration(id)
    suspend fun delete(id: String): UiState<Unit> = repo.delete(id)
    suspend fun joinEvent(id: String, body: Map<String, Any?>): UiState<Map<String, Any?>> = repo.joinEvent(id, body)
    
    suspend fun getRegistrations(id: String) = repo.getRegistrations(id)
    suspend fun approveRegistration(eventId: String, userId: String) = repo.approveRegistration(eventId, userId)
    suspend fun rejectRegistration(eventId: String, userId: String) = repo.rejectRegistration(eventId, userId)
}

@HiltViewModel
class CreateEventViewModel @Inject constructor(
    private val repo: ClubEventsRepository
) : ViewModel() {
    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving

    suspend fun create(payload: EventPayload): UiState<ClubEventDto> {
        _saving.value = true
        val res = repo.create(payload)
        _saving.value = false
        return res
    }
}

@HiltViewModel
class EditEventViewModel @Inject constructor(
    private val repo: ClubEventsRepository
) : ViewModel() {
    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving

    suspend fun update(id: String, payload: EventPayload): UiState<ClubEventDto> {
        _saving.value = true
        val res = repo.update(id, payload)
        _saving.value = false
        return res
    }
}

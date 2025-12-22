package com.example.esprit.ui.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.AiGeneratedAnnouncement
import com.example.esprit.model.AnnouncementDto
import com.example.esprit.repository.AnnouncementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiAnnouncementsUiState(
    val audience: String = "",
    val instruction: String = "",
    val isLoading: Boolean = false,
    val generated: List<AiGeneratedAnnouncement> = emptyList(),
    val selectedIndex: Int? = null,
    val saved: AnnouncementDto? = null,
    val error: String? = null
)

@HiltViewModel
class AiViewModel @Inject constructor(
    private val repo: AnnouncementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiAnnouncementsUiState())
    val uiState: StateFlow<AiAnnouncementsUiState> = _uiState

    // USER INPUT
    fun setAudience(value: String) {
        _uiState.update { it.copy(audience = value) }
    }

    fun setInstruction(value: String) {
        _uiState.update { it.copy(instruction = value) }
    }

    // GENERATE AI ANNOUNCEMENTS
    fun generate() {
        val state = _uiState.value
        if (state.audience.isBlank() || state.instruction.isBlank()) {
            _uiState.update { it.copy(error = "Champ manquant") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, generated = emptyList()) }

            try {
                val result = repo.generateAiAnnouncements(
                    audience = state.audience,
                    instruction = state.instruction
                )
                _uiState.update { it.copy(isLoading = false, generated = result) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    // SELECT ONE OF THE 3
    fun select(index: Int) {
        _uiState.update { it.copy(selectedIndex = index) }
    }

    // SAVE SELECTED
    fun save(senderId: String) {
        val state = _uiState.value
        val index = state.selectedIndex ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val saved = repo.saveSelectedAiAnnouncement(
                    audience = state.audience,
                    instruction = state.instruction,
                    senderId = senderId,
                    selectedIndex = index
                )
                _uiState.update { it.copy(isLoading = false, saved = saved) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

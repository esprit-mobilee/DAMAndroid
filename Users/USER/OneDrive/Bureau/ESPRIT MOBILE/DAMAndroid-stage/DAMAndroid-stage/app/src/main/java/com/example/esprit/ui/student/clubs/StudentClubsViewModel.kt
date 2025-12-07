package com.example.esprit.ui.student.clubs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.club.ClubHomeDto
import com.example.esprit.repository.StudentRepository
import com.example.esprit.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentClubsUiState(
    val loading: Boolean = false,
    val clubs: List<ClubHomeDto> = emptyList(),
    val error: String? = null,
    val showJoinDialog: Boolean = false,
    val selectedClubId: String? = null,
    val joinQuestions: List<String> = emptyList()
)

@HiltViewModel
class StudentClubsViewModel @Inject constructor(
    private val repo: StudentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentClubsUiState(loading = true))
    val uiState: StateFlow<StudentClubsUiState> = _uiState

    init {
        loadClubs()
    }

    fun loadClubs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            when (val res = repo.getAllClubs()) {
                is UiState.Success -> {
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        clubs = res.data ?: emptyList()
                    )
                }
                is UiState.Error -> {
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        error = res.message
                    )
                }
                UiState.Loading -> {}
            }
        }
    }

    fun onJoinClick(clubId: String) {
        val club = _uiState.value.clubs.find { it.id == clubId } ?: return
        if (!club.joinFormQuestions.isNullOrEmpty()) {
            _uiState.value = _uiState.value.copy(
                showJoinDialog = true,
                selectedClubId = clubId,
                joinQuestions = club.joinFormQuestions
            )
        } else {
            submitJoinRequest(clubId, emptyList())
        }
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(
            showJoinDialog = false,
            selectedClubId = null,
            joinQuestions = emptyList()
        )
    }

    fun submitJoinRequest(clubId: String, answers: List<Map<String, String>>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true) // Optional: show loading
            when (val res = repo.joinClub(clubId, answers)) {
                is UiState.Success -> {
                    loadClubs()
                    dismissDialog()
                }
                is UiState.Error -> {
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        error = res.message
                    )
                }
                else -> {}
            }
        }
    }
}

package com.example.esprit.ui.club

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.club.JoinRequestDto
import com.example.esprit.repository.ClubRepository
import com.example.esprit.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClubRequestsUiState(
    val loading: Boolean = false,
    val requests: List<JoinRequestDto> = emptyList(),
    val error: String? = null,
    val message: String? = null
)

@HiltViewModel
class ClubRequestsViewModel @Inject constructor(
    private val repo: ClubRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClubRequestsUiState(loading = true))
    val uiState: StateFlow<ClubRequestsUiState> = _uiState
    private var clubId: String? = null

    init {
        loadRequests()
    }

    fun loadRequests() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            
            // First get club ID from home()
            if (clubId == null) {
                when (val res = repo.home()) {
                    is UiState.Success<*> -> clubId = (res.data as? com.example.esprit.model.club.ClubHomeDto)?.id
                    is UiState.Error -> {
                        _uiState.value = _uiState.value.copy(loading = false, error = res.message)
                        return@launch
                    }
                    UiState.Loading -> {}
                }
            }

            val id = clubId ?: return@launch

            when (val res = repo.getPendingRequests(id)) {
                is UiState.Success<*> -> {
                   val data = res.data as? List<JoinRequestDto> ?: emptyList()
                   _uiState.value = _uiState.value.copy(
                       loading = false,
                       requests = data
                   )
                }
                is UiState.Error -> {
                    _uiState.value = _uiState.value.copy(loading = false, error = res.message)
                }
                UiState.Loading -> {}
            }
        }
    }

    fun approve(requestId: String) {
        viewModelScope.launch {
            when (val res = repo.approveRequest(requestId)) {
                is UiState.Success<*> -> {
                    loadRequests() // Refresh list
                    _uiState.value = _uiState.value.copy(message = "Demande acceptée")
                }
                is UiState.Error -> _uiState.value = _uiState.value.copy(error = res.message)
                else -> {}
            }
        }
    }

    fun reject(requestId: String) {
        viewModelScope.launch {
            when (val res = repo.rejectRequest(requestId)) {
                is UiState.Success<*> -> {
                    loadRequests()
                    _uiState.value = _uiState.value.copy(message = "Demande refusée")
                }
                is UiState.Error -> _uiState.value = _uiState.value.copy(error = res.message)
                else -> {}
            }
        }
    }
    
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null, error = null)
    }
}

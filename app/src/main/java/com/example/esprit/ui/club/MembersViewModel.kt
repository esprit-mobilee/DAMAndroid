package com.example.esprit.ui.club

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.club.ClubMemberDto
import com.example.esprit.repository.MembersRepository
import com.example.esprit.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MembersUiState(
    val loading: Boolean = false,
    val members: List<ClubMemberDto> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class MembersViewModel @Inject constructor(
    private val repo: MembersRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MembersUiState(loading = true))
    val uiState: StateFlow<MembersUiState> = _uiState

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            when (val res = repo.list()) {
                is UiState.Success -> _uiState.value = MembersUiState(members = res.data)
                is UiState.Error -> _uiState.value = MembersUiState(error = res.message)
                UiState.Loading -> _uiState.value = MembersUiState(loading = true)
            }
        }
    }

    suspend fun remove(id: String): UiState<Unit> = repo.remove(id)
    
    // Backend does not support make president endpoint yet
    // suspend fun makePresident(id: String): UiState<com.example.esprit.model.club.ClubHomeDto> = repo.makePresident(id)
}

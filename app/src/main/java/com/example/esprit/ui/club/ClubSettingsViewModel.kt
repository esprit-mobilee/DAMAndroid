package com.example.esprit.ui.club

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.club.ClubHomeDto
import com.example.esprit.repository.ClubRepository
import com.example.esprit.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

data class ClubSettingsUiState(
    val loading: Boolean = false,
    val club: ClubHomeDto? = null,
    val error: String? = null,
    val message: String? = null
)

@HiltViewModel
class ClubSettingsViewModel @Inject constructor(
    private val repo: ClubRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ClubSettingsUiState(loading = true))
    val uiState: StateFlow<ClubSettingsUiState> = _uiState
    private var clubId: String? = null

    fun load() {
        viewModelScope.launch {
            when (val res = repo.home()) {
                is UiState.Success<*> -> {
                    val data = res.data as? ClubHomeDto
                    clubId = data?.id
                    _uiState.value = ClubSettingsUiState(club = data)
                }
                is UiState.Error -> _uiState.value = ClubSettingsUiState(error = res.message)
                UiState.Loading -> _uiState.value = ClubSettingsUiState(loading = true)
            }
        }
    }

    fun update(
        name: String,
        description: String?,
        tags: String?,
        profileImage: MultipartBody.Part? = null,
        coverImage: MultipartBody.Part? = null
    ) {
        val id = clubId ?: return
        viewModelScope.launch {
            when (
                val res = repo.updateProfile(
                    id,
                    name,
                    description,
                    tags,
                    profileImage,
                    coverImage
                )
            ) {
                is UiState.Success<*> -> {
                    val data = res.data as? ClubHomeDto
                    _uiState.value = ClubSettingsUiState(
                        club = data,
                        message = "Profil mis à jour"
                    )
                }
                is UiState.Error -> _uiState.value = _uiState.value.copy(error = res.message)
                UiState.Loading -> _uiState.value = _uiState.value.copy(loading = true)
            }
        }
    }
    fun toggleJoin() {
        val id = clubId ?: return
        viewModelScope.launch {
            when (val res = repo.toggleJoinEnabled(id)) {
                is UiState.Success<*> -> {
                    val data = res.data as? ClubHomeDto
                    _uiState.value = _uiState.value.copy(
                        club = data,
                        message = if (data?.joinEnabled == true) "Inscriptions ouvertes" else "Inscriptions fermées"
                    )
                }
                is UiState.Error -> _uiState.value = _uiState.value.copy(error = res.message)
                UiState.Loading -> _uiState.value = _uiState.value.copy(loading = true)
            }
        }
    }

    fun updateJoinQuestions(questions: List<String>) {
        val id = clubId ?: return
        viewModelScope.launch {
            when (val res = repo.updateSettings(id, joinFormQuestions = questions)) {
                is UiState.Success<*> -> {
                    val data = res.data as? ClubHomeDto
                    _uiState.value = _uiState.value.copy(
                        club = data,
                        message = "Questions mises à jour"
                    )
                }
                is UiState.Error -> _uiState.value = _uiState.value.copy(error = res.message)
                UiState.Loading -> _uiState.value = _uiState.value.copy(loading = true)
            }
        }
    }

    // Backend does not support password change endpoint yet
    // fun changePassword(old: String, new: String) {
    //     viewModelScope.launch {
    //         when (val res = repo.changePassword(old, new)) {
    //             is UiState.Success -> _uiState.value = _uiState.value.copy(message = res.data["message"])
    //             is UiState.Error -> _uiState.value = _uiState.value.copy(error = res.message)
    //             UiState.Loading -> _uiState.value = _uiState.value.copy(loading = true)
    //         }
    //     }
    // }
}

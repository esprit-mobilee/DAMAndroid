package com.example.esprit.ui.vieetudiante

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.Club
import com.example.esprit.repository.ClubRepository
import com.example.esprit.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class ClubsViewModel @Inject constructor(
    private val clubRepository: ClubRepository
) : ViewModel() {

    private val _clubs = MutableStateFlow<UiState<List<Club>>>(UiState.Loading)
    val clubs = _clubs.asStateFlow()

    init {
        loadClubs()
    }

    fun loadClubs() {
        viewModelScope.launch {
            _clubs.value = UiState.Loading
            val result = clubRepository.getClubs()
            _clubs.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Erreur de chargement") }
            )
        }
    }

    fun loadClubById(
        id: String,
        onSuccess: (Club) -> Unit
    ) {
        viewModelScope.launch {
            val result = clubRepository.getClubById(id)
            result.onSuccess(onSuccess)
        }
    }

    // CREATE with optional image
    fun createClub(
        name: String,
        description: String?,
        presidentIdentifiant: String?,
        tags: List<String>,
        imagePart: MultipartBody.Part?,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            clubRepository.createClub(
                name = name,
                description = description,
                presidentIdentifiant = presidentIdentifiant,
                tags = tags,
                imagePart = imagePart
            )
            loadClubs()
            onDone()
        }
    }

    // UPDATE with optional new image
    fun updateClub(
        id: String,
        name: String,
        description: String?,
        presidentIdentifiant: String?,
        tags: List<String>,
        imagePart: MultipartBody.Part?,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            clubRepository.updateClub(
                id = id,
                name = name,
                description = description,
                presidentIdentifiant = presidentIdentifiant,
                tags = tags,
                imagePart = imagePart
            )
            loadClubs()
            onDone()
        }
    }

    fun deleteClub(
        id: String,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            clubRepository.deleteClub(id)
            loadClubs()
            onDone()
        }
    }
}

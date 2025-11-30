package com.example.esprit.ui.shared.internships

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.repository.FavoriteRepository
import com.example.esprit.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoriteUiState(
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val repository: FavoriteRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FavoriteUiState())
    val uiState: StateFlow<FavoriteUiState> = _uiState
    
    fun checkFavorite(internshipId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val res = repository.isFavorite(internshipId)) {
                is Resource.Success -> {
                    _uiState.value = FavoriteUiState(
                        isFavorite = res.data ?: false,
                        isLoading = false
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
    
    fun toggleFavorite(internshipId: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState.isFavorite) {
                // Supprimer des favoris
                when (repository.removeFavorite(internshipId)) {
                    is Resource.Success -> {
                        _uiState.value = currentState.copy(
                            isFavorite = false
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = currentState.copy(error = "Erreur lors de la suppression")
                    }
                    else -> {}
                }
            } else {
                // Ajouter aux favoris
                when (repository.addFavorite(internshipId)) {
                    is Resource.Success -> {
                        _uiState.value = currentState.copy(
                            isFavorite = true
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = currentState.copy(error = "Erreur lors de l'ajout")
                    }
                    else -> {}
                }
            }
        }
    }
}


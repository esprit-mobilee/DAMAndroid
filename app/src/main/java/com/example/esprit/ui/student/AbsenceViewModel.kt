<<<<<<< HEAD
package com.example.esprit.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.AbsenceItem
import com.example.esprit.repository.AbsenceRepository
import com.example.esprit.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing and exposing the student's absences.
 * Automatically fetches via repository using AuthInterceptor (no manual token).
 */
@HiltViewModel
class AbsenceViewModel @Inject constructor(
    private val repository: AbsenceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<AbsenceItem>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<AbsenceItem>>> = _uiState

    fun fetchAbsences() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val data = repository.getAbsences()
                _uiState.value = UiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.localizedMessage ?: "Erreur inconnue")
            }
        }
    }
}
=======
package com.example.esprit.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.AbsenceItem
import com.example.esprit.repository.AbsenceRepository
import com.example.esprit.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing and exposing the student's absences.
 * Automatically fetches via repository using AuthInterceptor (no manual token).
 */
@HiltViewModel
class AbsenceViewModel @Inject constructor(
    private val repository: AbsenceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<AbsenceItem>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<AbsenceItem>>> = _uiState

    fun fetchAbsences() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val data = repository.getAbsences()
                _uiState.value = UiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.localizedMessage ?: "Erreur inconnue")
            }
        }
    }
}
>>>>>>> origin/messaging-announcement

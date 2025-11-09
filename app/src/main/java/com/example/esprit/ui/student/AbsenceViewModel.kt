package com.example.esprit.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.AbsenceItem
import com.example.esprit.repository.AbsenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing and exposing the student's absence data.
 * Uses Hilt to inject AbsenceRepository.
 */
@HiltViewModel
class AbsenceViewModel @Inject constructor(
    private val repository: AbsenceRepository
) : ViewModel() {

    private val _absences = MutableStateFlow<List<AbsenceItem>>(emptyList())
    val absences: StateFlow<List<AbsenceItem>> = _absences

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    /**
     * Fetches absences for the current student.
     * The token must be a valid JWT (without adding "Bearer", handled here).
     */
    fun fetchAbsences(token: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val data = repository.getAbsences(token)
                _absences.value = data
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Erreur inconnue"
            } finally {
                _loading.value = false
            }
        }
    }
}

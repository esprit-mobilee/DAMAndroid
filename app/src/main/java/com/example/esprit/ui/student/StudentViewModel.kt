package com.example.esprit.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.TimetableItem
import com.example.esprit.repository.StudentRepository
import com.example.esprit.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentViewModel @Inject constructor(
    private val repo: StudentRepository
) : ViewModel() {

    private val _timetable =
        MutableStateFlow<UiState<List<TimetableItem>>>(UiState.Loading)
    val timetable: StateFlow<UiState<List<TimetableItem>>> = _timetable

    init {
        loadTimetable()
    }

    fun loadTimetable() {
        viewModelScope.launch {
            _timetable.value = UiState.Loading
            try {
                // repo.timetable() returns UiState directly
                _timetable.value = repo.timetable()
            } catch (e: Exception) {
                _timetable.value = UiState.Error(e.message ?: "Erreur")
            }
        }
    }
}

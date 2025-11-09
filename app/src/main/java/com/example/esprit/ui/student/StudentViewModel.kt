package com.example.esprit.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.TimetableItem
import com.example.esprit.repository.StudentRepository
import com.example.esprit.util.DataStoreManager
import com.example.esprit.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentViewModel @Inject constructor(
    private val repo: StudentRepository,
    private val dataStore: DataStoreManager
) : ViewModel() {

    private val _timetable = MutableStateFlow<Resource<List<TimetableItem>>>(Resource.Loading)
    val timetable: StateFlow<Resource<List<TimetableItem>>> = _timetable

    init {
        loadTimetable()
    }

    fun loadTimetable() {
        viewModelScope.launch {
            val token = dataStore.tokenFlow.first() ?: return@launch
            _timetable.value = Resource.Loading
            try {
                val res = repo.timetable(token)
                _timetable.value = Resource.Success(res)
            } catch (e: Exception) {
                _timetable.value = Resource.Error(e.message ?: "Erreur")
            }
        }
    }
}

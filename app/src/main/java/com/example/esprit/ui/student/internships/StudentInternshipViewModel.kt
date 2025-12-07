package com.example.esprit.ui.student.internships

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.InternshipOffer
import com.example.esprit.repository.InternshipOfferRepository
import com.example.esprit.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentInternshipUiState(
    val isLoading: Boolean = false,
    val offers: List<InternshipOffer> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class StudentInternshipViewModel @Inject constructor(
    private val repository: InternshipOfferRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentInternshipUiState())
    val uiState: StateFlow<StudentInternshipUiState> = _uiState

    init {
        loadOffers()
    }

    fun loadOffers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val res = repository.getAllOffers()) {
                is Resource.Success -> _uiState.value =
                    StudentInternshipUiState(offers = res.data ?: emptyList())
                is Resource.Error -> _uiState.value =
                    StudentInternshipUiState(error = res.message)
                else -> {}
            }
        }
    }
}

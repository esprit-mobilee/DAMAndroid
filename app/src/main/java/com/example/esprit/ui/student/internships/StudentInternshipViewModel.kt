package com.example.esprit.ui.student.internships

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.InternshipOffer
import com.example.esprit.repository.InternshipOfferRepository
import com.example.esprit.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentInternshipUiState(
    val isLoading: Boolean = false,
    val offers: List<InternshipOffer> = emptyList(),
    val error: String? = null,
    val searchHistory: List<String> = emptyList()
)

@HiltViewModel
class StudentInternshipViewModel @Inject constructor(
    private val repository: InternshipOfferRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentInternshipUiState())
    val uiState: StateFlow<StudentInternshipUiState> = _uiState

    private val prefs: SharedPreferences = context.getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
    private val HISTORY_KEY = "search_history"

    init {
        loadOffers()
        loadSearchHistory()
    }

    fun loadOffers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val res = repository.getAllOffers()) {
                is Resource.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    offers = res.data ?: emptyList()
                )
                is Resource.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = res.message
                )
                else -> {}
            }
        }
    }

    private fun loadSearchHistory() {
        val historyString = prefs.getString(HISTORY_KEY, "") ?: ""
        val historyList = if (historyString.isBlank()) emptyList() else historyString.split("|")
        _uiState.value = _uiState.value.copy(searchHistory = historyList)
    }

    fun addToSearchHistory(query: String) {
        if (query.isBlank()) return
        
        val currentHistory = _uiState.value.searchHistory.toMutableList()
        currentHistory.remove(query) // Remove if exists to move to top
        currentHistory.add(0, query) // Add to top
        
        // Limit to 10 items
        if (currentHistory.size > 10) {
            currentHistory.removeAt(currentHistory.lastIndex)
        }
        
        _uiState.value = _uiState.value.copy(searchHistory = currentHistory)
        
        // Save to prefs
        prefs.edit().putString(HISTORY_KEY, currentHistory.joinToString("|")).apply()
    }

    fun clearSearchHistory() {
        _uiState.value = _uiState.value.copy(searchHistory = emptyList())
        prefs.edit().remove(HISTORY_KEY).apply()
    }
}

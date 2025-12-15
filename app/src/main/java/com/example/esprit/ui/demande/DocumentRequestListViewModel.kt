package com.example.esprit.ui.demande

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.DocumentFileItem
import com.example.esprit.model.DocumentRequestItem
import com.example.esprit.repository.DocumentRequestRepository
import com.example.esprit.util.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.esprit.service.SmartMessage
import com.example.esprit.service.SmartPredictionService
import javax.inject.Inject

data class DocumentRequestListUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val requests: List<DocumentRequestItem> = emptyList(),
    val files: List<DocumentFileItem> = emptyList(),
    val isDeleting: Boolean = false,
    val deleteError: String? = null,
    val searchYear: String = "",
    val smartMessage: SmartMessage? = null
) {
    fun hasFile(requestId: String): Boolean {
        return files.any { it.documentRequestId == requestId && it.url != null }
    }
    
    val filteredRequests: List<DocumentRequestItem>
        get() = if (searchYear.isBlank()) {
            requests
        } else {
            requests.filter { 
                it.annee.contains(searchYear, ignoreCase = true)
            }
        }
}

@HiltViewModel
class DocumentRequestListViewModel @Inject constructor(
    private val repository: DocumentRequestRepository,
    private val dataStore: DataStoreManager,
    private val smartPredictionService: SmartPredictionService
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentRequestListUiState())
    val uiState: StateFlow<DocumentRequestListUiState> = _uiState

    fun loadRequests() {
        // Update smart message on load
        _uiState.update { it.copy(smartMessage = smartPredictionService.getSmartTip()) }
        
        viewModelScope.launch {
            val token = dataStore.tokenFlow.first().orEmpty()
            if (token.isBlank()) {
                _uiState.update { it.copy(error = "Token manquant, veuillez vous reconnecter.") }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val requests = repository.getRequests()
                // Try to load files in parallel
                val files = try {
                    repository.getFiles()
                } catch (e: Exception) {
                    emptyList<DocumentFileItem>()
                }
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        requests = requests,
                        files = files
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Impossible de charger les demandes."
                    )
                }
            }
        }
    }

    fun refresh() = loadRequests()

    fun deleteRequest(id: String) {
        viewModelScope.launch {
            val token = dataStore.tokenFlow.first().orEmpty()
            if (token.isBlank()) {
                _uiState.update { it.copy(deleteError = "Token manquant, veuillez vous reconnecter.") }
                return@launch
            }
            _uiState.update { it.copy(isDeleting = true, deleteError = null) }
            try {
                repository.deleteRequest(id)
                _uiState.update { state ->
                    state.copy(
                        isDeleting = false,
                        requests = state.requests.filterNot { it.id == id }
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        deleteError = e.localizedMessage ?: "Échec de la suppression."
                    )
                }
            }
        }
    }

    fun clearErrors() {
        _uiState.update { it.copy(error = null, deleteError = null) }
    }
    
    fun updateSearchYear(year: String) {
        _uiState.update { it.copy(searchYear = year) }
    }
}


package com.example.esprit.ui.demande

import android.util.Log
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
import javax.inject.Inject

data class DocumentRequestDetailUiState(
    val isLoading: Boolean = false,
    val isLoadingFile: Boolean = false,
    val error: String? = null,
    val request: DocumentRequestItem? = null,
    val file: DocumentFileItem? = null,
    val isDeleting: Boolean = false,
    val deleteError: String? = null
)

@HiltViewModel
class DocumentRequestDetailViewModel @Inject constructor(
    private val repository: DocumentRequestRepository,
    private val dataStore: DataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentRequestDetailUiState())
    val uiState: StateFlow<DocumentRequestDetailUiState> = _uiState

    fun loadRequest(id: String) {
        viewModelScope.launch {
            Log.d("DetailVM", "loadRequest called with id: $id")
            val token = dataStore.tokenFlow.first().orEmpty()
            if (token.isBlank()) {
                Log.e("DetailVM", "Token is blank")
                _uiState.update { it.copy(error = "Token manquant, veuillez vous reconnecter.") }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                Log.d("DetailVM", "Calling repository.getRequestById")
                val request = repository.getRequestById(token, id)
                Log.d("DetailVM", "Request loaded successfully: ${request.id}, type: ${request.type}")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        request = request
                    )
                }
                // Try to load file if request exists
                loadFile(id)
            } catch (e: Exception) {
                Log.e("DetailVM", "Error loading request", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Impossible de charger la demande."
                    )
                }
            }
        }
    }

    fun loadFile(requestId: String) {
        viewModelScope.launch {
            val token = dataStore.tokenFlow.first().orEmpty()
            if (token.isBlank()) {
                Log.e("DetailVM", "Token is blank when loading file")
                return@launch
            }
            _uiState.update { it.copy(isLoadingFile = true) }
            try {
                Log.d("DetailVM", "Loading file for requestId: $requestId")
                
                // Get the request to match file by type and annee
                val request = _uiState.value.request
                
                // Try the specific endpoint first
                val file = try {
                    repository.getRequestFile(token, requestId)
                } catch (e: Exception) {
                    Log.d("DetailVM", "Specific endpoint failed, trying files list: ${e.message}")
                    // If specific endpoint fails, try to find in files list
                    val allFiles = repository.getFiles(token)
                    Log.d("DetailVM", "Found ${allFiles.size} files in list")
                    
                    // Try to find by documentRequestId first
                    var foundFile = allFiles.find { it.documentRequestId == requestId }
                    
                    // If not found and we have request info, try by type and annee
                    if (foundFile == null && request != null) {
                        Log.d("DetailVM", "Searching by type=${request.type} and annee=${request.annee}")
                        foundFile = allFiles.find { 
                            it.type == request.type && it.annee == request.annee 
                        }
                    }
                    
                    foundFile
                }
                
                if (file != null) {
                    Log.d("DetailVM", "File loaded: id=${file.id}, url=${file.url}, nomFichier=${file.nomFichier}, documentRequestId=${file.documentRequestId}")
                    _uiState.update { 
                        it.copy(
                            isLoadingFile = false,
                            file = file
                        )
                    }
                } else {
                    Log.d("DetailVM", "No file found for requestId: $requestId")
                    _uiState.update { it.copy(isLoadingFile = false) }
                }
            } catch (e: Exception) {
                // File might not exist, that's okay
                Log.e("DetailVM", "Error loading file for requestId: $requestId", e)
                _uiState.update { it.copy(isLoadingFile = false) }
            }
        }
    }

    fun refresh(requestId: String) {
        loadRequest(requestId)
    }

    fun deleteRequest(id: String) {
        viewModelScope.launch {
            val token = dataStore.tokenFlow.first().orEmpty()
            if (token.isBlank()) {
                _uiState.update { it.copy(deleteError = "Token manquant, veuillez vous reconnecter.") }
                return@launch
            }
            _uiState.update { it.copy(isDeleting = true, deleteError = null) }
            try {
                repository.deleteRequest(token, id)
                _uiState.update { 
                    it.copy(
                        isDeleting = false,
                        request = null // Mark as deleted
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
}



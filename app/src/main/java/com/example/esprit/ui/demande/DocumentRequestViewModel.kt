 package com.example.esprit.ui.demande

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.CreateDocumentRequestPayload
import com.example.esprit.model.DocumentField
import com.example.esprit.model.DocumentRequestCreateResponse
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

private val SUPPORTED_DOCUMENT_TYPES = listOf("attestation", "relevé", "convention")

data class DocumentRequestUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val fields: List<DocumentField> = emptyList(),
    val formValues: Map<String, String> = emptyMap(),
    val availableTypes: List<String> = SUPPORTED_DOCUMENT_TYPES,
    val selectedType: String = SUPPORTED_DOCUMENT_TYPES.first(),
    val annee: String = "",
    val fileUrl: String = "",
    val created: DocumentRequestItem? = null,
    val createdFileUrl: String? = null
)

@HiltViewModel
class DocumentRequestViewModel @Inject constructor(
    private val repository: DocumentRequestRepository,
    private val dataStore: DataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentRequestUiState())
    val uiState: StateFlow<DocumentRequestUiState> = _uiState

    init {
        loadFields(_uiState.value.selectedType)
    }

    fun selectType(type: String) {
        if (type == _uiState.value.selectedType) return
        _uiState.update {
            it.copy(
                selectedType = type,
                fields = emptyList(),
                formValues = emptyMap(),
                successMessage = null,
                error = null
            )
        }
        loadFields(type)
    }

    fun updateFieldValue(name: String, value: String) {
        _uiState.update { state ->
            state.copy(
                formValues = state.formValues.toMutableMap().apply { put(name, value) }
            )
        }
    }

    fun updateAnnee(value: String) {
        _uiState.update { it.copy(annee = value) }
    }

    fun updateFileUrl(value: String) {
        _uiState.update { it.copy(fileUrl = value) }
    }

    fun submitRequest() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.annee.isBlank()) {
                _uiState.update { it.copy(error = "Veuillez renseigner l'année académique.") }
                return@launch
            }
            val token = dataStore.tokenFlow.first().orEmpty()
            if (token.isBlank()) {
                _uiState.update { it.copy(error = "Token manquant, veuillez vous reconnecter.") }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                val details = state.formValues
                    .filterKeys { key -> key !in setOf("annee", "fileUrl") }
                    .filterValues { it.isNotBlank() }
                    .ifEmpty { null }

                val payload = CreateDocumentRequestPayload(
                    type = state.selectedType,
                    annee = state.annee,
                    fileUrl = state.fileUrl.takeIf { it.isNotBlank() },
                    details = details
                )

                val response: DocumentRequestCreateResponse = repository.createRequest(token, payload)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Demande créée avec succès.",
                        created = response.documentRequest,
                        createdFileUrl = response.fileUrl,
                        formValues = emptyMap(),
                        fields = it.fields,
                        fileUrl = "",
                        annee = "",
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Erreur inconnue lors de la création."
                    )
                }
            }
        }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }

    private fun loadFields(type: String) {
        viewModelScope.launch {
            val token = dataStore.tokenFlow.first().orEmpty()
            if (token.isBlank()) {
                _uiState.update { it.copy(error = "Token manquant, veuillez vous reconnecter.") }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                val response = repository.getFormFields(token, type)
                val defaultValues = response.fields.associate { field ->
                    field.name to when (field.name) {
                        "annee" -> _uiState.value.annee
                        else -> ""
                    }
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        fields = response.fields,
                        formValues = defaultValues
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Impossible de charger les champs."
                    )
                }
            }
        }
    }
}


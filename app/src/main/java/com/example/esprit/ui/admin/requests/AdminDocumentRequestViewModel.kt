package com.example.esprit.ui.admin.requests

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.DocumentRequestItem
import com.example.esprit.repository.DocumentRequestRepository
import com.example.esprit.util.DataStoreManager
import com.example.esprit.util.PdfGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

data class AdminDocumentRequestUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val requests: List<DocumentRequestItem> = emptyList(),
    val selectedRequest: DocumentRequestItem? = null,
    val studentHistory: List<DocumentRequestItem> = emptyList(),
    val isLoadingHistory: Boolean = false,
    val isUploading: Boolean = false,
    val isGeneratingPdf: Boolean = false,
    val generatedPdfUri: Uri? = null
)

@HiltViewModel
class AdminDocumentRequestViewModel @Inject constructor(
    private val repository: DocumentRequestRepository,
    private val dataStore: DataStoreManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDocumentRequestUiState())
    val uiState: StateFlow<AdminDocumentRequestUiState> = _uiState

    fun loadRequests() {
        viewModelScope.launch {
            val token = dataStore.tokenFlow.first().orEmpty()
            if (token.isBlank()) return@launch

            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val requests = repository.getRequests(token)
                _uiState.update { it.copy(isLoading = false, requests = requests) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun loadRequest(id: String) {
        viewModelScope.launch {
            android.util.Log.d("AdminVM", "loadRequest called with id: $id")
            val token = dataStore.tokenFlow.first().orEmpty()
            android.util.Log.d("AdminVM", "Token: ${if (token.isBlank()) "EMPTY" else "EXISTS"}")
            if (token.isBlank()) {
                android.util.Log.e("AdminVM", "Token is blank, aborting loadRequest")
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                android.util.Log.d("AdminVM", "Calling repository.getRequestById...")
                val request = repository.getRequestById(token, id)
                android.util.Log.d("AdminVM", "Request loaded successfully: ${request.id}")
                _uiState.update { it.copy(isLoading = false, selectedRequest = request) }
            } catch (e: Exception) {
                android.util.Log.e("AdminVM", "Error loading request: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun selectRequest(request: DocumentRequestItem) {
        _uiState.update { it.copy(selectedRequest = request) }
    }

    fun loadStudentHistory(userId: String) {
        viewModelScope.launch {
            val token = dataStore.tokenFlow.first().orEmpty()
            if (token.isBlank()) return@launch

            _uiState.update { it.copy(isLoadingHistory = true) }
            try {
                // Charger toutes les demandes et filtrer par userId
                val allRequests = repository.getRequests(token)
                val studentRequests = allRequests.filter { it.user?.id == userId }
                    .sortedByDescending { it.createdAt } // Plus récentes en premier
                
                _uiState.update { 
                    it.copy(
                        isLoadingHistory = false, 
                        studentHistory = studentRequests
                    ) 
                }
            } catch (e: Exception) {
                android.util.Log.e("AdminVM", "Error loading student history: ${e.message}", e)
                _uiState.update { it.copy(isLoadingHistory = false) }
            }
        }
    }

    fun updateStatus(id: String, status: String, reason: String? = null) {
        viewModelScope.launch {
            val token = dataStore.tokenFlow.first().orEmpty()
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                android.util.Log.d("AdminVM", "Updating status to $status for request $id")
                android.util.Log.d("AdminVM", "Rejection reason: ${reason ?: "N/A"}")
                
                repository.updateStatus(token, id, status, reason)
                
                android.util.Log.d("AdminVM", "Status updated successfully")
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        successMessage = if (status == "REJECTED") "Demande rejetée" else "Statut mis à jour avec succès",
                        selectedRequest = it.selectedRequest?.copy(status = status, rejectionReason = reason)
                    ) 
                }
                loadRequests() // Refresh list
            } catch (e: com.google.gson.JsonSyntaxException) {
                android.util.Log.e("AdminVM", "JSON parsing error: ${e.message}", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Erreur de format de réponse du serveur. La demande a peut-être été mise à jour malgré l'erreur."
                    ) 
                }
                // Try to reload to see if it actually worked
                loadRequests()
            } catch (e: Exception) {
                android.util.Log.e("AdminVM", "Error updating status: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage ?: "Erreur inconnue") }
            }
        }
    }

    fun approveAndUpload(id: String, fileUri: Uri) {
        viewModelScope.launch {
            val token = dataStore.tokenFlow.first().orEmpty()
            _uiState.update { it.copy(isUploading = true, error = null) }
            try {
                val file = uriToFile(fileUri)
                if (file == null) {
                    _uiState.update { it.copy(isUploading = false, error = "Impossible de lire le fichier") }
                    return@launch
                }

                val requestFile = file.asRequestBody("application/pdf".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                repository.uploadFile(token, id, body)
                
                _uiState.update { 
                    it.copy(
                        isUploading = false, 
                        successMessage = "Fichier envoyé et demande approuvée",
                        selectedRequest = it.selectedRequest?.copy(status = "APPROVED")
                    ) 
                }
                loadRequests()
            } catch (e: Exception) {
                _uiState.update { it.copy(isUploading = false, error = e.localizedMessage) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("upload", ".pdf", context.cacheDir)
            val outputStream = FileOutputStream(tempFile)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun generateAttestationPdf(request: DocumentRequestItem) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingPdf = true, error = null, generatedPdfUri = null) }
            try {
                val fileName = "attestation_${request.user?.studentId}_${System.currentTimeMillis()}.pdf"
                val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                    ?: context.filesDir
                val outputFile = java.io.File(documentsDir, fileName)
                
                val success = PdfGenerator.generateAttestationPdf(request, outputFile)
                
                if (success) {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        outputFile
                    )
                    _uiState.update { 
                        it.copy(
                            isGeneratingPdf = false,
                            generatedPdfUri = uri,
                            successMessage = "PDF généré avec succès: ${outputFile.absolutePath}"
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isGeneratingPdf = false,
                            error = "Erreur lors de la génération du PDF"
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AdminVM", "Error generating PDF: ${e.message}", e)
                _uiState.update { 
                    it.copy(
                        isGeneratingPdf = false,
                        error = e.localizedMessage ?: "Erreur inconnue lors de la génération du PDF"
                    )
                }
            }
        }
    }

    fun sharePdf(uri: Uri) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Partager l'attestation").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            android.util.Log.e("AdminVM", "Error sharing PDF: ${e.message}", e)
            _uiState.update { it.copy(error = "Erreur lors du partage du PDF") }
        }
    }

    fun clearPdfUri() {
        _uiState.update { it.copy(generatedPdfUri = null) }
    }

    fun approveAndGeneratePdf(request: DocumentRequestItem) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingPdf = true, isUploading = true, error = null, generatedPdfUri = null) }
            try {
                val token = dataStore.tokenFlow.first().orEmpty()
                if (token.isBlank()) {
                    _uiState.update { it.copy(isGeneratingPdf = false, isUploading = false, error = "Token manquant") }
                    return@launch
                }

                // Step 1: Generate PDF
                val fileName = "attestation_${request.user?.studentId}_${System.currentTimeMillis()}.pdf"
                val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                    ?: context.filesDir
                val outputFile = java.io.File(documentsDir, fileName)
                
                val pdfSuccess = PdfGenerator.generateAttestationPdf(request, outputFile)
                
                if (!pdfSuccess) {
                    _uiState.update { 
                        it.copy(
                            isGeneratingPdf = false,
                            isUploading = false,
                            error = "Erreur lors de la génération du PDF"
                        )
                    }
                    return@launch
                }

                // Step 2: Get URI for viewing
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    outputFile
                )

                // Step 3: Upload PDF to backend
                val requestFile = outputFile.asRequestBody("application/pdf".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", outputFile.name, requestFile)

                repository.uploadFile(token, request.id, body)
                
                _uiState.update { 
                    it.copy(
                        isGeneratingPdf = false,
                        isUploading = false,
                        generatedPdfUri = uri,
                        successMessage = "PDF généré et demande approuvée avec succès",
                        selectedRequest = it.selectedRequest?.copy(status = "APPROVED")
                    )
                }
                loadRequests() // Refresh list
            } catch (e: Exception) {
                android.util.Log.e("AdminVM", "Error in approveAndGeneratePdf: ${e.message}", e)
                _uiState.update { 
                    it.copy(
                        isGeneratingPdf = false,
                        isUploading = false,
                        error = e.localizedMessage ?: "Erreur lors de l'approbation et génération du PDF"
                    )
                }
            }
        }
    }

    fun viewPdf(uri: Uri) {
        try {
            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(viewIntent)
        } catch (e: Exception) {
            android.util.Log.e("AdminVM", "Error viewing PDF: ${e.message}", e)
            _uiState.update { it.copy(error = "Aucune application pour ouvrir le PDF") }
        }
    }
}

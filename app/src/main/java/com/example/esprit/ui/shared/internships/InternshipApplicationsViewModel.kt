package com.example.esprit.ui.shared.internships

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.Application
import com.example.esprit.repository.ApplicationRepository
import com.example.esprit.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import javax.inject.Inject

data class ApplicationsUiState(
    val isLoading: Boolean = false,
    val applications: List<Application> = emptyList(),
    val error: String? = null,
    val isApplying: Boolean = false,
    val applyError: String? = null,
    val applySuccess: Boolean = false
)

@HiltViewModel
class InternshipApplicationsViewModel @Inject constructor(
    private val repo: ApplicationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApplicationsUiState())
    val uiState: StateFlow<ApplicationsUiState> = _uiState

    // Chargement des candidatures pour une offre (vue Admin)
    fun loadForInternship(internshipId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val res = repo.getApplicationsByInternship(internshipId)) {
                is Resource.Success ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        applications = res.data ?: emptyList(),
                        error = null
                    )

                is Resource.Error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = res.message
                    )

                else -> {}
            }
        }
    }

    // Étudiant : postuler
    fun apply(
        userId: String,
        internshipId: String,
        cvUrl: String,
        coverLetter: String?,
        cvFile: File? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isApplying = true,
                applyError = null,
                applySuccess = false
            )

            val res = if (cvFile != null) {
                try {
                    val requestFile = cvFile.asRequestBody("application/pdf".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("cv", cvFile.name, requestFile)
                    repo.applyWithCv(userId, internshipId, body, coverLetter)
                } catch (e: Exception) {
                    Resource.Error("Erreur préparation fichier: ${e.message}")
                }
            } else {
                repo.applyToInternship(userId, internshipId, cvUrl, coverLetter)
            }

            when (res) {
                is Resource.Success -> {
                    // recharger la liste pour que l’admin voie la nouvelle candidature
                    loadForInternship(internshipId)
                    _uiState.value = _uiState.value.copy(
                        isApplying = false,
                        applySuccess = true
                    )
                }

                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isApplying = false,
                        applyError = res.message
                    )
                }

                else -> {}
            }
        }
    }

    // Admin : accepter / refuser une candidature
    fun changeStatusForAdmin(
        applicationId: String,
        internshipId: String,
        newStatus: String
    ) {
        viewModelScope.launch {
            when (val res = repo.updateApplicationStatus(applicationId, status = newStatus)) {
                is Resource.Success -> {
                    // on recharge la liste pour afficher le nouveau statut
                    loadForInternship(internshipId)
                }

                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(error = res.message)
                }

                else -> {}
            }
        }
    }
}

<<<<<<< HEAD
package com.example.esprit.ui.shared.internships

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.Application
import com.example.esprit.service.AIService
import com.example.esprit.repository.AiProfileRepository
import com.example.esprit.repository.ApplicationRepository
import com.example.esprit.util.PdfUtil
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
    private val repo: ApplicationRepository,
    private val pdfUtil: PdfUtil,
    private val aiProfileRepository: AiProfileRepository,
    private val userRepo: com.example.esprit.repository.UserRepository
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


            // 🚀 AI Profile Generation (Fire & Forget)
            if (res is Resource.Success && cvFile != null) {
                // Use GlobalScope to ensure the task completes even if the user exits the screen (ViewModel destroyed)
                @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
                kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        android.util.Log.d("ProfilIA", "Starting CV processing for file: ${cvFile.name}")
                        val cvText = pdfUtil.extractTextFromPdf(cvFile)
                        android.util.Log.d("ProfilIA", "Extracted text length: ${cvText.length}")
                        
                        if (cvText.isNotBlank()) {
                            AIService.generateProfileFromCV(cvText)
                                .onSuccess { profile ->
                                    android.util.Log.d("ProfilIA", "AI Profile generated successfully: ${profile.summary?.take(20)}...")
                                    aiProfileRepository.saveProfile(profile)
                                    android.util.Log.d("ProfilIA", "Profile saved to local storage")
                                    
                                    // Update user name if found in CV
                                    if (!profile.firstName.isNullOrBlank() || !profile.lastName.isNullOrBlank()) {
                                        try {
                                            userRepo.updateUser(
                                                id = userId,
                                                firstName = profile.firstName,
                                                lastName = profile.lastName
                                            )
                                            android.util.Log.d("ProfilIA", "User name updated from CV: ${profile.firstName} ${profile.lastName}")
                                        } catch (e: Exception) {
                                            android.util.Log.e("ProfilIA", "Failed to update user name", e)
                                        }
                                    }
                                }
                                .onFailure { e ->
                                    android.util.Log.e("ProfilIA", "AI Generation failed", e)
                                }
                        } else {
                            android.util.Log.w("ProfilIA", "Extracted text was blank!")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ProfilIA", "Exception during CV processing", e)
                    }
                }
            } else {
                android.util.Log.d("ProfilIA", "Skipping AI generation. Success=${res is Resource.Success}, CvFileNull=${cvFile == null}")
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
=======
package com.example.esprit.ui.shared.internships

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.Application
import com.example.esprit.service.AIService
import com.example.esprit.repository.AiProfileRepository
import com.example.esprit.repository.ApplicationRepository
import com.example.esprit.util.PdfUtil
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
    private val repo: ApplicationRepository,
    private val pdfUtil: PdfUtil,
    private val aiProfileRepository: AiProfileRepository,
    private val userRepo: com.example.esprit.repository.UserRepository
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


            // 🚀 AI Profile Generation (Fire & Forget)
            if (res is Resource.Success && cvFile != null) {
                // Use GlobalScope to ensure the task completes even if the user exits the screen (ViewModel destroyed)
                @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
                kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        android.util.Log.d("ProfilIA", "Starting CV processing for file: ${cvFile.name}")
                        val cvText = pdfUtil.extractTextFromPdf(cvFile)
                        android.util.Log.d("ProfilIA", "Extracted text length: ${cvText.length}")
                        
                        if (cvText.isNotBlank()) {
                            AIService.generateProfileFromCV(cvText)
                                .onSuccess { profile ->
                                    android.util.Log.d("ProfilIA", "AI Profile generated successfully: ${profile.summary?.take(20)}...")
                                    aiProfileRepository.saveProfile(profile)
                                    android.util.Log.d("ProfilIA", "Profile saved to local storage")
                                    
                                    // Update user name if found in CV
                                    if (!profile.firstName.isNullOrBlank() || !profile.lastName.isNullOrBlank()) {
                                        try {
                                            userRepo.updateUser(
                                                id = userId,
                                                firstName = profile.firstName,
                                                lastName = profile.lastName
                                            )
                                            android.util.Log.d("ProfilIA", "User name updated from CV: ${profile.firstName} ${profile.lastName}")
                                        } catch (e: Exception) {
                                            android.util.Log.e("ProfilIA", "Failed to update user name", e)
                                        }
                                    }
                                }
                                .onFailure { e ->
                                    android.util.Log.e("ProfilIA", "AI Generation failed", e)
                                }
                        } else {
                            android.util.Log.w("ProfilIA", "Extracted text was blank!")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ProfilIA", "Exception during CV processing", e)
                    }
                }
            } else {
                android.util.Log.d("ProfilIA", "Skipping AI generation. Success=${res is Resource.Success}, CvFileNull=${cvFile == null}")
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
>>>>>>> origin/messaging-announcement

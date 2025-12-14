package com.example.esprit.repository

import com.example.esprit.model.Application
import com.example.esprit.network.ApiService
import com.example.esprit.util.Resource

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class ApplicationRepository(
    private val apiService: ApiService
) {


    // Obtenir toutes les candidatures (pour admin)
    suspend fun getAllApplications(): Resource<List<Application>> {
        return try {
            val all = apiService.getApplications()
            Resource.Success(all)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erreur lors du chargement des candidatures")
        }
    }

    // Toutes les candidatures, filtrées côté client par internshipId
    suspend fun getApplicationsByInternship(internshipId: String): Resource<List<Application>> {
        return try {
            val all = apiService.getApplications()
            // internshipId est un objet côté Android (avec un champ id)
            val filtered = all.filter { it.internshipId?.id == internshipId }
            Resource.Success(filtered)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erreur lors du chargement des candidatures")
        }
    }

    // Postuler à une offre (JSON simple - déprécié pour les fichiers locaux)
    suspend fun applyToInternship(
        userId: String,
        internshipId: String,
        cvUrl: String,
        coverLetter: String?
    ): Resource<Application> {
        return try {
            val body = mutableMapOf<String, Any>(
                "userId" to userId,
                "internshipId" to internshipId,
                "cvUrl" to cvUrl
            )
            if (!coverLetter.isNullOrBlank()) {
                body["coverLetter"] = coverLetter
            }

            val res = apiService.createApplication(body)
            Resource.Success(res)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erreur lors de la candidature")
        }
    }

    // Postuler avec upload de CV
    suspend fun applyWithCv(
        userId: String,
        internshipId: String,
        cvPart: MultipartBody.Part,
        coverLetter: String?
    ): Resource<Application> {
        return try {
            val textType = "text/plain".toMediaTypeOrNull()
            val userIdBody = userId.toRequestBody(textType)
            val internshipIdBody = internshipId.toRequestBody(textType)
            
            val data = mutableMapOf<String, RequestBody>(
                "userId" to userIdBody,
                "internshipId" to internshipIdBody
            )
            
            if (!coverLetter.isNullOrBlank()) {
                data["coverLetter"] = coverLetter.toRequestBody(textType)
            }

            val res = apiService.uploadCvAndCreateApplication(cvPart, data)
            Resource.Success(res)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erreur lors de l'upload de la candidature")
        }
    }

    // Obtenir toutes les candidatures d'un étudiant
    suspend fun getApplicationsByUser(userId: String): Resource<List<Application>> {
        return try {
            val all = apiService.getApplications()
            val filtered = all.filter { it.userId == userId }
            Resource.Success(filtered)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erreur lors du chargement des candidatures")
        }
    }
    
    // Obtenir une candidature par ID
    suspend fun getApplicationById(applicationId: String): Resource<Application> {
        return try {
            val all = apiService.getApplications()
            val app = all.find { it.id == applicationId }
            if (app != null) {
                Resource.Success(app)
            } else {
                Resource.Error("Candidature non trouvée")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erreur lors du chargement de la candidature")
        }
    }

    // Admin : changer le status ou le score
    suspend fun updateApplicationStatus(
        applicationId: String,
        status: String? = null,
        aiScore: Int? = null
    ): Resource<Application> {
        return try {
            val body = mutableMapOf<String, Any>()
            if (status != null) body["status"] = status
            if (aiScore != null) body["aiScore"] = aiScore

            val res = apiService.updateApplication(applicationId, body)
            Resource.Success(res)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erreur lors de la mise à jour de la candidature")
        }
    }
    
    // Étudiant : mettre à jour sa candidature
    suspend fun updateApplication(
        applicationId: String,
        cvUrl: String? = null,
        coverLetter: String? = null
    ): Resource<Application> {
        return try {
            val body = mutableMapOf<String, Any>()
            if (cvUrl != null) body["cvUrl"] = cvUrl
            if (coverLetter != null) body["coverLetter"] = coverLetter

            val res = apiService.updateApplication(applicationId, body)
            Resource.Success(res)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erreur lors de la mise à jour de la candidature")
        }
    }
    
    // Supprimer une candidature
    suspend fun deleteApplication(applicationId: String): Resource<Unit> {
        return try {
            val result = apiService.deleteApplication(applicationId)
            // L'API retourne une Application, mais on ignore le résultat
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erreur lors de la suppression de la candidature")
        }
    }
}

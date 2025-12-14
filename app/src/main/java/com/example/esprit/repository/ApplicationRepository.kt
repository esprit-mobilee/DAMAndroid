package com.example.esprit.repository

import com.example.esprit.model.Application
import com.example.esprit.network.ApiService
import com.example.esprit.util.Resource

class ApplicationRepository(
    private val apiService: ApiService
) {

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

    // Postuler à une offre
    suspend fun applyToInternship(
        userId: String,          // identifiant ESPRIT ou ObjectId sous forme de String
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
}

package com.example.esprit.repository

import com.example.esprit.model.InternshipOffer
import com.example.esprit.network.ApiService
import com.example.esprit.util.Resource
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class InternshipOfferRepository(
    private val apiService: ApiService
) {

    private val TEXT: String = "text/plain".toMediaType().toString()

    private fun String.toPart(): RequestBody =
        this.toRequestBody("text/plain".toMediaType())

    suspend fun getAllOffers(): Resource<List<InternshipOffer>> {
        return try {
            val res = apiService.getInternshipOffers()
            Resource.Success(res)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erreur lors du chargement des offres")
        }
    }

    suspend fun getOfferById(id: String): Resource<InternshipOffer> {
        return try {
            val res = apiService.getInternshipOffer(id)
            Resource.Success(res)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erreur lors du chargement de l'offre")
        }
    }

    // CREATE (multipart + logo)
    suspend fun createOffer(
        title: String,
        company: String,
        description: String,
        location: String?,
        duration: Int,
        salary: Int?,
        logoPart: MultipartBody.Part?,
        tags: List<String>? = null,
        internshipType: String? = null,
        procedure: String? = null,
        interviewProcess: String? = null,
        startDate: String? = null,
        interviewDetails: String? = null,
        positionsAvailable: Int? = null
    ): Resource<InternshipOffer> {
        return try {
            val data = mutableMapOf<String, RequestBody>(
                "title" to title.toPart(),
                "company" to company.toPart(),
                "description" to description.toPart(),
                "duration" to duration.toString().toPart()
            )

            if (!location.isNullOrBlank()) {
                data["location"] = location.toPart()
            }
            if (salary != null) {
                data["salary"] = salary.toString().toPart()
            }
            
            // New fields
            if (!tags.isNullOrEmpty()) {
                data["tags"] = tags.joinToString(",").toPart()
            }
            if (!internshipType.isNullOrBlank()) {
                data["internshipType"] = internshipType.toPart()
            }
            if (!procedure.isNullOrBlank()) {
                data["procedure"] = procedure.toPart()
            }
            if (!interviewProcess.isNullOrBlank()) {
                data["interviewProcess"] = interviewProcess.toPart()
            }
            if (!startDate.isNullOrBlank()) {
                data["startDate"] = startDate.toPart()
            }
            if (!interviewDetails.isNullOrBlank()) {
                data["interviewDetails"] = interviewDetails.toPart()
            }
            if (positionsAvailable != null) {
                data["positionsAvailable"] = positionsAvailable.toString().toPart()
            }

            val res = apiService.createInternshipOffer(data, logoPart)
            Resource.Success(res)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erreur lors de la création")
        }
    }

    // UPDATE (multipart + logo)
    suspend fun updateOffer(
        id: String,
        title: String,
        company: String,
        description: String,
        location: String?,
        duration: Int,
        salary: Int?,
        logoPart: MultipartBody.Part?,
        tags: List<String>? = null,
        internshipType: String? = null,
        procedure: String? = null,
        interviewProcess: String? = null,
        startDate: String? = null,
        interviewDetails: String? = null,
        positionsAvailable: Int? = null
    ): Resource<InternshipOffer> {
        return try {
            val data = mutableMapOf<String, RequestBody>(
                "title" to title.toPart(),
                "company" to company.toPart(),
                "description" to description.toPart(),
                "duration" to duration.toString().toPart()
            )

            if (!location.isNullOrBlank()) {
                data["location"] = location.toPart()
            }
            if (salary != null) {
                data["salary"] = salary.toString().toPart()
            }
            
            // New fields
            if (!tags.isNullOrEmpty()) {
                data["tags"] = tags.joinToString(",").toPart()
            }
            if (!internshipType.isNullOrBlank()) {
                data["internshipType"] = internshipType.toPart()
            }
            if (!procedure.isNullOrBlank()) {
                data["procedure"] = procedure.toPart()
            }
            if (!interviewProcess.isNullOrBlank()) {
                data["interviewProcess"] = interviewProcess.toPart()
            }
            if (!startDate.isNullOrBlank()) {
                data["startDate"] = startDate.toPart()
            }
            if (!interviewDetails.isNullOrBlank()) {
                data["interviewDetails"] = interviewDetails.toPart()
            }
            if (positionsAvailable != null) {
                data["positionsAvailable"] = positionsAvailable.toString().toPart()
            }

            val res = apiService.updateInternshipOffer(id, data, logoPart)
            Resource.Success(res)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erreur lors de la mise à jour")
        }
    }

    suspend fun deleteOffer(id: String): Resource<Unit> {
        return try {
            apiService.deleteInternshipOffer(id)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erreur lors de la suppression")
        }
    }
}

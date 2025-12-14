package com.example.esprit.repository

import com.example.esprit.model.CreateDocumentRequestPayload
import com.example.esprit.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentRequestRepository @Inject constructor(
    private val api: ApiService
) {

    suspend fun getFormFields(token: String, type: String) =
        api.getDocumentFormFields(type, "Bearer $token")

    suspend fun createRequest(token: String, payload: CreateDocumentRequestPayload) =
        api.createDocumentRequest(payload, "Bearer $token")

    suspend fun getRequests(token: String) =
        api.getDocumentRequests("Bearer $token")

    suspend fun getStats(token: String) =
        api.getDocumentRequestStats("Bearer $token")

    suspend fun getFiles(token: String) =
        api.getDocumentFiles("Bearer $token")

    suspend fun deleteRequest(token: String, id: String) =
        api.deleteDocumentRequest("Bearer $token", id)

    suspend fun getRequestById(token: String, id: String) =
        api.getDocumentRequestById(id, "Bearer $token")

    suspend fun getRequestFile(token: String, requestId: String) =
        api.getDocumentRequestFile(requestId, "Bearer $token")

    suspend fun updateStatus(token: String, id: String, status: String, reason: String? = null) =
        api.updateDocumentRequestStatus(
            id,
            mapOf("status" to status).let {
                if (reason != null) it + ("rejectionReason" to reason) else it
            },
            "Bearer $token"
        )

    suspend fun uploadFile(token: String, id: String, filePart: okhttp3.MultipartBody.Part) =
        api.uploadDocumentRequestFile(id, filePart, "Bearer $token")

    suspend fun updateReference(token: String, id: String, reference: String, hash: String) =
        api.updateDocumentReference(
            id,
            mapOf(
                "documentReference" to reference,
                "verificationHash" to hash
            ),
            "Bearer $token"
        )
}



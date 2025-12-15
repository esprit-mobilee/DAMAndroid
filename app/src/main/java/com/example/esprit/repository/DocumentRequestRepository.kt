package com.example.esprit.repository

import com.example.esprit.model.CreateDocumentRequestPayload
import com.example.esprit.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentRequestRepository @Inject constructor(
    private val api: ApiService
) {

    suspend fun getFormFields(type: String) =
        api.getDocumentFormFields(type)

    suspend fun createRequest(payload: CreateDocumentRequestPayload) =
        api.createDocumentRequest(payload)

    suspend fun getRequests() =
        api.getDocumentRequests()

    suspend fun getStats() =
        api.getDocumentRequestStats()

    suspend fun getFiles() =
        api.getDocumentFiles()

    suspend fun deleteRequest(id: String) =
        api.deleteDocumentRequest(id)

    suspend fun getRequestById(id: String) =
        api.getDocumentRequestById(id)

    suspend fun getRequestFile(requestId: String) =
        api.getDocumentRequestFile(requestId)

    suspend fun updateStatus(id: String, status: String, reason: String? = null) =
        api.updateDocumentRequestStatus(
            id,
            mapOf("status" to status).let {
                if (reason != null) it + ("rejectionReason" to reason) else it
            }
        )

    suspend fun uploadFile(id: String, filePart: okhttp3.MultipartBody.Part) =
        api.uploadDocumentRequestFile(id, filePart)

    suspend fun updateReference(id: String, reference: String, hash: String) =
        api.updateDocumentReference(
            id,
            mapOf(
                "documentReference" to reference,
                "verificationHash" to hash
            )
        )
}



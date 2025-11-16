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
}


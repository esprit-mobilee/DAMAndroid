package com.example.esprit.network

import com.example.esprit.model.*
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    // ---------- AUTH ----------
    @POST("auth/login")
    suspend fun login(
        @Body body: Map<String, String> // { identifiant, password }
    ): AuthResponse

    // ✅ FIX: this must match your Nest route → /auth/me (not /me)
    @GET("auth/me")
    suspend fun getMe(
        @Header("Authorization") token: String
    ): User

    // correspond à ton contrôleur NestJS: @Patch(':id')
    @PATCH("utilisateurs/{id}")
    suspend fun updateUser(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): User

    // ---------- ETUDIANT / DONNÉES PÉDAGOGIQUES ----------
    @GET("timetable")
    suspend fun getTimetable(
        @Header("Authorization") token: String
    ): List<TimetableItem>

    @GET("absences")
    suspend fun getAbsences(
        @Header("Authorization") token: String
    ): List<AbsenceItem>

    @GET("exams")
    suspend fun getExams(
        @Header("Authorization") token: String
    ): List<ExamItem>

    @GET("results")
    suspend fun getResults(
        @Header("Authorization") token: String
    ): List<ResultItem>

    @GET("internships")
    suspend fun getInternships(
        @Header("Authorization") token: String
    ): List<InternshipItem>

    // ---------- ANNONCES / PARTAGÉ ----------
    @GET("announcements")
    suspend fun getAnnouncements(
        @Header("Authorization") token: String
    ): List<Announcement>

    // ---------- OFFRES DE STAGE ----------
    @GET("internship-offers")
    suspend fun getInternshipOffers(
        @Header("Authorization") token: String
    ): List<InternshipItem>

    // ✅ FIX: make sure it matches Nest route — we’ll use PATCH /utilisateurs/me/password
    @PATCH("utilisateurs/me/password")
    suspend fun changeMyPassword(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Map<String, String>

    // ---------- DOCUMENT REQUESTS ----------
    @GET("document-request/form-fields/{type}")
    suspend fun getDocumentFormFields(
        @Path("type") type: String,
        @Header("Authorization") token: String
    ): DocumentFormFieldsResponse

    @POST("document-request")
    suspend fun createDocumentRequest(
        @Body body: CreateDocumentRequestPayload,
        @Header("Authorization") token: String
    ): DocumentRequestCreateResponse

    @GET("document-request")
    suspend fun getDocumentRequests(
        @Header("Authorization") token: String
    ): List<DocumentRequestItem>

    @GET("document-request/stats")
    suspend fun getDocumentRequestStats(
        @Header("Authorization") token: String
    ): DocumentRequestStats

    @GET("document-request/files")
    suspend fun getDocumentFiles(
        @Header("Authorization") token: String
    ): List<DocumentFileItem>

    @DELETE("document-request/{id}")
    suspend fun deleteDocumentRequest(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Map<String, String>

    @GET("document-request/request/{id}")
    suspend fun getDocumentRequestById(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): DocumentRequestItem

    @GET("document-request/request/{requestId}/file")
    suspend fun getDocumentRequestFile(
        @Path("requestId") requestId: String,
        @Header("Authorization") token: String
    ): DocumentFileItem

    @PATCH("document-request/{id}/status")
    suspend fun updateDocumentRequestStatus(
        @Path("id") id: String,
        @Body body: Map<String, String>, // { status, rejectionReason }
        @Header("Authorization") token: String
    ): DocumentRequestItem

    @retrofit2.http.Multipart
    @POST("document-request/{id}/file")
    suspend fun uploadDocumentRequestFile(
        @Path("id") id: String,
        @retrofit2.http.Part file: okhttp3.MultipartBody.Part,
        @Header("Authorization") token: String
    ): DocumentRequestItem
    @PATCH("document-request/{id}")
    suspend fun updateDocumentReference(
        @Path("id") id: String,
        @Body body: Map<String, String>, // { documentReference, verificationHash }
        @Header("Authorization") token: String
    ): DocumentRequestItem
}

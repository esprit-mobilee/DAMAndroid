package com.example.esprit.network

import com.example.esprit.model.*
import retrofit2.http.Body
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
}

package com.example.esprit.network

import com.example.esprit.model.*
import retrofit2.http.*

interface ApiService {

    // ---------------------------------------------------------
    // AUTH
    // ---------------------------------------------------------

    @POST("auth/login")
    suspend fun login(
        @Body body: Map<String, String>
    ): AuthResponse

    @GET("auth/me")
    suspend fun getMe(
        @Header("Authorization") token: String
    ): User

    @PATCH("utilisateurs/{id}")
    suspend fun updateUser(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): User

    @PATCH("utilisateurs/me/password")
    suspend fun changeMyPassword(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Map<String, String>


    // ---------------------------------------------------------
    // STUDENT / PEDAGOGIC DATA
    // ---------------------------------------------------------

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

    @GET("internship-offers")
    suspend fun getInternshipOffers(
        @Header("Authorization") token: String
    ): List<InternshipItem>


    // ---------------------------------------------------------
    // ANNOUNCEMENTS (TES ENDPOINTS COMPLETS)
    // ---------------------------------------------------------

    @GET("announcements")
    suspend fun getAnnouncements(): List<AnnouncementDto>

    @POST("announcements")
    suspend fun createAnnouncement(
        @Body req: CreateAnnouncementRequest
    ): AnnouncementDto

    @PUT("announcements/{id}")
    suspend fun updateAnnouncement(
        @Path("id") id: String,
        @Body req: CreateAnnouncementRequest
    ): AnnouncementDto

    @DELETE("announcements/{id}")
    suspend fun deleteAnnouncement(
        @Path("id") id: String
    )


    // ---------------------------------------------------------
    // MESSAGING (TES ENDPOINTS COMPLETS)
    // ---------------------------------------------------------

    // conversation entre deux utilisateurs
    @GET("messages/conversation/{u1}/{u2}")
    suspend fun getConversation(
        @Path("u1") user1: String,
        @Path("u2") user2: String
    ): List<MessageDto>

    // envoyer un message
    @POST("messages")
    suspend fun sendMessage(
        @Body req: SendMessageRequest
    ): MessageDto
}

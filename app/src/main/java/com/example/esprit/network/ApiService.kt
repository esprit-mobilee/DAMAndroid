package com.example.esprit.network

import com.example.esprit.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*
import com.example.esprit.model.Application

interface ApiService {

    // --------------------------------------------------
    // AUTH  (/api/auth/...)
    // --------------------------------------------------
    @POST("auth/login")
    suspend fun login(
        @Body body: Map<String, String>
    ): AuthResponse

    @GET("auth/me")
    suspend fun getMe(): User

    // --------------------------------------------------
    // UTILISATEURS
    // --------------------------------------------------
    @GET("utilisateurs/{id}")
    suspend fun getUserById(@Path("id") id: String): User

    @PATCH("utilisateurs/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): User

    @PATCH("utilisateurs/me/password")
    suspend fun changeMyPassword(
        @Body body: Map<String, String>
    ): Map<String, String>

    // --------------------------------------------------
    // ÉTUDIANT / DONNÉES PÉDAGOGIQUES
    // --------------------------------------------------
    @GET("timetable")
    suspend fun getTimetable(): List<TimetableItem>

    @GET("absences")
    suspend fun getAbsences(): List<AbsenceItem>

    @GET("exams")
    suspend fun getExams(): List<ExamItem>

    @GET("results")
    suspend fun getResults(): List<ResultItem>

    @GET("internships")
    suspend fun getInternships(): List<InternshipItem>

    @GET("announcements")
    suspend fun getAnnouncements(): List<Announcement>

    // ==================================================
    // OFFRES DE STAGE  (/api/internship-offers/...)
    // ==================================================
    @GET("internship-offers")
    suspend fun getInternshipOffers(): List<InternshipOffer>

    @GET("internship-offers/{id}")
    suspend fun getInternshipOffer(
        @Path("id") id: String
    ): InternshipOffer

    @Multipart
    @POST("internship-offers")
    suspend fun createInternshipOffer(
        @PartMap data: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part logo: MultipartBody.Part?
    ): InternshipOffer

    @Multipart
    @PUT("internship-offers/{id}")
    suspend fun updateInternshipOffer(
        @Path("id") id: String,
        @PartMap data: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part logo: MultipartBody.Part?
    ): InternshipOffer

    @DELETE("internship-offers/{id}")
    suspend fun deleteInternshipOffer(
        @Path("id") id: String
    ): Unit

    // ==================================================
    // VIE ÉTUDIANTE / CLUBS
    // ==================================================
    @GET("clubs")
    suspend fun getClubs(): List<Club>

    @GET("clubs/{id}")
    suspend fun getClubById(@Path("id") id: String): Club

    @Multipart
    @POST("clubs")
    suspend fun createClub(
        @Part image: MultipartBody.Part?,
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody?,
        @Part("president") president: RequestBody?,
        @Part("tags") tags: RequestBody?
    ): Club

    @Multipart
    @PUT("clubs/{id}")
    suspend fun updateClub(
        @Path("id") id: String,
        @Part image: MultipartBody.Part?,
        @Part("name") name: RequestBody?,
        @Part("description") description: RequestBody?,
        @Part("president") president: RequestBody?,
        @Part("tags") tags: RequestBody?
    ): Club

    @DELETE("clubs/{id}")
    suspend fun deleteClub(@Path("id") id: String): Map<String, String>

    @PUT("clubs/{clubId}/president/{userId}")
    suspend fun assignPresident(
        @Path("clubId") clubId: String,
        @Path("userId") userId: String
    ): Club

    @POST("clubs/{clubId}/join/{userId}")
    suspend fun addMemberToClub(
        @Path("clubId") clubId: String,
        @Path("userId") userId: String
    ): Club

    @POST("clubs/{clubId}/leave/{userId}")
    suspend fun removeMemberFromClub(
        @Path("clubId") clubId: String,
        @Path("userId") userId: String
    ): Club

    @GET("clubs/{clubId}/members")
    suspend fun getClubMembers(
        @Path("clubId") clubId: String
    ): List<User>

    // ==================================================
    // VIE ÉTUDIANTE / EVENTS
    // ==================================================
    @GET("events")
    suspend fun getEvents(): List<Event>

    @GET("events/{id}")
    suspend fun getEventById(@Path("id") id: String): Event

    @Multipart
    @POST("events")
    suspend fun createEvent(
        @PartMap data: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part image: MultipartBody.Part?
    ): Event

    @Multipart
    @PUT("events/{id}")
    suspend fun updateEvent(
        @Path("id") id: String,
        @PartMap data: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part image: MultipartBody.Part?
    ): Event

    @DELETE("events/{id}")
    suspend fun deleteEvent(@Path("id") id: String): Map<String, String>

    // ==================================================
    // APPLICATIONS DE STAGE (/api/applications)
    // ==================================================
    @GET("applications")
    suspend fun getApplications(): List<Application>

    @POST("applications")
    suspend fun createApplication(
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Application

    @PATCH("applications/{id}")
    suspend fun updateApplication(
        @Path("id") id: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Application

    @DELETE("applications/{id}")
    suspend fun deleteApplication(
        @Path("id") id: String
    ): Application

    @Multipart
    @POST("applications/upload")
    suspend fun uploadCvAndCreateApplication(
        @Part cv: MultipartBody.Part,
        @PartMap data: Map<String, @JvmSuppressWildcards RequestBody>
    ): Application

    // ==================================================
    // FAVORIS (/api/favorites)
    // ==================================================
    @GET("favorites")
    suspend fun getFavorites(): List<Favorite>

    @POST("favorites")
    suspend fun addFavorite(
        @Body body: Map<String, String>
    ): Favorite

    @DELETE("favorites/{id}")
    suspend fun deleteFavorite(
        @Path("id") id: String
    ): Map<String, String>
    
    @GET("favorites/internship/{internshipId}/user/{userId}")
    suspend fun getFavoriteByInternshipAndUser(
        @Path("internshipId") internshipId: String,
        @Path("userId") userId: String
    ): Favorite?
}

package com.example.esprit.network

import com.example.esprit.model.*
import com.example.esprit.model.club.*
import com.example.esprit.model.chat.*
import com.example.esprit.model.notification.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*
import com.example.esprit.model.Application

interface   ApiService {

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
    // NEW METHODS FROM CLUB BRANCH (Renamed where conflicting)
    // ==================================================
// ==================================================
    // CLUB (role club)
    // ==================================================
    @GET("clubs/{clubId}/members")
    suspend fun getClubMembers(@Path("clubId") clubId: String): List<ClubMemberDto>
    @GET("clubs/{id}")
    suspend fun getClub(@Path("id") id: String): ClubHomeDto

    @GET("events")
    suspend fun getEvents(): List<ClubEventDto>

    @Multipart
    @POST("events")
    suspend fun createClubEvent(
        @Part("title") title: RequestBody,
        @Part("startDate") startDate: RequestBody,
        @Part("endDate") endDate: RequestBody,
        @Part("location") location: RequestBody?,
        @Part("description") description: RequestBody?,
        @Part("category") category: RequestBody?,
        @Part("formQuestions") formQuestions: RequestBody?,
        @Part image: MultipartBody.Part?
    ): ClubEventDto
    @Multipart
    @POST("events")
    suspend fun createEvent(
        @Part("title") title: RequestBody,
        @Part("startDate") startDate: RequestBody,
        @Part("endDate") endDate: RequestBody,
        @Part("location") location: RequestBody?,
        @Part("description") description: RequestBody?,
        @Part("category") category: RequestBody?,
        @Part("formQuestions") formQuestions: RequestBody?,
        @Part image: MultipartBody.Part?
    ): ClubEventDto
    @DELETE("events/{id}")
    suspend fun deleteEvent(@Path("id") id: String): Unit
    @Multipart
    @PUT("events/{id}")
    suspend fun updateEvent(
        @Path("id") id: String,
        @Part("title") title: RequestBody,
        @Part("startDate") startDate: RequestBody,
        @Part("endDate") endDate: RequestBody,
        @Part("location") location: RequestBody?,
        @Part("description") description: RequestBody?,
        @Part("category") category: RequestBody?,
        @Part("formQuestions") formQuestions: RequestBody?,
        @Part image: MultipartBody.Part?
    ): ClubEventDto

    @Multipart
    @PUT("events/{id}")
    suspend fun updateClubEvent(
        @Path("id") id: String,
        @Part("title") title: RequestBody,
        @Part("startDate") startDate: RequestBody,
        @Part("endDate") endDate: RequestBody,
        @Part("location") location: RequestBody?,
        @Part("description") description: RequestBody?,
        @Part("category") category: RequestBody?,
        @Part("formQuestions") formQuestions: RequestBody?,
        @Part image: MultipartBody.Part?
    ): ClubEventDto

    @DELETE("events/{id}")
    suspend fun deleteClubEvent(@Path("id") id: String): Unit

    @POST("events/{id}/toggle-registration")
    suspend fun toggleEventRegistration(@Path("id") id: String): ClubEventDto

    @POST("events/{id}/join")
    suspend fun joinEvent(
        @Path("id") id: String,
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): Map<String, Any?>

    @GET("events/{id}/registrations")
    suspend fun getEventRegistrations(@Path("id") id: String): List<EventRegistrationDto>

    @POST("events/{id}/registrations/{userId}/approve")
    suspend fun approveEventRegistration(
        @Path("id") id: String,
        @Path("userId") userId: String
    ): Map<String, String>

    @POST("events/{id}/registrations/{userId}/reject")
    suspend fun rejectEventRegistration(
        @Path("id") id: String,
        @Path("userId") userId: String
    ): Map<String, String>

    @GET("posts/club/{clubId}")
    suspend fun getClubPosts(@Path("clubId") clubId: String): List<ClubPostDto>

    @Multipart
    @POST("posts/{clubId}")
    suspend fun createClubPost(
        @Path("clubId") clubId: String,
        @Part("text") text: RequestBody,
        @Part image: MultipartBody.Part?
    ): ClubPostDto

    @Multipart
    @PUT("posts/{id}")
    suspend fun updateClubPost(
        @Path("id") id: String,
        @Part("text") text: RequestBody,
        @Part image: MultipartBody.Part?
    ): ClubPostDto

    @DELETE("posts/{id}")
    suspend fun deleteClubPost(@Path("id") id: String): Unit

    @POST("posts/{id}/like")
    suspend fun likePost(@Path("id") id: String): ClubPostDto

    @POST("posts/{id}/dislike")
    suspend fun dislikePost(@Path("id") id: String): ClubPostDto

    @POST("posts/{id}/comment")
    suspend fun commentPost(
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): ClubPostDto

    @PUT("posts/{postId}/comments/{commentId}")
    suspend fun updateComment(
        @Path("postId") postId: String,
        @Path("commentId") commentId: String,
        @Body body: Map<String, String>
    ): ClubPostDto

    @DELETE("posts/{postId}/comments/{commentId}")
    suspend fun deleteComment(
        @Path("postId") postId: String,
        @Path("commentId") commentId: String
    ): ClubPostDto

    @POST("posts/{postId}/comments/{commentId}/react")
    suspend fun reactToComment(
        @Path("postId") postId: String,
        @Path("commentId") commentId: String,
        @Body body: Map<String, String>
    ): ClubPostDto

    @POST("posts/{postId}/comments/{commentId}/reply")
    suspend fun replyToComment(
        @Path("postId") postId: String,
        @Path("commentId") commentId: String,
        @Body body: Map<String, String>
    ): ClubPostDto

    // Note: getClubMembers already exists above returning List<User>.
    // Club Members repo might expect List<ClubMemberDto>?
    // If so, we need another one.
    @GET("clubs/{clubId}/members")
    suspend fun getClubMembersDto(@Path("clubId") clubId: String): List<ClubMemberDto>

    @POST("clubs/{clubId}/leave/{userId}")
    suspend fun leaveClub(
        @Path("clubId") clubId: String,
        @Path("userId") userId: String
    ): Unit

    @POST("clubs/{id}/toggle-join")
    suspend fun toggleJoinEnabled(@Path("id") id: String): ClubHomeDto

    @Multipart
    @PUT("clubs/{id}")
    suspend fun updateClubProfile(
        @Path("id") id: String,
        @Part("name") name: RequestBody?,
        @Part("description") description: RequestBody?,
        @Part("tags") tags: RequestBody?,
        @Part profileImage: MultipartBody.Part?,
        @Part coverImage: MultipartBody.Part?
    ): ClubHomeDto

    // --------------------------------------------------
    // STUDENT FEATURES
    // --------------------------------------------------
    @GET("posts/feed")
    suspend fun getStudentFeed(): List<ClubPostDto>
    @GET("clubs")
    suspend fun getAllClubs(): List<ClubHomeDto>
    @POST("clubs/{id}/join-request")
    suspend fun requestJoinClub(
        @Path("id") clubId: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Map<String, Any>

    @PUT("clubs/{id}/settings")
    suspend fun updateClubSettings(
        @Path("id") id: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): ClubHomeDto

    @GET("clubs/{clubId}/join-requests/pending")
    suspend fun getPendingRequests(@Path("clubId") clubId: String): List<JoinRequestDto>

    @POST("clubs/join-requests/{requestId}/approve")
    suspend fun approveJoinRequest(@Path("requestId") requestId: String): Map<String, String>

    @POST("clubs/join-requests/{requestId}/reject")
    suspend fun rejectJoinRequest(@Path("requestId") requestId: String): Map<String, String>

    @GET("clubs/{clubId}/join-request-status")
    suspend fun checkJoinRequestStatus(@Path("clubId") clubId: String): Map<String, Any>

    // --------------------------------------------------
    // NOTIFICATIONS
    // --------------------------------------------------
    @GET("notifications/club/{clubId}")
    suspend fun getClubNotifications(@Path("clubId") clubId: String): List<NotificationDto>

    @GET("notifications/club/{clubId}/unread-count")
    suspend fun getUnreadCount(@Path("clubId") clubId: String): Map<String, Int>

    @PATCH("notifications/{id}/read")
    suspend fun markNotificationAsRead(@Path("id") id: String): NotificationDto

    @DELETE("notifications/{id}")
    suspend fun deleteNotification(@Path("id") id: String): Map<String, String>


    // ==================================================



    @GET("chat/history/{clubId}")
    suspend fun getChatHistory(
        @Path("clubId") clubId: String,
        @Query("limit") limit: Int? = 50,
        @Query("before") before: String? = null
    ): List<MessageDto>

    @GET("chat/conversations/{userId}")
    suspend fun getConversations(@Path("userId") userId: String): List<ConversationDto>

    @GET("chat/private/{userId}/{partnerId}")
    suspend fun getPrivateHistory(
        @Path("userId") userId: String,
        @Path("partnerId") partnerId: String,
        @Query("limit") limit: Int = 50,
        @Query("before") before: String? = null
    ): List<MessageDto>
    
    @POST("chat/translate")
    suspend fun translateMessage(@Body body: TranslateRequest): TranslationResponse

    @Multipart
    @POST("chat/upload")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part
    ): Map<String, String>


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

    // Planifier un entretien
    @POST("applications/{id}/schedule-interview")
    suspend fun scheduleInterview(
        @Path("id") applicationId: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>
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

    // ==================================================
    // FORGOT PASSWORD
    // ==================================================
    @POST("auth/forgot-password")
    suspend fun forgotPassword(
        @Body body: Map<String, String>
    ): Map<String, String>

    @POST("auth/verify-code")
    suspend fun verifyCode(
        @Body body: Map<String, String>
    ): Map<String, String>

    @POST("auth/reset-password")
    suspend fun resetPassword(
        @Body body: Map<String, String>
    ): Map<String, String>



    // ---------- DOCUMENT REQUESTS ----------
    @GET("document-request/form-fields/{type}")
    suspend fun getDocumentFormFields(
        @Path("type") type: String
    ): DocumentFormFieldsResponse

    @POST("document-request")
    suspend fun createDocumentRequest(
        @Body body: CreateDocumentRequestPayload
    ): DocumentRequestCreateResponse

    @GET("document-request")
    suspend fun getDocumentRequests(): List<DocumentRequestItem>

    @GET("document-request/stats")
    suspend fun getDocumentRequestStats(): DocumentRequestStats

    @GET("document-request/files")
    suspend fun getDocumentFiles(): List<DocumentFileItem>

    @DELETE("document-request/{id}")
    suspend fun deleteDocumentRequest(
        @Path("id") id: String
    ): Map<String, String>

    @GET("document-request/request/{id}")
    suspend fun getDocumentRequestById(
        @Path("id") id: String
    ): DocumentRequestItem

    @GET("document-request/request/{requestId}/file")
    suspend fun getDocumentRequestFile(
        @Path("requestId") requestId: String
    ): DocumentFileItem

    @PATCH("document-request/{id}/status")
    suspend fun updateDocumentRequestStatus(
        @Path("id") id: String,
        @Body body: Map<String, String> // { status, rejectionReason }
    ): DocumentRequestItem

    @retrofit2.http.Multipart
    @POST("document-request/{id}/file")
    suspend fun uploadDocumentRequestFile(
        @Path("id") id: String,
        @retrofit2.http.Part file: okhttp3.MultipartBody.Part
    ): DocumentRequestItem
    @PATCH("document-request/{id}")
    suspend fun updateDocumentReference(
        @Path("id") id: String,
        @Body body: Map<String, String> // { documentReference, verificationHash }
    ): DocumentRequestItem
}



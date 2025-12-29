package com.example.esprit.ui.nav

object Destinations {

    const val SPLASH = "splash"
    const val LOGIN = "login"

    const val STUDENT_HOME = "student_home"
    const val TEACHER_HOME = "teacher_home"
    const val PARENT_HOME = "parent_home"
    const val ADMIN_HOME = "admin_home"
    const val CLUB_HOME = "club_home"
    const val CLUB_EVENTS = "club/events"
    const val CLUB_EVENT_CREATE = "club/events/create"

    // DETAIL / EDIT (dynamic routes)
    const val CLUB_EVENT_DETAIL = "club/events/detail/{eventId}"
    const val CLUB_EVENT_EDIT = "club/events/edit/{eventId}"

    fun clubEventDetailRoute(eventId: String) =
        "club/events/detail/$eventId"

    fun clubEventEditRoute(eventId: String) =
        "club/events/edit/$eventId"
    const val CLUB_POSTS = "club/posts"
    const val CLUB_POST_CREATE = "club/posts/create/{clubId}"
    const val CLUB_POST_EDIT = "club/posts/edit/{postId}"
    
    const val CLUB_POST_DETAIL = "post_detail/{postId}"
    fun clubPostDetailRoute(postId: String) = "post_detail/$postId"

    const val CLUB_MEMBERS = "club/members"
    const val CLUB_SETTINGS = "club/settings"
    const val CLUB_NOTIFICATIONS = "club/notifications"
    const val CLUB_REQUESTS = "club/requests"

    const val TIMETABLE = "timetable"
    const val ABSENCES = "absences"
    const val ANNOUNCEMENTS = "announcements"
    const val PROFILE = "profile"

    // Document Requests
    const val DOCUMENT_REQUEST_FORM = "document_request_form"
    const val DOCUMENT_REQUEST_HISTORY = "document_request_history"
    const val DOCUMENT_REQUEST_DETAIL = "document_request_detail/{requestId}"
    const val DOCUMENT_VIEWER = "document_viewer/{fileUrl}"

    // Admin Requests
    const val ADMIN_DOCUMENT_REQUESTS = "admin_document_requests"
    const val ADMIN_DOCUMENT_REQUEST_DETAIL = "admin_document_request_detail/{requestId}"




    // --- Offres de stage ---
    // Admin
    const val ADMIN_INTERNSHIP_LIST = "admin/internships"
    const val ADMIN_INTERNSHIP_CREATE = "admin/internships/create"
    const val ADMIN_INTERNSHIP_EDIT = "admin/internships/edit/{id}"

    // Shared details (admin + student)
    const val INTERNSHIP_DETAILS = "internships/details/{id}"

    const val MESSAGES = "messages"

    // Student
    const val STUDENT_INTERNSHIP_LIST = "student/internships"
    const val STUDENT_APPLY = "student/internships/{id}/apply"
    const val STUDENT_APPLICATIONS = "student/applications"
    const val STUDENT_APPLICATION_DETAILS = "student/applications/{id}"
    const val STUDENT_APPLICATION_EDIT = "student/applications/{id}/edit"
    const val STUDENT_FAVORITES = "student/internships/favorites"
    const val STUDENT_SEARCH = "student/internships/search"

    const val FORGOT_PASSWORD = "forgot_password"

    // Admin Applications
    const val ADMIN_APPLICATIONS_LIST = "admin/applications"
    const val ADMIN_APPLICATION_DETAILS = "admin/applications/{id}"
    const val SCHEDULE_INTERVIEW = "admin/applications/{id}/schedule_interview"

    // AI Chat Assistant
    const val AI_CHAT = "student/ai_chat"
    const val AI_CHAT_HISTORY = "student/ai_chat/history"

    // --- Student Club Features ---
    const val STUDENT_FEED = "student/feed"
    const val STUDENT_CLUBS = "student/clubs"
    const val STUDENT_CLUB_PROFILE = "student/club/{clubId}"
    
    fun studentClubProfileRoute(clubId: String) = "student/club/$clubId"

    fun clubPostCreateRoute(clubId: String) = "club/posts/create/$clubId"
    fun clubPostEditRoute(postId: String) = "club/posts/edit/$postId"

    const val CLUB_CHAT = "club/chat/{clubId}?name={name}"
    fun clubChatRoute(clubId: String, name: String) = "club/chat/$clubId?name=$name"



    const val PRIVATE_CHAT = "chat/private/{partnerId}?name={name}"
    fun privateChatRoute(partnerId: String, name: String) = "chat/private/$partnerId?name=$name"

    const val CONTACT_LIST = "chat/contacts"
}

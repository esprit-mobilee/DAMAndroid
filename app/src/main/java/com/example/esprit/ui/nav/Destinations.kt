package com.example.esprit.ui.nav

object Destinations {
    const val SPLASH = "splash"
    const val LOGIN = "login"

    const val STUDENT_HOME = "student_home"
    const val TEACHER_HOME = "teacher_home"
    const val PARENT_HOME = "parent_home"
    const val ADMIN_HOME = "admin_home"

    const val TIMETABLE = "timetable"
    const val ABSENCES = "absences"
    const val ANNOUNCEMENTS = "announcements"

    // 👇 New route for profile/security screen (changer le mot de passe)
    const val PROFILE = "profile"

    const val DOCUMENT_REQUEST_FORM = "document_request_form"
    const val DOCUMENT_REQUEST_HISTORY = "document_request_history"
    const val DOCUMENT_REQUEST_DETAIL = "document_request_detail/{requestId}"
    const val DOCUMENT_VIEWER = "document_viewer/{fileUrl}"

    // Admin Requests
    const val ADMIN_DOCUMENT_REQUESTS = "admin_document_requests"
    const val ADMIN_DOCUMENT_REQUEST_DETAIL = "admin_document_request_detail/{requestId}"
}

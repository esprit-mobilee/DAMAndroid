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
    const val PROFILE = "profile"

    // Vie étudiante
    const val VIE_ETUDIANTE = "vie_etudiante"
    const val EVENT_FORM = "event_form"
    const val CLUB_FORM = "club_form"
    const val EVENT_DETAILS = "event_details"
    const val CLUB_DETAILS = "club_details"

    // --- Offres de stage ---
    // Admin
    const val ADMIN_INTERNSHIP_LIST = "admin/internships"
    const val ADMIN_INTERNSHIP_CREATE = "admin/internships/create"
    const val ADMIN_INTERNSHIP_EDIT = "admin/internships/edit/{id}"

    // Shared details (admin + student)
    const val INTERNSHIP_DETAILS = "internships/details/{id}"

    // Student
    const val STUDENT_INTERNSHIP_LIST = "student/internships"
    const val STUDENT_APPLY = "student/internships/{id}/apply"
    const val STUDENT_APPLICATIONS = "student/applications"
    const val STUDENT_APPLICATION_DETAILS = "student/applications/{id}"
    const val STUDENT_APPLICATION_EDIT = "student/applications/{id}/edit"
    const val STUDENT_FAVORITES = "student/internships/favorites"
    const val STUDENT_SEARCH = "student/internships/search"

    // Admin Applications
    const val ADMIN_APPLICATIONS_LIST = "admin/applications"
    const val ADMIN_APPLICATION_DETAILS = "admin/applications/{id}"
    const val SCHEDULE_INTERVIEW = "admin/applications/{id}/schedule_interview"

    // AI Chat Assistant
    const val AI_CHAT = "student/ai_chat"
    const val AI_CHAT_HISTORY = "student/ai_chat/history"
    
    // Auth
    const val FORGOT_PASSWORD = "auth/forgot_password"
}

package com.example.esprit.ui.nav

object Destinations {

    // ---------- AUTH ----------
    const val SPLASH = "splash"
    const val LOGIN = "login"

    // ---------- ROLES ----------
    const val STUDENT_HOME = "student_home"
    const val TEACHER_HOME = "teacher_home"
    const val PARENT_HOME = "parent_home"
    const val ADMIN_HOME = "admin_home"

    // ---------- ETUDIANT / DONNÉES PÉDAGOGIQUES ----------
    const val TIMETABLE = "timetable"
    const val ABSENCES = "absences"

    // ---------- ANNOUNCEMENTS ----------
    const val ANNOUNCEMENTS = "announcements"
    const val ANNOUNCEMENT_ADD = "announcement_add"
    const val ANNOUNCEMENT_EDIT = "announcement_edit/{id}"
    const val ANNOUNCEMENT_DETAILS = "announcement_details/{id}"

    // ---------- PROFILE ----------
    const val PROFILE = "profile"

    // ---------- MESSAGING ----------
    const val MESSAGES = "messages"
    const val CHAT = "chat/{peerId}/{peerName}"
}

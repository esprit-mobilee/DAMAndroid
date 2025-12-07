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
    const val CLUB_MEMBERS = "club/members"
    const val CLUB_SETTINGS = "club/settings"
    const val CLUB_NOTIFICATIONS = "club/notifications"
    const val CLUB_REQUESTS = "club/requests"

    const val TIMETABLE = "timetable"
    const val ABSENCES = "absences"
    const val ANNOUNCEMENTS = "announcements"
    const val PROFILE = "profile"




    // --- Internships ---
    const val ADMIN_INTERNSHIP_LIST = "admin/internships"
    const val ADMIN_INTERNSHIP_CREATE = "admin/internships/create"
    const val ADMIN_INTERNSHIP_EDIT = "admin/internships/edit/{id}"

    const val INTERNSHIP_DETAILS = "internships/details/{id}"
    const val STUDENT_INTERNSHIP_LIST = "student/internships"
    
    const val MESSAGES = "messages"

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

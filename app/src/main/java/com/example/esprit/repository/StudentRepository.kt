package com.example.esprit.repository

import com.example.esprit.network.ApiService
import com.example.esprit.network.safeCall
import com.example.esprit.util.UiState
import javax.inject.Inject

class StudentRepository @Inject constructor(
    private val api: ApiService
) {
    // ✅ No token argument needed anymore
    suspend fun timetable() = safeCall { api.getTimetable() }
    suspend fun absences() = safeCall { api.getAbsences() }
    suspend fun exams() = safeCall { api.getExams() }
    suspend fun results() = safeCall { api.getResults() }
    suspend fun internships() = safeCall { api.getInternships() }
    suspend fun announcements() = safeCall { api.getAnnouncements() }

    // Missing methods
    suspend fun getMe() = safeCall { api.getMe() }
    suspend fun getClubDetails(clubId: String): com.example.esprit.util.UiState<com.example.esprit.model.club.ClubHomeDto> = com.example.esprit.network.safeCall {
        api.getClub(clubId)
    }
    suspend fun getAllClubs() = safeCall { api.getAllClubs() }
    suspend fun getFeed() = safeCall { api.getStudentFeed() }

    suspend fun joinClub(clubId: String, answers: List<Map<String, String>> = emptyList()) = safeCall {
        val body = mutableMapOf<String, Any>("answers" to answers)
        api.requestJoinClub(clubId, body)
    }
}

package com.example.esprit.repository

import com.example.esprit.network.ApiService
import javax.inject.Inject

class StudentRepository @Inject constructor(
    private val api: ApiService
) {
    // ✅ No token argument needed anymore
    suspend fun timetable() = api.getTimetable()
    suspend fun absences() = api.getAbsences()
    suspend fun exams() = api.getExams()
    suspend fun results() = api.getResults()
    suspend fun internships() = api.getInternships()
    suspend fun announcements() = api.getAnnouncements()

    // New features using UiState pattern
    suspend fun getFeed(): com.example.esprit.util.UiState<List<com.example.esprit.model.club.ClubPostDto>> = com.example.esprit.network.safeCall {
        api.getStudentFeed()
    }

    suspend fun getAllClubs(): com.example.esprit.util.UiState<List<com.example.esprit.model.club.ClubHomeDto>> = com.example.esprit.network.safeCall {
        api.getAllClubs()
    }

    suspend fun joinClub(
        clubId: String,
        answers: List<Map<String, String>> = emptyList()
    ): com.example.esprit.util.UiState<Unit> = com.example.esprit.network.safeCall {
        val body = mapOf("answers" to answers)
        api.requestJoinClub(clubId, body)
        Unit
    }
    
    suspend fun getClubDetails(clubId: String): com.example.esprit.util.UiState<com.example.esprit.model.club.ClubHomeDto> = com.example.esprit.network.safeCall {
        api.getClub(clubId)
    }

    suspend fun getMe(): com.example.esprit.util.UiState<com.example.esprit.model.User> = com.example.esprit.network.safeCall {
        api.getMe()
    }
}

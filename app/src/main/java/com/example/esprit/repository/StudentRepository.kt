package com.example.esprit.repository


import com.example.esprit.network.ApiService
import javax.inject.Inject

class StudentRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun timetable(token: String) = api.getTimetable("Bearer $token")
    suspend fun absences(token: String) = api.getAbsences("Bearer $token")
    suspend fun exams(token: String) = api.getExams("Bearer $token")
    suspend fun results(token: String) = api.getResults("Bearer $token")
    suspend fun internships(token: String) = api.getInternships("Bearer $token")
    suspend fun announcements(token: String) = api.getAnnouncements("Bearer $token")
}

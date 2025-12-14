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
}

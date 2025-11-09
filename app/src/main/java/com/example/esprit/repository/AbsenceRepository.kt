package com.example.esprit.repository

import com.example.esprit.model.AbsenceItem
import com.example.esprit.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AbsenceRepository @Inject constructor(
    private val api: ApiService
) {
    /**
     * Fetches the list of absences for the authenticated user.
     * Requires a valid JWT token.
     */
    suspend fun getAbsences(token: String): List<AbsenceItem> {
        return api.getAbsences("Bearer $token")
    }
}

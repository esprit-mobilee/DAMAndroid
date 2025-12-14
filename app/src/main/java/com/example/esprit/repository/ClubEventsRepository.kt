package com.example.esprit.repository

import com.example.esprit.model.club.ClubEventDto
import com.example.esprit.model.club.EventRegistrationDto
import com.example.esprit.network.ApiService
import com.example.esprit.network.safeCall
import com.example.esprit.util.UiState
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

data class EventPayload(
    val title: RequestBody,
    val startDate: RequestBody,
    val endDate: RequestBody,
    val location: RequestBody? = null,
    val description: RequestBody? = null,
    val category: RequestBody? = null,
    val formQuestions: RequestBody? = null,
    val image: MultipartBody.Part? = null
)

class ClubEventsRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun list(): UiState<List<ClubEventDto>> = safeCall { api.getEvents() }

    suspend fun create(payload: EventPayload): UiState<ClubEventDto> = safeCall {
        api.createEvent(
            payload.title,
            payload.startDate,
            payload.endDate,
            payload.location,
            payload.description,
            payload.category,
            payload.formQuestions,
            payload.image
        )
    }

    suspend fun update(id: String, payload: EventPayload): UiState<ClubEventDto> = safeCall {
        api.updateEvent(
            id,
            payload.title,
            payload.startDate,
            payload.endDate,
            payload.location,
            payload.description,
            payload.category,
            payload.formQuestions,
            payload.image
        )
    }

    suspend fun delete(id: String): UiState<Unit> = safeCall { api.deleteEvent(id) }

    suspend fun toggleRegistration(id: String): UiState<ClubEventDto> =
        safeCall { api.toggleEventRegistration(id) }

    suspend fun joinEvent(id: String, body: Map<String, Any?>): UiState<Map<String, Any?>> =
        safeCall { api.joinEvent(id, body) }

    suspend fun getRegistrations(id: String): UiState<List<EventRegistrationDto>> =
        safeCall { api.getEventRegistrations(id) }

    suspend fun approveRegistration(eventId: String, userId: String): UiState<Map<String, String>> =
        safeCall { api.approveEventRegistration(eventId, userId) }

    suspend fun rejectRegistration(eventId: String, userId: String): UiState<Map<String, String>> =
        safeCall { api.rejectEventRegistration(eventId, userId) }
}

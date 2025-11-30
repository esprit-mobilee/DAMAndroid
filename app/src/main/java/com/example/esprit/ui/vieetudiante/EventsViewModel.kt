package com.example.esprit.ui.vieetudiante

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.Event
import com.example.esprit.repository.EventRepository
import com.example.esprit.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _events = MutableStateFlow<UiState<List<Event>>>(UiState.Loading)
    val events = _events.asStateFlow()

    // plus tard: calculer selon le rôle
    val canCreateEvent: Boolean = true

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            _events.value = UiState.Loading
            val result = eventRepository.getEvents()
            _events.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Erreur de chargement") }
            )
        }
    }

    // --------------------------------------------------
    // CREATE
    // --------------------------------------------------

    fun createEvent(
        title: String,
        description: String?,
        dateIso: String,
        location: String?,
        organizerId: String,
        category: String?,
        imageFile: File?,              // 👈 NEW
        onDone: () -> Unit
    ) {
        if (organizerId.isBlank()) return

        viewModelScope.launch {
            eventRepository.createEvent(
                title = title,
                description = description,
                dateIso = dateIso,
                location = location,
                organizerId = organizerId,
                category = category,
                imageFile = imageFile
            )
            loadEvents()
            onDone()
        }
    }

    fun createEvent(event: Event, onDone: () -> Unit) {
        val title = event.title ?: return
        val dateIso = event.date ?: return
        val organizerId = extractOrganizerId(event) ?: return

        createEvent(
            title = title,
            description = event.description,
            dateIso = dateIso,
            location = event.location,
            organizerId = organizerId,
            category = event.category,
            imageFile = null,     // depuis un Event existant, pas d’image locale
            onDone = onDone
        )
    }

    private fun extractOrganizerId(event: Event): String? {
        val org = event.organizer ?: return null
        return when (org) {
            is String -> org
            is Map<*, *> -> org["_id"] as? String
            else -> null
        }
    }

    // --------------------------------------------------
    // UPDATE
    // --------------------------------------------------
    fun updateEvent(
        id: String,
        title: String,
        description: String?,
        dateIso: String,
        location: String?,
        category: String?,
        imageFile: File?,              // 👈 NEW
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            val fields = mapOf<String, Any?>(
                "title" to title,
                "description" to description,
                "date" to dateIso,
                "location" to location,
                "category" to category
            )

            eventRepository.updateEvent(id, fields, imageFile)
            loadEvents()
            onDone()
        }
    }

    // --------------------------------------------------
    // DELETE
    // --------------------------------------------------
    fun deleteEvent(
        id: String,
        onDone: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            eventRepository.deleteEvent(id)
            loadEvents()
            onDone?.invoke()
        }
    }
}

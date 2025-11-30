package com.example.esprit.ui.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.AnnouncementDto
import com.example.esprit.repository.AnnouncementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val CURRENT_USER_ID = "691e24db7a1a6b2eb5bc6617"

@HiltViewModel
class AnnouncementsViewModel @Inject constructor(
    private val repo: AnnouncementRepository
) : ViewModel() {

    private val _announcements = MutableStateFlow<List<AnnouncementDto>>(emptyList())
    val announcements: StateFlow<List<AnnouncementDto>> = _announcements

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val badWords = listOf(
        "stupid", "fuck", "shit", "merde", "pute",
        "spam", "escroquerie", "arnaque", "insulte", "badword"
    )

    init {
        refresh()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun refresh() = viewModelScope.launch {
        _announcements.value = repo.list()
    }

    fun addAnnouncement(
        title: String,
        content: String,
        audience: String,
        onDone: (Boolean) -> Unit
    ) = viewModelScope.launch {

        val text = "$title $content".lowercase()

        // ----------------------------
        // 1. BAD WORD CHECK
        // ----------------------------
        if (badWords.any { text.contains(it) }) {
            _errorMessage.value = "Votre annonce contient des propos interdits ou du spam."
            onDone(false)
            return@launch
        }

        // ----------------------------
        // 2. DUPLICATE CHECK
        // ----------------------------
        val existing = _announcements.value

        val duplicated = existing.any { ann ->
            ann.title.trim().equals(title.trim(), ignoreCase = true) &&
                    ann.content.trim().equals(content.trim(), ignoreCase = true)
        }

        if (duplicated) {
            _errorMessage.value =
                "Une annonce identique (même titre et même contenu) existe déjà."
            onDone(false)
            return@launch
        }

        // ----------------------------
        // 3. OK → Create
        // ----------------------------
        repo.create(title, content, audience, CURRENT_USER_ID)
        refresh()
        onDone(true)
    }

    fun updateAnnouncement(
        id: String,
        title: String,
        content: String,
        audience: String
    ) = viewModelScope.launch {
        repo.update(id, title, content, audience, CURRENT_USER_ID)
        refresh()
    }

    fun deleteAnnouncement(id: String) = viewModelScope.launch {
        repo.delete(id)
        _announcements.value = _announcements.value.filterNot { it.id == id }
    }
}

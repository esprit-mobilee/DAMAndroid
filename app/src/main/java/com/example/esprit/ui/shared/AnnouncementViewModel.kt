package com.example.esprit.ui.shared

import com.example.esprit.model.AnnouncementDto
import com.example.esprit.repository.AnnouncementRepository



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ID utilisateur fixe pour tests
private const val CURRENT_USER_ID = "691e24db7a1a6b2eb5bc6617" //manel
//691e2126d4558f41c78b085a
@HiltViewModel
class AnnouncementsViewModel @Inject constructor(
    private val repo: AnnouncementRepository
) : ViewModel() {

    private val _announcements = MutableStateFlow<List<AnnouncementDto>>(emptyList())
    val announcements: StateFlow<List<AnnouncementDto>> = _announcements.asStateFlow()

    init {
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        _announcements.value = repo.list()
    }

    fun addAnnouncement(title: String, content: String, audience: String) =
        viewModelScope.launch {
            repo.create(title, content, audience, CURRENT_USER_ID)
            refresh()
        }

    fun updateAnnouncement(id: String, title: String, content: String, audience: String) =
        viewModelScope.launch {
            repo.update(id, title, content, audience, CURRENT_USER_ID)
            refresh()
        }

    fun deleteAnnouncement(id: String) = viewModelScope.launch {
        repo.delete(id)
        _announcements.value = _announcements.value.filterNot { it.id == id }
    }
}

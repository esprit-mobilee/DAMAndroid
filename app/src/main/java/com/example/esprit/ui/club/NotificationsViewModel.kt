package com.example.esprit.ui.club

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
<<<<<<< HEAD
import com.example.esprit.model.notification.Notification
=======
import com.example.esprit.model.notification.NotificationDto
>>>>>>> origin/messaging-announcement
import com.example.esprit.repository.NotificationsRepository
import com.example.esprit.repository.ClubRepository
import com.example.esprit.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val loading: Boolean = false,
<<<<<<< HEAD
    val notifications: List<Notification> = emptyList(),
=======
    val notifications: List<NotificationDto> = emptyList(),
>>>>>>> origin/messaging-announcement
    val unreadCount: Int = 0,
    val error: String? = null
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
<<<<<<< HEAD
    private val repo: NotificationsRepository
=======
    private val repo: NotificationsRepository,
    private val clubRepo: ClubRepository
>>>>>>> origin/messaging-announcement
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationsUiState(loading = true))
    val uiState: StateFlow<NotificationsUiState> = _uiState

<<<<<<< HEAD
    init {
        observeRealTimeNotifications()
    }

    private fun observeRealTimeNotifications() {
        viewModelScope.launch {
            repo.notifications.collect { notification ->
                val currentList = _uiState.value.notifications.toMutableList()
                // Add new notification to top
                currentList.add(0, notification)
                
                _uiState.value = _uiState.value.copy(
                    notifications = currentList,
                    unreadCount = _uiState.value.unreadCount + 1
                )
            }
        }
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)
            
            // Load my notifications (unified stream)
            when (val res = repo.getMyNotifications()) {
                is UiState.Success<*> -> {
                    val data = res.data as? List<Notification> ?: emptyList()
=======
    fun load() {
        viewModelScope.launch {
            // Get club ID from club repository
            val clubRes = clubRepo.home()
            if (clubRes !is UiState.Success<*> || clubRes.data == null) {
                _uiState.value = NotificationsUiState(error = "Club non trouvé")
                return@launch
            }
            
            val clubId = (clubRes.data as? com.example.esprit.model.club.ClubHomeDto)?.id ?: return@launch

            // Load notifications
            when (val res = repo.getClubNotifications(clubId)) {
                is UiState.Success<*> -> {
                    val data = res.data as? List<NotificationDto> ?: emptyList()
>>>>>>> origin/messaging-announcement
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        notifications = data
                    )
<<<<<<< HEAD
                    // Calculate unread count locally
=======
                    // Calculate unread count locally or fetch from API
>>>>>>> origin/messaging-announcement
                    val unread = data.count { !it.read }
                    _uiState.value = _uiState.value.copy(unreadCount = unread)
                }
                is UiState.Error -> _uiState.value = NotificationsUiState(error = res.message)
                UiState.Loading -> _uiState.value = NotificationsUiState(loading = true)
            }
        }
    }

<<<<<<< HEAD
    fun markAsRead(notification: Notification) {
=======
    fun markAsRead(notification: NotificationDto) {
>>>>>>> origin/messaging-announcement
        if (notification.read) return

        viewModelScope.launch {
            when (repo.markAsRead(notification.id)) {
                is UiState.Success<*> -> {
                    // Update local state
                    val updatedList = _uiState.value.notifications.map {
                        if (it.id == notification.id) it.copy(read = true) else it
                    }
                    val unread = updatedList.count { !it.read }
                    _uiState.value = _uiState.value.copy(
                        notifications = updatedList,
                        unreadCount = unread
                    )
                }
                else -> {} // Handle error silently or show message
            }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
<<<<<<< HEAD
            when (repo.deleteNotification(id)) {
=======
            when (repo.delete(id)) {
>>>>>>> origin/messaging-announcement
                is UiState.Success<*> -> {
                    val updatedList = _uiState.value.notifications.filter { it.id != id }
                    val unread = updatedList.count { !it.read }
                    _uiState.value = _uiState.value.copy(
                        notifications = updatedList,
                        unreadCount = unread
                    )
                }
                else -> {}
            }
        }
    }
}

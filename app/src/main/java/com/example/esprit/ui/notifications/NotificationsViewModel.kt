package com.example.esprit.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.notification.Notification
import com.example.esprit.repository.NotificationsRepository
import com.example.esprit.util.UiState
import com.example.esprit.util.DataStoreManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing notifications
 * Handles both real-time WebSocket notifications and API-based operations
 * Persists WebSocket notifications locally to survive app restarts
 */
@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: NotificationsRepository,
    private val systemNotificationManager: com.example.esprit.util.SystemNotificationManager,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val gson = Gson()
    
    // State for notifications list
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    // State for unread count
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    // State for banner notification (shows for 4 seconds)
    private val _showBanner = MutableStateFlow<Notification?>(null)
    val showBanner: StateFlow<Notification?> = _showBanner.asStateFlow()

    // Loading and error states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Socket connection state
    val isConnected: StateFlow<Boolean> = repository.isConnected

    init {
        // Listen to real-time notifications from WebSocket
        observeRealtimeNotifications()
        // Load locally stored WebSocket notifications on init
        loadLocalNotifications()
    }

    /**
     * Connect to notification socket
     */
    fun connectSocket(userId: String) {
        viewModelScope.launch {
            repository.connectSocket(userId)
        }
    }

    /**
     * Disconnect from notification socket
     */
    fun disconnectSocket() {
        repository.disconnectSocket()
    }

    /**
     * Load locally stored WebSocket notifications
     */
    private fun loadLocalNotifications() {
        viewModelScope.launch {
            val localNotificationsJson = dataStoreManager.getLocalNotifications()
            if (!localNotificationsJson.isNullOrEmpty()) {
                try {
                    val type = object : TypeToken<List<Notification>>() {}.type
                    val localNotifications: List<Notification> = gson.fromJson(localNotificationsJson, type)
                    _notifications.value = localNotifications
                } catch (e: Exception) {
                    // Failed to parse, ignore
                }
            }
        }
    }

    /**
     * Save notifications to local storage
     */
    private suspend fun saveLocalNotifications(notifications: List<Notification>) {
        try {
            val json = gson.toJson(notifications)
            dataStoreManager.saveLocalNotifications(json)
        } catch (e: Exception) {
            // Failed to save, ignore
        }
    }

    /**
     * Observe real-time notifications from WebSocket
     */
    private fun observeRealtimeNotifications() {
        viewModelScope.launch {
            repository.notifications.collect { notification ->
                // Add to beginning of list (avoid duplicates)
                val currentList = _notifications.value
                if (currentList.none { it.id == notification.id }) {
                    val updatedList = listOf(notification) + currentList
                    _notifications.value = updatedList
                    
                    // Save to local storage
                    saveLocalNotifications(updatedList)

                    // Increment unread count
                    _unreadCount.value += 1
                }

                // Show banner
                _showBanner.value = notification
                
                // Show system notification
                systemNotificationManager.showNotification(notification)

                // Auto-dismiss banner after 4 seconds
                kotlinx.coroutines.delay(4000)
                if (_showBanner.value?.id == notification.id) {
                    _showBanner.value = null
                }
            }
        }
    }

    /**
     * Fetch all notifications from API and merge with local WebSocket notifications
     */
    fun fetchNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = repository.getMyNotifications()) {
                is UiState.Success -> {
                    // Get API notifications
                    val apiNotifications = result.data
                    
                    // Get local WebSocket notifications
                    val localNotificationsJson = dataStoreManager.getLocalNotifications()
                    val localNotifications: List<Notification> = if (!localNotificationsJson.isNullOrEmpty()) {
                        try {
                            val type = object : TypeToken<List<Notification>>() {}.type
                            gson.fromJson<List<Notification>>(localNotificationsJson, type) ?: emptyList()
                        } catch (e: Exception) {
                            emptyList()
                        }
                    } else {
                        emptyList()
                    }
                    
                    // Merge: API notifications + local notifications (remove duplicates by ID)
                    val mergedNotifications = (apiNotifications + localNotifications)
                        .distinctBy { it.id }
                        .sortedByDescending { it.createdAt }
                    
                    _notifications.value = mergedNotifications
                    fetchUnreadCount()
                }
                is UiState.Error -> {
                    _error.value = result.message
                }
                else -> {}
            }

            _isLoading.value = false
        }
    }

    /**
     * Fetch unread count from API
     */
    private fun fetchUnreadCount() {
        viewModelScope.launch {
            when (val result = repository.getUnreadCount()) {
                is UiState.Success -> {
                    _unreadCount.value = result.data.count
                }
                is UiState.Error -> {
                    // Silently fail, not critical
                }
                else -> {}
            }
        }
    }

    /**
     * Mark notification as read
     */
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            when (val result = repository.markAsRead(notificationId)) {
                is UiState.Success -> {
                    // Update local state
                    val updatedList = _notifications.value.map {
                        if (it.id == notificationId) result.data else it
                    }
                    _notifications.value = updatedList
                    
                    // Update local storage
                    saveLocalNotifications(updatedList)
                    
                    // Decrement unread count if it was unread
                    val wasUnread = _notifications.value.find { it.id == notificationId }?.read == false
                    if (wasUnread && _unreadCount.value > 0) {
                        _unreadCount.value -= 1
                    }
                }
                is UiState.Error -> {
                    _error.value = result.message
                }
                else -> {}
            }
        }
    }

    /**
     * Delete notification
     */
    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            when (val result = repository.deleteNotification(notificationId)) {
                is UiState.Success -> {
                    // Remove from local state
                    val notification = _notifications.value.find { it.id == notificationId }
                    val updatedList = _notifications.value.filter { it.id != notificationId }
                    _notifications.value = updatedList
                    
                    // Update local storage
                    saveLocalNotifications(updatedList)
                    
                    // Decrement unread count if it was unread
                    if (notification?.read == false && _unreadCount.value > 0) {
                        _unreadCount.value -= 1
                    }
                }
                is UiState.Error -> {
                    _error.value = result.message
                }
                else -> {} // Handle Loading or other states if necessary
            }
        }
    }

    /**
     * Dismiss banner manually
     */
    fun dismissBanner() {
        _showBanner.value = null
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }
}

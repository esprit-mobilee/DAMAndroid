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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
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

    // Channel for throttling system notifications to prevent Binder crash
    private val systemNotificationChannel = Channel<Notification>(Channel.UNLIMITED)
    
    // Job for auto-dismissing banner
    private var bannerDismissJob: kotlinx.coroutines.Job? = null
    
    // Job for debouncing local storage saves
    private var saveJob: kotlinx.coroutines.Job? = null
    
    // Timestamp for banner throttling
    private var lastBannerTime = 0L

    init {
        // Listen to real-time notifications from WebSocket
        observeRealtimeNotifications()
        // Load locally stored WebSocket notifications on init
        loadLocalNotifications()
        // Start processing system notifications queue
        processSystemNotifications()
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
    /**
     * Load locally stored WebSocket notifications
     */
    private fun loadLocalNotifications() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
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
    }

    /**
     * Save notifications to local storage with debounce
     * Prevents excessive I/O when multiple notifications arrive rapidly
     */
    private fun saveLocalNotifications(notifications: List<Notification>) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch(Dispatchers.IO) {
            delay(1000) // Debounce for 1 second
            try {
                val json = gson.toJson(notifications)
                dataStoreManager.saveLocalNotifications(json)
            } catch (e: Exception) {
                // Failed to save, ignore
            }
        }
    }

    /**
     * Process system notifications sequentially with delay
     */
    private fun processSystemNotifications() {
        viewModelScope.launch(Dispatchers.IO) {
            for (notification in systemNotificationChannel) {
                // Throttle to prevent Binder transaction failure (crash)
                delay(200) 
                withContext(Dispatchers.Main) {
                    systemNotificationManager.showNotification(notification)
                }
            }
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
                val isNew = currentList.none { it.id == notification.id }
                
                if (isNew) {
                    val updatedList = listOf(notification) + currentList
                    _notifications.value = updatedList
                    
                    // Save to local storage (Debounced)
                    saveLocalNotifications(updatedList)

                    // Increment unread count
                    _unreadCount.value += 1
                    
                    // Queue system notification (Throttled via Channel)
                    systemNotificationChannel.trySend(notification)
                }

                // Show banner (Throttled)
                // Only show banner if enough time passed or if it's the only one
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastBannerTime > 2000 || _showBanner.value == null) {
                    lastBannerTime = currentTime
                    _showBanner.value = notification
                    
                    // Auto-dismiss banner after 4 seconds
                    bannerDismissJob?.cancel()
                    bannerDismissJob = viewModelScope.launch {
                        delay(4000)
                        if (_showBanner.value?.id == notification.id) {
                            _showBanner.value = null
                        }
                    }
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
                    // Perform heavy lifting (merging, sorting, JSON parsing) on IO thread
                    val mergedNotifications = withContext(Dispatchers.IO) {
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
                        (apiNotifications + localNotifications)
                            .distinctBy { it.id }
                            .sortedByDescending { it.createdAt }
                    }
                    
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
                    // _error.value = result.message <-- Do NOT show global error screen for this
                    // Just log it or show a snackbar (if we had a snackbar state)
                    android.util.Log.e("NotificationsViewModel", "Failed to mark as read: ${result.message}")
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
            // Optimistic update: Remove immediately from UI
            val currentList = _notifications.value
            val notificationToDelete = currentList.find { it.id == notificationId }
            val updatedList = currentList.filter { it.id != notificationId }
            _notifications.value = updatedList
            
            // Decrement unread count immediately if it was unread
            if (notificationToDelete?.read == false && _unreadCount.value > 0) {
                _unreadCount.value -= 1
            }

            // Perform API call
            val result = repository.deleteNotification(notificationId)
            
            if (result is UiState.Error) {
                // Restore state on failure
                android.util.Log.e("NotificationsVM", "Delete failed, restoring item: ${result.message}")
                fetchNotifications() 
            } else if (result is UiState.Success) {
                 saveLocalNotifications(updatedList)
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

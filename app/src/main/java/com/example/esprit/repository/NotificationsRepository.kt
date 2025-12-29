package com.example.esprit.repository

import com.example.esprit.model.notification.Notification
import com.example.esprit.model.notification.UnreadCountResponse
import com.example.esprit.network.ApiService
import com.example.esprit.network.NotificationSocketManager
import com.example.esprit.network.safeCall
import com.example.esprit.util.UiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing notifications
 * Handles both API calls and WebSocket real-time notifications
 */
@Singleton
class NotificationsRepository @Inject constructor(
    private val api: ApiService,
    private val socketManager: NotificationSocketManager
) {
    // WebSocket flows
    val notifications: Flow<Notification> = socketManager.notifications
    val isConnected: StateFlow<Boolean> = socketManager.isConnected
    val connectionError: StateFlow<String?> = socketManager.connectionError
    
    /**
     * Connect to notification socket
     */
    suspend fun connectSocket(userId: String) {
        socketManager.connect(userId)
    }
    
    /**
     * Disconnect from notification socket
     */
    fun disconnectSocket() {
        socketManager.disconnect()
    }
    
    /**
     * Check if socket is connected
     */
    fun isSocketConnected(): Boolean = socketManager.isSocketConnected()
    
    /**
     * Get all notifications for current user
     */
    suspend fun getMyNotifications(): UiState<List<Notification>> = safeCall {
        api.getMyNotifications()
    }
    
    /**
     * Get unread notification count for current user
     */
    suspend fun getUnreadCount(): UiState<UnreadCountResponse> = safeCall {
        api.getUnreadNotificationCount()
    }
    
    /**
     * Mark notification as read
     */
    suspend fun markAsRead(id: String): UiState<Notification> = safeCall {
        api.markNotificationAsRead(id)
    }
    
    /**
     * Delete notification
     */
    suspend fun deleteNotification(id: String): UiState<Map<String, String>> = safeCall {
        api.deleteNotification(id)
    }
    
    // Legacy club-specific methods (kept for backward compatibility)
    suspend fun getClubNotifications(clubId: String): UiState<List<Notification>> = safeCall {
        api.getClubNotifications(clubId)
    }
}

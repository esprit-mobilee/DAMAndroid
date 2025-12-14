package com.example.esprit.repository

import com.example.esprit.model.notification.NotificationDto
import com.example.esprit.network.ApiService
import com.example.esprit.network.safeCall
import com.example.esprit.util.UiState
import javax.inject.Inject

class NotificationsRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun getClubNotifications(clubId: String): UiState<List<NotificationDto>> = safeCall {
        api.getClubNotifications(clubId)
    }

    suspend fun getUnreadCount(clubId: String): UiState<Map<String, Int>> = safeCall {
        api.getUnreadCount(clubId)
    }

    suspend fun markAsRead(id: String): UiState<NotificationDto> = safeCall {
        api.markNotificationAsRead(id)
    }

    suspend fun delete(id: String): UiState<Map<String, String>> = safeCall {
        api.deleteNotification(id)
    }
}

package com.example.esprit.model.notification

import androidx.compose.ui.graphics.Color
import com.example.esprit.R
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

/**
 * Notification model supporting all types of real-time notifications
 */
data class Notification(
    @SerializedName("_id")
    val id: String,
    @SerializedName("type")
    val type: NotificationType? = null,
    val message: String,
    val read: Boolean,
    val createdAt: String,
    val updatedAt: String? = null,
    
    // User who triggered the notification (optional)
    // Using Any? because backend might return populated object or just ID string
    @SerializedName("userId")
    private val _userId: Any? = null,
    
    // Related entities (optional based on type)
    // Using Any? because backend might return populated object or just ID string
    @SerializedName("clubId")
    private val _clubId: Any? = null,
    
    @SerializedName("internshipOfferId")
    private val _internshipOfferId: Any? = null,
    
    @SerializedName("applicationId")
    private val _applicationId: Any? = null,
    
    @SerializedName("postId")
    private val _postId: Any? = null,
    
    @SerializedName("commentId")
    private val _commentId: Any? = null,
    
    @SerializedName("eventId")
    private val _eventId: Any? = null,

    @SerializedName("sender")
    private val _sender: Any? = null
) {
    // Helper to extract ID from String or Map
    private fun extractId(value: Any?): String? {
        return when (value) {
            is String -> value
            is Map<*, *> -> value["_id"] as? String
            else -> null
        }
    }

    val userId: String? get() = extractId(_userId)
    val clubId: String? get() = extractId(_clubId)
    val internshipOfferId: String? get() = extractId(_internshipOfferId)
    val applicationId: String? get() = extractId(_applicationId)
    val postId: String? get() = extractId(_postId)
    val commentId: String? get() = extractId(_commentId)
    val eventId: String? get() = extractId(_eventId)
    val senderId: String? get() = extractId(_sender)

    val safeType: NotificationType get() = type ?: NotificationType.NEW_MESSAGE

    val iconResId: Int get() = safeType.getIcon()
    val color: Color get() = safeType.getColor()
    val typeTitle: String get() = safeType.getTitle()

    /**
     * Calculate relative time (e.g., "Il y a 5 min")
     */
    fun getTimeAgo(): String {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            formatter.timeZone = TimeZone.getTimeZone("UTC")
            val date = formatter.parse(createdAt) ?: return "À l'instant"
            
            val diff = System.currentTimeMillis() - date.time
            when {
                diff < 60_000 -> "À l'instant"
                diff < 3600_000 -> "Il y a ${diff / 60_000} min"
                diff < 86400_000 -> "Il y a ${diff / 3600_000}h"
                else -> "Il y a ${diff / 86400_000}j"
            }
        } catch (e: Exception) {
            "À l'instant"
        }
    }
    
    /**
     * Get navigation route for this notification
     */
    fun getNavigationRoute(): String {
        return when (safeType) {
            NotificationType.INTERNSHIP_CREATED,
            NotificationType.INTERNSHIP_UPDATED,
            NotificationType.INTERNSHIP_DELETED -> 
                "internships/details/$internshipOfferId"
            
            NotificationType.APPLICATION_SUBMITTED -> 
                if (applicationId != null) "admin/applications/$applicationId" else "admin/applications"
                
            NotificationType.APPLICATION_ACCEPTED,
            NotificationType.APPLICATION_REJECTED -> 
                if (applicationId != null) "student/applications/$applicationId" else "student/applications"
            
            NotificationType.CLUB_POST_CREATED,
            NotificationType.POST_LIKED,
            NotificationType.POST_DISLIKED -> 
                if (postId != null) "post_detail/$postId" else "feed"
            
            NotificationType.COMMENT_LIKED,
            NotificationType.COMMENT_REPLIED,
            NotificationType.POST_COMMENTED -> 
                if (postId != null) {
                    if (commentId != null) "post_detail/$postId?commentId=$commentId"
                    else "post_detail/$postId"
                } else "feed"
            
            NotificationType.CLUB_EVENT_CREATED -> 
                if (eventId != null) "club/events/detail/$eventId" else "club/events"
            
            NotificationType.JOIN_REQUEST -> 
                "club/requests"
                
            NotificationType.EVENT_REGISTRATION -> 
                if (eventId != null) "club/events/detail/$eventId" else "club/events"

            NotificationType.PRIVATE_MESSAGE,
            NotificationType.NEW_MESSAGE ->
                if (senderId != null) "chat/private/$senderId?name=Chat" else "messages"

            NotificationType.CLUB_MESSAGE ->
                if (clubId != null) "club/chat/$clubId?name=Chat" else "messages"
        }
    }
}

/**
 * All supported notification types
 */
enum class NotificationType {
    @SerializedName("join_request")
    JOIN_REQUEST,
    
    @SerializedName("event_registration")
    EVENT_REGISTRATION,
    
    @SerializedName("internship_created")
    INTERNSHIP_CREATED,
    
    @SerializedName("internship_updated")
    INTERNSHIP_UPDATED,
    
    @SerializedName("internship_deleted")
    INTERNSHIP_DELETED,
    
    @SerializedName("application_submitted")
    APPLICATION_SUBMITTED,
    
    @SerializedName("application_accepted")
    APPLICATION_ACCEPTED,
    
    @SerializedName("application_rejected")
    APPLICATION_REJECTED,
    
    @SerializedName("club_post_created")
    CLUB_POST_CREATED,
    
    @SerializedName("club_event_created")
    CLUB_EVENT_CREATED,
    
    @SerializedName("comment_liked")
    COMMENT_LIKED,
    
    @SerializedName("comment_replied")
    COMMENT_REPLIED,
    
    @SerializedName("post_liked")
    POST_LIKED,

    @SerializedName("post_disliked")
    POST_DISLIKED,
    
    @SerializedName("post_commented")
    POST_COMMENTED,

    @SerializedName("private_message")
    PRIVATE_MESSAGE,

    @SerializedName("club_message")
    CLUB_MESSAGE,

    @SerializedName("new_message")
    NEW_MESSAGE;
    
    /**
     * Get icon resource for this notification type
     */
    fun getIcon(): Int {
        return when (this) {
            INTERNSHIP_CREATED, INTERNSHIP_UPDATED -> R.drawable.ic_work
            INTERNSHIP_DELETED -> R.drawable.ic_delete
            APPLICATION_SUBMITTED, APPLICATION_ACCEPTED, APPLICATION_REJECTED -> R.drawable.ic_description
            CLUB_POST_CREATED -> R.drawable.ic_article
            CLUB_EVENT_CREATED, EVENT_REGISTRATION -> R.drawable.ic_event
            COMMENT_LIKED, POST_LIKED -> R.drawable.ic_favorite
            POST_DISLIKED -> R.drawable.ic_favorite // You might want a broken heart or dislike icon
            COMMENT_REPLIED, POST_COMMENTED -> R.drawable.ic_comment
            JOIN_REQUEST -> R.drawable.ic_person_add
            PRIVATE_MESSAGE, CLUB_MESSAGE, NEW_MESSAGE -> R.drawable.ic_comment
        }
    }
    
    /**
     * Get color for this notification type
     */
    fun getColor(): Color {
        return when (this) {
            INTERNSHIP_CREATED -> Color(0xFF9C27B0) // Purple
            INTERNSHIP_UPDATED -> Color(0xFFFF9800) // Orange
            INTERNSHIP_DELETED -> Color(0xFFF44336) // Red
            APPLICATION_SUBMITTED -> Color(0xFF2196F3) // Blue
            APPLICATION_ACCEPTED -> Color(0xFF4CAF50) // Green
            APPLICATION_REJECTED -> Color(0xFFF44336) // Red
            CLUB_POST_CREATED -> Color(0xFF2196F3) // Blue
            CLUB_EVENT_CREATED, EVENT_REGISTRATION -> Color(0xFF00BCD4) // Cyan
            COMMENT_LIKED, POST_LIKED -> Color(0xFFE91E63) // Pink
            POST_DISLIKED -> Color(0xFF757575) // Grey for dislike
            COMMENT_REPLIED, POST_COMMENTED -> Color(0xFF00BCD4) // Cyan
            JOIN_REQUEST -> Color(0xFF673AB7) // Deep Purple
            PRIVATE_MESSAGE, NEW_MESSAGE -> Color(0xFF4CAF50) // Green
            CLUB_MESSAGE -> Color(0xFF2196F3) // Blue
        }
    }
    
    /**
     * Get display title for this notification type
     */
    fun getTitle(): String {
        return when (this) {
            JOIN_REQUEST -> "Demande d'adhésion"
            EVENT_REGISTRATION -> "Inscription événement"
            INTERNSHIP_CREATED -> "Nouvelle offre"
            INTERNSHIP_UPDATED -> "Offre mise à jour"
            INTERNSHIP_DELETED -> "Offre supprimée"
            APPLICATION_SUBMITTED -> "Nouvelle candidature"
            APPLICATION_ACCEPTED -> "Candidature acceptée"
            APPLICATION_REJECTED -> "Candidature refusée"
            CLUB_POST_CREATED -> "Nouvelle publication"
            CLUB_EVENT_CREATED -> "Nouvel événement"
            COMMENT_LIKED -> "Commentaire aimé"
            COMMENT_REPLIED -> "Réponse à votre commentaire"
            POST_LIKED -> "Publication aimée"
            POST_DISLIKED -> "Réaction négative"
            POST_COMMENTED -> "Nouveau commentaire"
            PRIVATE_MESSAGE, NEW_MESSAGE -> "Nouveau message"
            CLUB_MESSAGE -> "Nouveau message de club"
        }
    }
}

/**
 * Response for unread count endpoint
 */
data class UnreadCountResponse(
    val count: Int
)

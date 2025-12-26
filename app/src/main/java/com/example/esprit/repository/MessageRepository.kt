package com.example.esprit.repository

import com.example.esprit.model.*
import com.example.esprit.network.ApiService
import javax.inject.Inject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

class MessageRepository @Inject constructor(
    private val api: ApiService
) {

    suspend fun getUserConversations(userId: String): List<ConversationResponse> {
        println("📡 API CALL → GET /messages/conversations?userId=$userId")
        return api.getDirectConversations(userId)
    }

    suspend fun getConversationForUser(
        currentUserId: String,
        peerId: String
    ): List<Message> {
        val dtos = api.getDirectConversation(
            user1 = currentUserId,
            user2 = peerId
        )
        return dtos.map { it.toDomain(currentUserId) }
    }

    suspend fun sendMessageForUser(
        currentUserId: String,
        peerId: String,
        content: String
    ): Message {
        val body = SendMessageRequest(
            senderId = currentUserId,
            receiverId = peerId,
            content = content,
            type = "text"
        )
        return api.sendDirectMessage(body).toDomain(currentUserId)
    }

    suspend fun summarizeConversationForUser(
        currentUserId: String,
        peerId: String
    ): ChatSummaryResponse? {
        return api.summarizeDirectMessages(
            receiverId = currentUserId,
            senderId = peerId
        )
    }

    suspend fun summarizeAllMessages(
        currentUserId: String,
        peerId: String
    ): ChatSummaryResponse? {
        return api.summarizeAllDirectMessages(
            receiverId = currentUserId,
            senderId = peerId
        )
    }
}

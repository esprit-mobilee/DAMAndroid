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

    suspend fun getUserConversations(currentUserId: String): List<ConversationResponse> {
        return api.getConversations(currentUserId)
    }

    suspend fun getConversationForUser(
        currentUserId: String,
        peerId: String
    ): List<Message> {
        val dtos = api.getConversation(
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
        return api.sendMessage(body).toDomain(currentUserId)
    }

    suspend fun summarizeConversationForUser(
        currentUserId: String,
        peerId: String
    ): ChatSummaryResponse? {
        return api.summarizeMessages(
            receiverId = currentUserId,
            senderId = peerId
        )
    }
    suspend fun reactToMessage(
        messageId: String,
        userId: String,
        emoji: String
    ): Message {
        val body = mapOf(
            "userId" to userId,
            "emoji" to emoji
        )

        val dto = api.reactToMessage(messageId, body)
        return dto.toDomain(userId)
    }


    suspend fun uploadFile(file: java.io.File, mimeType: String): UploadResponse {
        val requestBody = file.asRequestBody(mimeType.toMediaType())
        val multipart = MultipartBody.Part.createFormData("file", file.name, requestBody)
        return api.uploadMessageFile(multipart)
    }
    suspend fun summarizeAllMessages(
        currentUserId: String,
        peerId: String
    ): ChatSummaryResponse? {
        return api.summarizeAll(
            receiverId = currentUserId,
            senderId = peerId
        )
    }



}

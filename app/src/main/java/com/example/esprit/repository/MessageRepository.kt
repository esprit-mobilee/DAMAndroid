package com.example.esprit.repository

import com.example.esprit.model.*
import com.example.esprit.network.ApiService
import javax.inject.Inject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

private const val CURRENT_USER_ID = "691e24db7a1a6b2eb5bc6617" // TODO: remplacer DataStore

class MessageRepository @Inject constructor(
    private val api: ApiService
) {

    // -------------------------------------------
    // LISTE DES CONVERSATIONS
    // -------------------------------------------
    suspend fun getUserConversations(): List<ConversationResponse> {
        return api.getConversations(CURRENT_USER_ID)
    }

    // -------------------------------------------
    // GET CONVERSATION
    // -------------------------------------------
    suspend fun getConversation(peerId: String): List<Message> {
        val dtos = api.getConversation(CURRENT_USER_ID, peerId)
        return dtos.map { it.toDomain(CURRENT_USER_ID) }
    }

    // -------------------------------------------
    // SEND MESSAGE
    // -------------------------------------------
    suspend fun sendMessage(peerId: String, content: String): Message {
        val body = SendMessageRequest(
            senderId = CURRENT_USER_ID,
            receiverId = peerId,
            content = content,
            type = "text"
        )

        val dto = api.sendMessage(body)
        return dto.toDomain(CURRENT_USER_ID)
    }
    // -------------------------------------------
    // UPLOAD FILE (audio / image / pdf)
    // -------------------------------------------
    suspend fun uploadFile(file: java.io.File, mimeType: String): UploadResponse {

        val requestFile = file
            .asRequestBody(mimeType.toMediaType())

        val multipart = MultipartBody.Part.createFormData(
            name = "file",
            filename = file.name,
            body = requestFile
        )

        return api.uploadMessageFile(multipart)
    }

}

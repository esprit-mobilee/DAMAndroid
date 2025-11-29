package com.example.esprit.repository

import com.example.esprit.model.Message
import com.example.esprit.model.SendMessageRequest
import com.example.esprit.model.toDomain
import com.example.esprit.network.ApiService
import javax.inject.Inject

// ⚠️ ID FIXE TEMPORAIRE – tu peux plus tard utiliser DataStore pour getMe.user.id
private const val CURRENT_USER_ID = "691e2126d4558f41c78b085a"

class MessageRepository @Inject constructor(
    private val api: ApiService
) {

    // -------------------------------------------------------------
    // GET CONVERSATION
    // -------------------------------------------------------------
    suspend fun getConversation(peerId: String): List<Message> {
        val dtos = api.getConversation(
            CURRENT_USER_ID,
            peerId
        )
        return dtos.map { it.toDomain(CURRENT_USER_ID) }
    }

    // -------------------------------------------------------------
    // SEND MESSAGE
    // -------------------------------------------------------------
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
}

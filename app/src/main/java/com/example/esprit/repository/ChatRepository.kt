package com.example.esprit.repository

import com.example.esprit.model.chat.ConversationDto
import com.example.esprit.model.chat.MessageDto
import com.example.esprit.network.ApiService
import com.example.esprit.network.SocketManager
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val apiService: ApiService,
    private val socketManager: SocketManager
) {
    val incomingMessages = socketManager.messages
    val typingEvents = socketManager.typingEvents
    val messageUpdates = socketManager.messageUpdates
    val messageDeletions = socketManager.messageDeletions
    val readReceipts = socketManager.readReceipts
    
    // Local flow for sent messages to update UI instantly across ViewModels
    private val _sentMessages = kotlinx.coroutines.flow.MutableSharedFlow<MessageDto>()
    val sentMessages = _sentMessages.asSharedFlow()

    suspend fun emitSentMessage(message: MessageDto) {
        _sentMessages.emit(message)
    }

    fun connect(userId: String) { 
        socketManager.connect(userId)
    }

    fun disconnect() {
        socketManager.disconnect()
    }

    fun joinRoom(clubId: String) {
        socketManager.joinRoom(clubId)
    }

    fun leaveRoom(clubId: String) {
        socketManager.leaveRoom(clubId)
    }

    fun sendMessage(clubId: String?, senderId: String, content: String, type: String = "TEXT", attachmentUrl: String? = null, replyTo: String? = null, recipientId: String? = null) {
        socketManager.sendMessage(clubId, senderId, content, type, attachmentUrl, replyTo, recipientId)
    }

    fun sendTyping(clubId: String, userId: String, isTyping: Boolean) {
        socketManager.sendTyping(clubId, userId, isTyping)
    }

    fun editMessage(messageId: String, userId: String, content: String) {
        socketManager.editMessage(messageId, userId, content)
    }

    fun deleteMessage(messageId: String, userId: String) {
        socketManager.deleteMessage(messageId, userId)
    }

    fun addReaction(messageId: String, userId: String, emoji: String) {
        socketManager.addReaction(messageId, userId, emoji)
    }
    
    fun markAsRead(messageId: String, userId: String, clubId: String) {
        socketManager.markAsRead(messageId, userId, clubId)
    }

    suspend fun uploadFile(file: okhttp3.MultipartBody.Part): Result<String> {
        return try {
            val response = apiService.uploadFile(file)
            val url = response["url"] ?: throw Exception("No URL returned")
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHistory(clubId: String, limit: Int = 50, before: String? = null): Result<List<MessageDto>> {
        return try {
            val messages = apiService.getChatHistory(clubId, limit, before)
            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getConversations(userId: String): Result<List<ConversationDto>> {
        return try {
            val conversations = apiService.getConversations(userId)
            Result.success(conversations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPrivateHistory(userId: String, partnerId: String, limit: Int = 50, before: String? = null): Result<List<MessageDto>> {
        return try {
            val messages = apiService.getPrivateHistory(userId, partnerId, limit, before)
            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun translateMessage(messageId: String, targetLang: String): Result<com.example.esprit.model.chat.TranslationResponse> {
        return try {
            val req = com.example.esprit.model.chat.TranslateRequest(messageId, targetLang)
            val res = apiService.translateMessage(req)
            Result.success(res)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUnifiedConversations(userId: String): Result<List<com.example.esprit.model.chat.UnifiedConversationDto>> {
        return try {
            val list = apiService.getUnifiedConversations(userId)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getContacts(userId: String): Result<List<com.example.esprit.model.User>> {
        return try {
            val list = apiService.getContacts(userId)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

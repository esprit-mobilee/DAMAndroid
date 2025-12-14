package com.example.esprit.ui.chat

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.User
import com.example.esprit.model.chat.MessageDto
import com.example.esprit.network.ApiService
import com.example.esprit.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import com.example.esprit.model.chat.ReplyDto
import com.google.gson.Gson

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val apiService: ApiService,
    private val chatRepository: ChatRepository,
    private val studentRepository: com.example.esprit.repository.StudentRepository
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _messages = MutableStateFlow<List<MessageDto>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    // Error state for loading messages
    private val _loadError = MutableStateFlow<String?>(null)
    val loadError = _loadError.asStateFlow()
    
    // Club Members for Group Chats
    private val _clubMembers = MutableStateFlow<List<com.example.esprit.model.club.ClubMemberDto>>(emptyList())
    val clubMembers = _clubMembers.asStateFlow()
    
    // Typing status for UI (from other users) - Placeholder for now
    private val _isTyping = MutableStateFlow(false)
    val isTyping = _isTyping.asStateFlow()

    // Translations: Map<MessageID, TranslatedText>
    private val _translations = MutableStateFlow<Map<String, String>>(emptyMap())
    val translations = _translations.asStateFlow()

    private var currentClubId: String? = null
    private var currentPartnerId: String? = null

    init {
        fetchCurrentUser()
        viewModelScope.launch {
            launch {
                chatRepository.incomingMessages.collectLatest { msg ->
                    // Only append if it belongs to current context
                    val belongsToContext = (currentClubId != null && msg.clubId == currentClubId) ||
                                           (currentPartnerId != null && (msg.senderId.id == currentPartnerId || msg.recipientId == currentPartnerId))
                    
                    if (belongsToContext) {
                        val params = _messages.value.toMutableList()
                        val existingIndex = params.indexOfFirst { it.id == msg.id }
                        
                        if (existingIndex != -1) {
                            // Message with this ID already exists, update it
                            params[existingIndex] = msg
                            _messages.value = params
                        } else {
                            // New message - check if we should remove a temp version
                            // ONLY remove temp if: (1) message is from me, (2) there ARE temp messages
                            if (msg.senderId.id == (_currentUser.value?.id)) {
                                val hasTempMessages = params.any { it.id.startsWith("temp_") }
                                if (hasTempMessages) {
                                    val tempMessage = params.find { 
                                        it.id.startsWith("temp_") && 
                                        it.content == msg.content &&
                                        it.type == msg.type 
                                    }
                                    if (tempMessage != null) {
                                        params.remove(tempMessage)
                                    }
                                }
                            }
                            params.add(msg)
                            _messages.value = params
                        }
                    }
                }
            }
            launch {
                chatRepository.messageUpdates.collectLatest { updatedMsg ->
                    _messages.value = _messages.value.map { if (it.id == updatedMsg.id) updatedMsg else it }
                }
            }
            launch {
                chatRepository.messageDeletions.collectLatest { deletedId ->
                    _messages.value = _messages.value.filter { it.id != deletedId }
                }
            }
        }
    }

    private fun fetchCurrentUser() {
        viewModelScope.launch {
            try {
                val user = apiService.getMe()
                _currentUser.value = user
                chatRepository.connect(user.id) 
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadMessages(clubId: String?, partnerId: String?) {
        android.util.Log.d("ChatViewModel", "loadMessages: clubId=$clubId, partnerId=$partnerId")
        android.util.Log.d("ChatViewModel", "Current state: currentClubId=$currentClubId, currentPartnerId=$currentPartnerId")
        
        // Check if switching to a different chat BEFORE updating current IDs
        val isSwitchingChat = (clubId != currentClubId) || (partnerId != currentPartnerId)
        android.util.Log.d("ChatViewModel", "isSwitchingChat=$isSwitchingChat")
        
        // Update current IDs AFTER checking
        this.currentClubId = clubId
        this.currentPartnerId = partnerId
        
        viewModelScope.launch {
            _isLoading.value = true
            _loadError.value = null
            
            // Only clear messages when switching to a different chat
            if (isSwitchingChat) {
                android.util.Log.d("ChatViewModel", "Clearing messages (switching chat)")
                _messages.value = emptyList()
                _clubMembers.value = emptyList()
            } else {
                android.util.Log.d("ChatViewModel", "Keeping existing messages (same chat)")
            }
            
            try {
                val result = if (clubId != null) {
                    chatRepository.joinRoom(clubId)
                    // Load Members
                    loadClubMembers(clubId)
                    chatRepository.getHistory(clubId)
                } else if (partnerId != null) {
                    val user = _currentUser.value ?: apiService.getMe().also { _currentUser.value = it }
                    chatRepository.getPrivateHistory(user.id, partnerId)
                } else {
                    Result.success(emptyList())
                }
                
                if (result.isSuccess) {
                    val msgs = result.getOrDefault(emptyList())
                    android.util.Log.d("ChatViewModel", "loadMessages success: ${msgs.size} messages")
                    if (msgs.isNotEmpty()) {
                        android.util.Log.d("ChatViewModel", "First message: ${msgs[0].content}")
                    }
                    _messages.value = msgs.sortedBy { it.createdAt }
                    _loadError.value = null
                } else {
                    val error = result.exceptionOrNull()
                    android.util.Log.e("ChatViewModel", "Error fetching messages", error)
                    _loadError.value = error?.message ?: "Failed to load messages"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("ChatViewModel", "loadMessages exception", e)
                _loadError.value = e.message ?: "An error occurred while loading messages"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private suspend fun loadClubMembers(clubId: String) {
        try {
            android.util.Log.d("ChatViewModel", "loadClubMembers for clubId: $clubId")
            val members = apiService.getClubMembers(clubId)
            android.util.Log.d("ChatViewModel", "loadClubMembers success: ${members.size} members")
            _clubMembers.value = members
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("ChatViewModel", "loadClubMembers error", e)
            _clubMembers.value = emptyList()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendMessage(content: String, type: String = "TEXT", replyTo: MessageDto? = null) {
        val user = _currentUser.value ?: return
        
        // Sanitize content
        val sanitizedContent = com.example.esprit.util.BadWordFilter.sanitize(content)
        
        android.util.Log.d("ChatViewModel", "sendMessage: type=$type, content=$sanitizedContent (original: $content)")
        
        // Optimistic UI Update
        val tempId = "temp_${System.currentTimeMillis()}"
        val optimisticMessage = MessageDto(
            id = tempId,
            clubId = currentClubId,
            recipientId = currentPartnerId,
            senderId = com.example.esprit.model.chat.ChatUserDto(
                id = user.id,
                firstName = user.firstName ?: "",
                lastName = user.lastName ?: "",
                imageUrl = null // User model missing image field
            ),
            content = sanitizedContent,
            type = type,
            createdAt = java.time.Instant.now().toString(),

            replyTo = replyTo?.let {
                ReplyDto(
                    id = it.id,
                    content = it.content,
                    type = it.type,
                    senderId = Gson().toJsonTree(it.senderId)
                )
            }
        )
        _messages.value = _messages.value + optimisticMessage
        
        // Validated ReplyTo
        val safeReplyTo = if (replyTo?.id?.startsWith("temp_") == true) null else replyTo?.id
        
        chatRepository.sendMessage(
            clubId = currentClubId,
            senderId = user.id,
            content = sanitizedContent,
            type = type,
            recipientId = currentPartnerId,
            replyTo = safeReplyTo
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendVoice(file: File, replyTo: MessageDto?) {
        android.util.Log.d("ChatViewModel", "sendVoice: file=${file.path}")
        uploadFile(file) { url ->
            android.util.Log.d("ChatViewModel", "sendVoice uploaded: url=$url")
            if (url != null) {
                val user = _currentUser.value ?: return@uploadFile
                
                // Optimistic Update for Voice
                val tempId = "temp_${System.currentTimeMillis()}"
                 val optimisticMessage = MessageDto(
                    id = tempId,
                    clubId = currentClubId,
                    recipientId = currentPartnerId,
                    senderId = com.example.esprit.model.chat.ChatUserDto(
                        id = user.id,
                        firstName = user.firstName ?: "",
                        lastName = user.lastName ?: "",
                        imageUrl = null
                    ),
                    content = "Sent a voice message",
                    type = "VOICE",
                    attachmentUrl = url, // Local file? Use url for now
                    createdAt = java.time.Instant.now().toString(),

                    replyTo = replyTo?.let {
                        ReplyDto(
                            id = it.id,
                            content = it.content,
                            type = it.type,
                            senderId = Gson().toJsonTree(it.senderId)
                        )
                    }
                )
                _messages.value = _messages.value + optimisticMessage
                
                val safeReplyTo = if (replyTo?.id?.startsWith("temp_") == true) null else replyTo?.id
                chatRepository.sendMessage(
                    clubId = currentClubId,
                    senderId = user.id,
                    content = "Sent a voice message",
                    type = "VOICE",
                    attachmentUrl = url,
                    replyTo = safeReplyTo,
                    recipientId = currentPartnerId
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendImage(file: File, replyTo: MessageDto?) {
        android.util.Log.d("ChatViewModel", "sendImage: file=${file.path}")
        uploadFile(file) { url ->
            android.util.Log.d("ChatViewModel", "sendImage uploaded: url=$url")
            if (url != null) {
                 val user = _currentUser.value ?: return@uploadFile
                 
                 // Optimistic Update for Image
                 val tempId = "temp_${System.currentTimeMillis()}"
                 val optimisticMessage = MessageDto(
                    id = tempId,
                    clubId = currentClubId,
                    recipientId = currentPartnerId,
                    senderId = com.example.esprit.model.chat.ChatUserDto(
                        id = user.id,
                        firstName = user.firstName ?: "",
                        lastName = user.lastName ?: "",
                        imageUrl = null
                    ),
                    content = "Sent an image",
                    type = "IMAGE",
                    attachmentUrl = url,
                    createdAt = java.time.Instant.now().toString(),

                    replyTo = replyTo?.let {
                        ReplyDto(
                            id = it.id,
                            content = it.content,
                            type = it.type,
                            senderId = Gson().toJsonTree(it.senderId)
                        )
                    }
                )
                _messages.value = _messages.value + optimisticMessage
                 
                 val safeReplyTo = if (replyTo?.id?.startsWith("temp_") == true) null else replyTo?.id
                 chatRepository.sendMessage(
                    clubId = currentClubId,
                    senderId = user.id,
                    content = "Sent an image",
                    type = "IMAGE",
                    attachmentUrl = url,
                    replyTo = safeReplyTo,
                    recipientId = currentPartnerId
                )
            }
        }
    }
    
    fun editMessage(messageId: String, content: String) {
        val user = _currentUser.value ?: return
        chatRepository.editMessage(messageId, user.id, content)
    }
    
    fun deleteMessage(messageId: String) {
        // If it's a temp message, just remove it locally and don't bother the server (it doesn't have it)
        if (messageId.startsWith("temp_")) {
            _messages.value = _messages.value.filter { it.id != messageId }
            return
        }
        
        val user = _currentUser.value ?: return
        chatRepository.deleteMessage(messageId, user.id)
    }
    
    fun addReaction(messageId: String, emoji: String) {
         val user = _currentUser.value ?: return
         chatRepository.addReaction(messageId, user.id, emoji)
    }

    private fun uploadFile(file: File, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                val body = okhttp3.MultipartBody.Part.createFormData("file", file.name, requestFile)
                val result = chatRepository.uploadFile(body)
                onResult(result.getOrNull())
            } catch(e: Exception) {
                e.printStackTrace()
                onResult(null)
            }
        }
    }

    fun sendTyping(isTyping: Boolean) {
        val user = _currentUser.value ?: return
        if (currentClubId != null) {
             chatRepository.sendTyping(currentClubId!!, user.id, isTyping)
        }
        // Private chat typing not implemented yet in Repo but handled loosely
    }

    fun translateMessage(messageId: String, targetLang: String = "fr") {
        viewModelScope.launch {
            val result = chatRepository.translateMessage(messageId, targetLang)
            if (result.isSuccess) {
                val res = result.getOrNull()
                if (res != null) {
                    val currentMap = _translations.value.toMutableMap()
                    currentMap[messageId] = res.translated
                    _translations.value = currentMap
                }
            } else {
                android.util.Log.e("ChatViewModel", "Translation failed", result.exceptionOrNull())
            }
        }
    }
}

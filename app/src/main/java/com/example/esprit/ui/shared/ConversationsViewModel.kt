package com.example.esprit.ui.shared

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.ConversationResponse
import com.example.esprit.model.chat.ConversationDto
import com.example.esprit.model.club.ClubHomeDto
import com.example.esprit.repository.ChatRepository
import com.example.esprit.repository.MessageRepository
import com.example.esprit.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

sealed class UnifiedConversation {
    data class Direct(val data: ConversationResponse) : UnifiedConversation()
    data class Club(val data: ClubHomeDto) : UnifiedConversation()
}

@HiltViewModel
class ConversationsViewModel @Inject constructor(
    private val msgRepo: MessageRepository,
    private val studentRepo: StudentRepository,
    private val chatRepository: ChatRepository,
    private val dataStore: com.example.esprit.util.DataStoreManager
) : ViewModel() {

    var items = mutableStateListOf<UnifiedConversation>()
        private set

    var loading = mutableStateOf(true)

    init {
        // Listen for real-time messages to update the list
        viewModelScope.launch {
            chatRepository.incomingMessages.collect { msg ->
                // Refresh list when a new message arrives
                // We need userId to reload. We can get it from DataStore or keep a reference if load() was called.
                // For now, checks if we have a current userId loaded.
                val userId = dataStore.userIdFlow.firstOrNull()
                if (!userId.isNullOrBlank()) {
                    load(userId)
                }
            }
        }
    }

    fun load(userId: String) {
        println("🔥 [VM] load() with userId = $userId")
        loading.value = true

        viewModelScope.launch {
            try {
                // Ensure socket connection for this user so we receive events
                chatRepository.connect(userId)

                coroutineScope {
                    // 1. Fetch Direct Messages
                    val directDeferred = async {
                        try {
                            println("🔄 fetching conversations via ChatRepository...")
                            val result = chatRepository.getConversations(userId)
                            println("✅ Fetch result success: ${result.isSuccess}")

                            result.getOrNull()?.mapNotNull { dto ->
                                try {
                                    val partnerElem = dto.partner
                                    var pName = "Utilisateur"
                                    var pRole = ""

                                    if (partnerElem is com.google.gson.JsonObject) {
                                        val obj = partnerElem
                                        val fn = if (obj.has("firstName")) obj.get("firstName").asString else ""
                                        val ln = if (obj.has("lastName")) obj.get("lastName").asString else ""
                                        pName = "$fn $ln".trim()
                                        if (pName.isEmpty()) pName = "Utilisateur"

                                        pRole = if (obj.has("role")) obj.get("role").asString else "STUDENT"
                                    } else if (partnerElem is com.google.gson.JsonPrimitive) {
                                        // It's just an ID string
                                        pName = "Utilisateur (${partnerElem.asString.take(4)}...)"
                                    }

                                    ConversationResponse(
                                        userId = dto.partnerId,
                                        fullName = pName,
                                        role = pRole,
                                        lastMessage = dto.lastMessage.content,
                                        lastMessageTime = dto.lastMessage.createdAt
                                    )
                                } catch (e: Exception) {
                                    println("⚠️ Error mapping conversation: ${e.message}")
                                    null
                                }
                            } ?: emptyList()
                        } catch (e: Exception) {
                            println("❌ Error fetching DMs: ${e.message}")
                            e.printStackTrace()
                            emptyList<ConversationResponse>()
                        }
                    }

                    // 2. Fetch Clubs
                    val clubsDeferred = async {
                        try {
                            // We fetch all clubs and filter manually for now since there's no "my-clubs" endpoint yet
                            val result = studentRepo.getAllClubs()
                            if (result is com.example.esprit.util.UiState.Success) {
                                (result.data as? List<ClubHomeDto>)?.filter {
                                    it.membershipStatus == "MEMBER" || it.membershipStatus == "PRESIDENT" || it.membershipStatus == "ADMIN"
                                } ?: emptyList()
                            } else {
                                emptyList()
                            }
                        } catch (e: Exception) {
                            emptyList<ClubHomeDto>()
                        }
                    }

                    val directList = directDeferred.await()
                    val clubList = clubsDeferred.await()

                    println("📩 DMs: ${directList.size}, Clubs: ${clubList.size}")

                    items.clear()
                    
                    // Add Clubs first (Group Chats)
                    items.addAll(clubList.map { UnifiedConversation.Club(it) })
                    
                    // Add Direct Messages
                    items.addAll(directList.map { UnifiedConversation.Direct(it) })
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                loading.value = false
            }
        }
    }
}

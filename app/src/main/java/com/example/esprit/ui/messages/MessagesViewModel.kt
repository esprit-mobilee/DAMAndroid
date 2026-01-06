package com.example.esprit.ui.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.chat.ConversationDto
import com.example.esprit.repository.ChatRepository
import com.example.esprit.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import javax.inject.Inject

// UI Model for MessagesScreen to avoid JsonElement handling
data class ConversationUi(
    val partnerId: String,
    val partner: com.example.esprit.model.User,

    val lastMessage: com.example.esprit.model.chat.MessageDto,
    val unreadCount: Int = 0
)

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val studentRepository: com.example.esprit.repository.StudentRepository
) : ViewModel() {

    private val _conversations = MutableStateFlow<List<ConversationUi>>(emptyList())
    val conversations = _conversations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        // Listen for real-time messages to update list order/content
        viewModelScope.launch {
             chatRepository.incomingMessages.collect { msg ->
                 // 1. Identify Conversation Key (Club ID or Partner ID)
                 val isClub = msg.clubId != null
                 val conversationId = if (isClub) msg.clubId!! else {
                     // For DM: if I am sender, partner is recipient. If I am recipient, partner is sender.
                     // But wait, I don't know "my" ID easily here without state.
                     // Actually, msg.senderId is ChatUserDto.
                     // We can try to find the conversation by matching against existing list.
                     
                     // Heuristic: Check if conversation exists with senderId or recipientId
                     // But simpler:
                     // If msg.recipientId is ME, proper partner is Sender.
                     // If msg.senderId is ME, proper partner is Recipient.
                     // Since I don't have "ME" handy in this scope easily without fetching,
                     // let's try to find it in the current list.
                     val list = _conversations.value
                     val foundStart = list.find { it.partnerId == msg.senderId.id || it.partnerId == msg.recipientId }
                     foundStart?.partnerId
                 }
                 
                 if (conversationId != null) {
                     val currentList = _conversations.value.toMutableList()
                     val index = currentList.indexOfFirst { it.partnerId == conversationId }
                     
                     if (index != -1) {
                         // Found! Move to top with updated message
                         val existing = currentList[index]
                         
                         // Logic for unread count:
                         // If I am the recipient (msg.recipientId == ME), then it's an unread message for me.
                         // But I don't have "ME" easily.
                         // However, if the message sender is the PARTNER, then it is incoming -> Unread + 1
                         val isFromPartner = msg.senderId.id == existing.partnerId
                         val newUnreadCount = if (isFromPartner) existing.unreadCount + 1 else existing.unreadCount
                         
                         val updated = existing.copy(
                             lastMessage = msg,
                             unreadCount = newUnreadCount
                         )
                         currentList.removeAt(index)
                         currentList.add(0, updated)
                         _conversations.value = currentList.toList()
                     } else {
                         // Not found in list (new conversation?), reload to be safe
                         loadConversations()
                     }
                 } else {
                     // Could not identify, reload
                     loadConversations()
                      }
        }
        
        // Listen for locally sent messages to update list order instantly
        viewModelScope.launch {
            chatRepository.sentMessages.collect { msg ->
                 // 1. Identify Conversation Key (Club ID or Partner ID)
                 val isClub = msg.clubId != null
                 val conversationId = if (isClub) msg.clubId!! else {
                     val list = _conversations.value
                     // For sent messages: sender is ME, recipient is PARTNER
                     // Logic handles both anyway
                     val foundStart = list.find { it.partnerId == msg.senderId.id || it.partnerId == msg.recipientId }
                     foundStart?.partnerId
                 }
                 
                 if (conversationId != null) {
                     val currentList = _conversations.value.toMutableList()
                     val index = currentList.indexOfFirst { it.partnerId == conversationId }
                     
                     if (index != -1) {
                         // Found! Move to top with updated message
                         val existing = currentList[index]
                         // For sent messages, reset unread count? NO, unread count is messages I HAVEN'T read. 
                         // Sent messages don't change unread count (or maybe they verify I read everything else?)
                         // Usually sending a message implies I've read the chat? 
                         // Ideally yes, but let's just update content for now.
                         val updated = existing.copy(lastMessage = msg)
                         currentList.removeAt(index)
                         currentList.add(0, updated)
                         _conversations.value = currentList.toList()
                     }
                 }
            }
        }
             }
        }


    fun loadConversations() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val user = userRepository.getMe()
                val userId = user.id ?: throw Exception("User ID is null") // Safe unwrap
                
                // Ensure socket is connected to receive real-time updates while in the list
                chatRepository.connect(userId)
                
                android.util.Log.d("MessagesViewModel", "loadConversations: user=${userId}")
                
                 kotlinx.coroutines.coroutineScope {
                    // 1. Fetch Direct Messages
                    val directDeferred = async {
                        try {
                           val res = chatRepository.getConversations(userId)
                           res.getOrNull() ?: emptyList()
                        } catch (e: Exception) {
                            android.util.Log.e("MessagesViewModel", "Error fetching DMs", e)
                            emptyList<com.example.esprit.model.chat.ConversationDto>()
                        }
                    }
                    
                    // 2. Fetch Clubs (Group Chats)
                    val clubsDeferred = async {
                        try {
                            val res = studentRepository.getAllClubs()
                            if (res is com.example.esprit.util.UiState.Success) {
                                res.data.filter { 
                                    it.membershipStatus == "MEMBER" || it.membershipStatus == "PRESIDENT" || it.membershipStatus == "ADMIN"
                                }
                            } else emptyList()
                        } catch (e: Exception) {
                            emptyList<com.example.esprit.model.club.ClubHomeDto>()
                        }
                    }

                    val directList = directDeferred.await()
                    val clubList = clubsDeferred.await()
                    
                    val combined = mutableListOf<ConversationUi>()
                    
                    // Map Clubs
                    combined.addAll(clubList.map { club ->
                        ConversationUi(
                            partnerId = club.id,
                            partner = com.example.esprit.model.User(
                                id = club.id,
                                name = club.name,
                                role = "CLUB",
                                profileImageUrl = club.imageUrl, 
                                email = "Discussion de groupe"
                            ),
                            lastMessage = com.example.esprit.model.chat.MessageDto(
                                id = "summary_${club.id}",
                                senderId = com.example.esprit.model.chat.ChatUserDto(
                                    id = "system",
                                    firstName = "",
                                    lastName = ""
                                ),
                                content = club.latestPost?.content ?: "Discussion de groupe", 
                                type = "TEXT",
                                createdAt = club.latestPost?.createdAt ?: "" 

                            ),
                            unreadCount = 0 // Clubs don't have unread count yet
                        )
                    })
                    
                    // Map DMs
                    combined.addAll(directList.mapNotNull { dto ->
                         try {
                            android.util.Log.d("MessagesViewModel", "Processing DM with partnerId: ${dto.partnerId}")
                            val partnerElem = dto.partner
                            var pName = "Utilisateur"
                            var pRole = ""
                            var pFirstName: String? = null
                            var pLastName: String? = null

                            if (partnerElem != null) {
                                // Use explicit methods to match ConversationsViewModel and avoid property access issues
                                if (partnerElem.isJsonObject) {
                                    val obj = partnerElem.asJsonObject
                                    val fn = if (obj.has("firstName") && !obj.get("firstName").isJsonNull) obj.get("firstName").asString else ""
                                    val ln = if (obj.has("lastName") && !obj.get("lastName").isJsonNull) obj.get("lastName").asString else ""
                                    pFirstName = fn
                                    pLastName = ln
                                    pName = "$fn $ln".trim()
                                    if (pName.isEmpty()) pName = "Utilisateur"

                                    pRole = if (obj.has("role") && !obj.get("role").isJsonNull) obj.get("role").asString else "STUDENT"
                                } else if (partnerElem.isJsonPrimitive) {
                                    // It's just an ID string
                                    pName = "Utilisateur (${partnerElem.asString.take(4)}...)"
                                } else {
                                     android.util.Log.w("MessagesViewModel", "Partner element is neither object nor primitive: $partnerElem")
                                }
                            } else {
                                // Partner is null, fallback
                                android.util.Log.w("MessagesViewModel", "Partner element is null for DM: ${dto.partnerId}")
                                pName = "Utilisateur Inconnu"
                            }

                            ConversationUi(
                                partnerId = dto.partnerId,
                                partner = com.example.esprit.model.User(
                                    id = dto.partnerId,
                                    name = pName,
                                    firstName = pFirstName,
                                    lastName = pLastName,
                                    role = pRole,
                                    isOnline = false // TODO: Realtime presence
                                ),
                                lastMessage = com.example.esprit.model.chat.MessageDto(
                                    id = "placeholder_${dto.partnerId}", 
                                    content = dto.lastMessage.content,
                                    type = dto.lastMessage.type,
                                    createdAt = dto.lastMessage.createdAt,
                                    senderId = com.example.esprit.model.chat.ChatUserDto(
                                        id = if (dto.lastMessage.senderId?.isJsonPrimitive == true) dto.lastMessage.senderId.asString else "unknown",
                                        firstName = "",
                                        lastName = ""
                                    )
                                ),
                                unreadCount = dto.unreadCount
                            )
                        } catch (e: Exception) {
                            android.util.Log.e("MessagesViewModel", "Error mapping conversation ${dto.partnerId}: ${e.message}")
                            e.printStackTrace()
                            // Return a fallback instead of null if possible, or null to skip
                            // Let's try to return a partial item to ensure it's visible
                            ConversationUi(
                                partnerId = dto.partnerId,
                                partner = com.example.esprit.model.User(
                                    id = dto.partnerId,
                                    name = "Erreur Chargement",
                                    role = "STUDENT"
                                ),
                                lastMessage = com.example.esprit.model.chat.MessageDto(
                                    id = "error",
                                    content = "Message unavailable",
                                    createdAt = "",
                                    senderId = com.example.esprit.model.chat.ChatUserDto("system", "", "")
                                )
                            )
                        }
                    })


                    android.util.Log.d("MessagesViewModel", "Total items: ${combined.size} (DMs: ${directList.size}, Clubs: ${clubList.size})")
                    
                    android.util.Log.d("MessagesViewModel", "Sorting ${combined.size} items...")
                    combined.forEach { 
                        android.util.Log.d("MessagesViewModel", "Item: ${it.partner.name}, Time: '${it.lastMessage.createdAt}', Unread: ${it.unreadCount}") 
                    }
                    
                    // Sort by newest
                    _conversations.value = combined.sortedByDescending { it.lastMessage.createdAt }
                 }

            } catch (e: Exception) {
                android.util.Log.e("MessagesViewModel", "Error loading conversations", e)
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}

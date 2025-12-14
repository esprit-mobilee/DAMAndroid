package com.example.esprit.ui.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.chat.ConversationDto
import com.example.esprit.repository.ChatRepository
import com.example.esprit.repository.UserRepository
import com.example.esprit.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val _conversations = MutableStateFlow<List<ConversationDto>>(emptyList())
    val conversations = _conversations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun loadConversations() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val user = userRepository.getMe()
                val userId = user.id ?: throw Exception("User ID is null") // Safe unwrap
                
                android.util.Log.d("MessagesViewModel", "loadConversations: user=${userId} role=${user.role} name=${user.name}")
                
                // 1. Fetch Private Chats
                val chatResult = chatRepository.getConversations(userId)
                if (chatResult.isFailure) {
                    android.util.Log.e("MessagesViewModel", "Failed to fetch private chats: ${chatResult.exceptionOrNull()}")
                }
                val privateChats = chatResult.getOrElse { emptyList() }

                // 2. Fetch Group Chats (Logic differs by Role)
                val isClub = user.role.equals("CLUB", ignoreCase = true)
                
                val clubChats = if (isClub) {
                     // Get the actual Club ID (not User ID)
                     val clubId = user.presidentOf ?: user.club ?: userId
                     android.util.Log.d("MessagesViewModel", "User is CLUB. AccountId=${userId}, ClubId=$clubId")

                     // If I am a CLUB, fetch MY OWN group chat status
                     val historyResult = chatRepository.getHistory(clubId, limit = 1)
                     val lastMsg = historyResult.getOrNull()?.firstOrNull()
                     
                     if (lastMsg != null) {
                         android.util.Log.d("MessagesViewModel", "Found existing group history. Last: ${lastMsg.content}")
                         listOf(
                            com.example.esprit.model.chat.ConversationDto(
                                partnerId = clubId, 
                                partner = com.example.esprit.model.User(
                                    id = clubId,
                                    name = user.name ?: "My Club",
                                    email = "Club Group",
                                    role = "CLUB"
                                ),
                                lastMessage = lastMsg
                            )
                         )
                     } else {
                         android.util.Log.d("MessagesViewModel", "No history found. Showing empty state.")
                         listOf(
                            com.example.esprit.model.chat.ConversationDto(
                                partnerId = clubId,
                                partner = com.example.esprit.model.User(
                                    id = clubId,
                                    name = user.name ?: "My Club",
                                    email = "Club Group",
                                    role = "CLUB"
                                ),
                                lastMessage = com.example.esprit.model.chat.MessageDto(
                                    id = "club_dummy_${clubId}",
                                    clubId = clubId,
                                    senderId = com.example.esprit.model.chat.ChatUserDto(user.id, user.name ?: "Club", "", null),
                                    content = "Tap to view group chat",
                                    createdAt = java.time.Instant.now().toString(),
                                    type = "INFO"
                                )
                            )
                         )
                     }
                } else {
                    android.util.Log.d("MessagesViewModel", "User is STUDENT. Fetching joined clubs.")
                    // If I am a STUDENT, fetch clubs I have joined
                    val clubResult = studentRepository.getAllClubs()
                    val joinedClubs = if (clubResult is com.example.esprit.util.UiState.Success<*>) {
                        val data = clubResult.data as? List<com.example.esprit.model.club.ClubHomeDto> ?: emptyList()
                        data.filter { 
                            it.membershipStatus == "MEMBER" || it.membershipStatus == "PRESIDENT" 
                        }
                    } else {
                        emptyList()
                    }
    
                    // Map Joined Clubs to ConversationDto
                    joinedClubs.map { club ->
                        com.example.esprit.model.chat.ConversationDto(
                            partnerId = club.id,
                            partner = com.example.esprit.model.User(
                                id = club.id,
                                name = club.name,
                                email = "Club Group",
                                role = "CLUB" 
                            ),
                            lastMessage = com.example.esprit.model.chat.MessageDto(
                                id = "club_dummy_${club.id}",
                                clubId = club.id,
                                senderId = com.example.esprit.model.chat.ChatUserDto(club.id, club.name, "", club.imageUrl),
                                content = "Tap to view group chat",
                                createdAt = java.time.Instant.now().toString(), // Put at top or sort by real activity if available
                                type = "INFO"
                            )
                        )
                    }
                }

                // 4. Merge and Sort
                val merged = (privateChats + clubChats).sortedByDescending { it.lastMessage.createdAt }
                android.util.Log.d("MessagesViewModel", "Total conversations: ${merged.size}")
                _conversations.value = merged

            } catch (e: Exception) {
                android.util.Log.e("MessagesViewModel", "Error loading conversations", e)
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}

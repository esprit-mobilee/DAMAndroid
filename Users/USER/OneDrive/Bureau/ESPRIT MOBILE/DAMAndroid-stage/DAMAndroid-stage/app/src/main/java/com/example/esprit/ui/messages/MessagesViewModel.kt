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
                
                // 1. Fetch Private Chats
                val chatResult = chatRepository.getConversations(user.id)
                val privateChats = chatResult.getOrElse { emptyList() }

                // 2. Fetch Joined Clubs (Group Chats)
                val clubResult = studentRepository.getAllClubs()
                val joinedClubs = if (clubResult is com.example.esprit.util.UiState.Success) {
                    clubResult.data?.filter { 
                        it.membershipStatus == "MEMBER" || it.membershipStatus == "PRESIDENT" 
                    } ?: emptyList()
                } else {
                    emptyList()
                }

                // 3. Map Clubs to ConversationDto
                val clubChats = joinedClubs.map { club ->
                    com.example.esprit.model.chat.ConversationDto(
                        partnerId = club.id,
                        partner = com.example.esprit.model.User(
                            id = club.id,
                            name = club.name,
                            email = "Club Group",
                            role = "CLUB" // distinguish club
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

                // 4. Merge and Sort
                val merged = (privateChats + clubChats).sortedByDescending { it.lastMessage.createdAt }
                _conversations.value = merged

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}

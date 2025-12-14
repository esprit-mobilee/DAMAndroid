package com.example.esprit.ui.student.club

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.club.ClubEventDto
import com.example.esprit.model.club.ClubHomeDto
import com.example.esprit.model.club.ClubPostDto
import com.example.esprit.repository.ClubPostsRepository
import com.example.esprit.repository.StudentRepository
import com.example.esprit.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentClubProfileUiState(
    val loading: Boolean = false,
    val club: ClubHomeDto? = null,
    val posts: List<ClubPostDto> = emptyList(),
    val events: List<ClubEventDto> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class StudentClubProfileViewModel @Inject constructor(
    private val studentRepo: StudentRepository,
    private val postsRepo: ClubPostsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentClubProfileUiState(loading = true))
    val uiState: StateFlow<StudentClubProfileUiState> = _uiState

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId

    fun loadClub(clubId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            
            // 1. Fetch Current User
            val userRes = studentRepo.getMe()
            val userId = if (userRes is UiState.Success) {
                _currentUserId.value = userRes.data.id
                userRes.data.id
            } else null

            // 2. Load Club Details
            when (val clubRes = studentRepo.getClubDetails(clubId)) {
                is UiState.Success -> {
                    val rawClub = clubRes.data
                    
                    // 3. Compute Membership Status
                    // Backend findOne might not return 'membershipStatus', so we force compute it if we have the user
                    val actualStatus = if (userId != null) {
                        val isMember = rawClub.members?.any { it.id == userId } == true
                        val isPresident = rawClub.members?.any { it.id == userId && it.role == "PRESIDENT" } == true
                        
                        if (isPresident) "PRESIDENT"
                        else if (isMember) "MEMBER"
                        else rawClub.membershipStatus // Fallback to backend value (e.g. PENDING)
                    } else {
                        rawClub.membershipStatus
                    }

                    _uiState.value = _uiState.value.copy(
                        club = rawClub.copy(membershipStatus = actualStatus),
                        loading = false
                    )
                    
                    // Load posts
                    loadPosts(clubId)
                }
                is UiState.Error -> {
                    _uiState.value = StudentClubProfileUiState(error = clubRes.message)
                }
                UiState.Loading -> {}
            }
        }
    }

    private fun loadPosts(clubId: String) {
        viewModelScope.launch {
            when (val postsRes = postsRepo.list(clubId)) {
                is UiState.Success -> {
                    _uiState.value = _uiState.value.copy(posts = postsRes.data ?: emptyList())
                }
                else -> {}
            }
        }
    }

    fun joinClub(clubId: String) {
        viewModelScope.launch {
            when (studentRepo.joinClub(clubId)) {
                is UiState.Success -> {
                    // Reload club to update join status
                    loadClub(clubId)
                }
                else -> {}
            }
        }
    }

    fun likePost(postId: String) {
        viewModelScope.launch {
            when (val res = postsRepo.like(postId)) {
                is UiState.Success -> updatePostInList(res.data)
                else -> {}
            }
        }
    }

    fun dislikePost(postId: String) {
        viewModelScope.launch {
            when (val res = postsRepo.dislike(postId)) {
                is UiState.Success -> updatePostInList(res.data)
                else -> {}
            }
        }
    }

    fun commentPost(postId: String, content: String) {
        viewModelScope.launch {
            when (val res = postsRepo.comment(postId, content)) {
                is UiState.Success -> updatePostInList(res.data)
                else -> {}
            }
        }
    }

    fun updateComment(postId: String, commentId: String, content: String) {
        viewModelScope.launch {
            when (val res = postsRepo.updateComment(postId, commentId, content)) {
                is UiState.Success -> updatePostInList(res.data)
                else -> {}
            }
        }
    }

    fun deleteComment(postId: String, commentId: String) {
        viewModelScope.launch {
            when (val res = postsRepo.deleteComment(postId, commentId)) {
                is UiState.Success -> updatePostInList(res.data)
                else -> {}
            }
        }
    }

    fun reactToComment(postId: String, commentId: String, emoji: String) {
        viewModelScope.launch {
            when (val res = postsRepo.reactToComment(postId, commentId, emoji)) {
                is UiState.Success -> updatePostInList(res.data)
                else -> {}
            }
        }
    }

    fun replyToComment(postId: String, commentId: String, content: String) {
        viewModelScope.launch {
            when (val res = postsRepo.replyToComment(postId, commentId, content)) {
                is UiState.Success -> updatePostInList(res.data)
                else -> {}
            }
        }
    }

    private fun updatePostInList(updatedPost: ClubPostDto?) {
        updatedPost ?: return
        val currentList = _uiState.value.posts.toMutableList()
        val index = currentList.indexOfFirst { it.id == updatedPost.id }
        if (index != -1) {
            currentList[index] = updatedPost
            _uiState.value = _uiState.value.copy(posts = currentList)
        }
    }
}

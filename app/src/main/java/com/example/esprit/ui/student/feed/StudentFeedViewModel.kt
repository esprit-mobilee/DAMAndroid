package com.example.esprit.ui.student.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.club.ClubPostDto
import com.example.esprit.repository.ClubPostsRepository
import com.example.esprit.repository.StudentRepository
import com.example.esprit.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

data class StudentFeedUiState(
    val loading: Boolean = false,
    val posts: List<ClubPostDto> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class StudentFeedViewModel @Inject constructor(
    private val studentRepo: StudentRepository,
    private val postsRepo: ClubPostsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentFeedUiState(loading = true))
    val uiState: StateFlow<StudentFeedUiState> = _uiState

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId

    init {
        loadFeed()
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
             val userRes = studentRepo.getMe()
            val userId = if (userRes is UiState.Success<*>) {
                val user = userRes.data as? com.example.esprit.model.User
                _currentUserId.value = user?.id
                user?.id
            } else null
        }
    }

    fun loadFeed() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            when (val res = studentRepo.getFeed()) {
                is UiState.Success<*> -> {
                    val posts = res.data as? List<ClubPostDto>
                    _uiState.value = StudentFeedUiState(posts = posts ?: emptyList())
                }
                is UiState.Error -> {
                    _uiState.value = StudentFeedUiState(error = res.message)
                }
                UiState.Loading -> {}
            }
        }
    }

    fun likePost(postId: String) {
        viewModelScope.launch {
            when (val res = postsRepo.like(postId)) {
                is UiState.Success<*> -> updatePostInList(res.data as? ClubPostDto)
                else -> {} // Handle error
            }
        }
    }

    fun dislikePost(postId: String) {
        viewModelScope.launch {
            when (val res = postsRepo.dislike(postId)) {
                is UiState.Success<*> -> updatePostInList(res.data as? ClubPostDto)
                else -> {}
            }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            when (postsRepo.delete(postId)) {
                is UiState.Success<*> -> {
                    // Remove from list
                    val currentList = _uiState.value.posts.toMutableList()
                    currentList.removeAll { it.id == postId }
                    _uiState.value = _uiState.value.copy(posts = currentList)
                }
                else -> {}
            }
        }
    }

    fun editPost(postId: String, content: String) {
        viewModelScope.launch {
            val contentBody = RequestBody.create(MultipartBody.FORM, content)
            when (val res = postsRepo.update(postId, contentBody, null)) {
                is UiState.Success<*> -> updatePostInList(res.data as? ClubPostDto)
                else -> {}
            }
        }
    }

    fun commentPost(postId: String, content: String) {
        viewModelScope.launch {
            when (val res = postsRepo.comment(postId, content)) {
                is UiState.Success<*> -> updatePostInList(res.data as? ClubPostDto)
                else -> {}
            }
        }
    }

    fun updateComment(postId: String, commentId: String, content: String) {
        viewModelScope.launch {
            when (val res = postsRepo.updateComment(postId, commentId, content)) {
                is UiState.Success<*> -> updatePostInList(res.data as? ClubPostDto)
                else -> {}
            }
        }
    }

    fun deleteComment(postId: String, commentId: String) {
        viewModelScope.launch {
            when (val res = postsRepo.deleteComment(postId, commentId)) {
                is UiState.Success<*> -> updatePostInList(res.data as? ClubPostDto)
                else -> {}
            }
        }
    }

    fun reactToComment(postId: String, commentId: String, emoji: String) {
        viewModelScope.launch {
            when (val res = postsRepo.reactToComment(postId, commentId, emoji)) {
                is UiState.Success<*> -> updatePostInList(res.data as? ClubPostDto)
                else -> {}
            }
        }
    }

    fun replyToComment(postId: String, commentId: String, content: String) {
        viewModelScope.launch {
            when (val res = postsRepo.replyToComment(postId, commentId, content)) {
                is UiState.Success<*> -> updatePostInList(res.data as? ClubPostDto)
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

package com.example.esprit.ui.club

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.club.ClubPostDto
import com.example.esprit.network.ApiService
import com.example.esprit.repository.ClubPostsRepository
import com.example.esprit.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

data class PostsUiState(
    val loading: Boolean = false,
    val posts: List<ClubPostDto> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ClubPostsViewModel @Inject constructor(
    private val repo: ClubPostsRepository,
    private val apiService: ApiService
) : ViewModel() {
    private val _uiState = MutableStateFlow(PostsUiState(loading = true))
    val uiState: StateFlow<PostsUiState> = _uiState
    private var clubId: String? = null
    
    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId
    
    init {
        viewModelScope.launch {
            try {
                val user = apiService.getMe()
                _currentUserId.value = user.id
            } catch (e: Exception) {
                android.util.Log.e("ClubPostsViewModel", "Error fetching current user", e)
            }
        }
    }

    fun load(clubId: String) {
        viewModelScope.launch {
            android.util.Log.d("ClubPostsViewModel", "Loading posts for clubId: $clubId")
            this@ClubPostsViewModel.clubId = clubId
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            when (val res = repo.list(clubId)) {
                is UiState.Success -> {
                    android.util.Log.d("ClubPostsViewModel", "Loaded ${res.data.size} posts")
                    _uiState.value = PostsUiState(posts = res.data)
                }
                is UiState.Error -> {
                    android.util.Log.e("ClubPostsViewModel", "Error loading posts: ${res.message}")
                    _uiState.value = PostsUiState(error = res.message)
                }
                UiState.Loading -> _uiState.value = PostsUiState(loading = true)
            }
        }
    }

    suspend fun delete(id: String): UiState<Unit> = repo.delete(id)

    fun refresh() {
        clubId?.let { load(it) }
    }

    fun like(postId: String) {
        viewModelScope.launch {
            when (val res = repo.like(postId)) {
                is UiState.Success -> updatePostInList(res.data)
                is UiState.Error -> android.util.Log.e("ClubPostsViewModel", "Error liking post: ${res.message}")
                else -> {}
            }
        }
    }

    fun dislike(postId: String) {
        viewModelScope.launch {
            when (val res = repo.dislike(postId)) {
                is UiState.Success -> updatePostInList(res.data)
                is UiState.Error -> android.util.Log.e("ClubPostsViewModel", "Error disliking post: ${res.message}")
                else -> {}
            }
        }
    }

    fun comment(postId: String, content: String) {
        viewModelScope.launch {
            when (val res = repo.comment(postId, content)) {
                is UiState.Success -> updatePostInList(res.data)
                is UiState.Error -> android.util.Log.e("ClubPostsViewModel", "Error commenting post: ${res.message}")
                else -> {}
            }
        }
    }

    fun updateComment(postId: String, commentId: String, content: String) {
        viewModelScope.launch {
            when (val res = repo.updateComment(postId, commentId, content)) {
                is UiState.Success -> updatePostInList(res.data)
                is UiState.Error -> android.util.Log.e("ClubPostsViewModel", "Error updating comment: ${res.message}")
                else -> {}
            }
        }
    }

    fun deleteComment(postId: String, commentId: String) {
        viewModelScope.launch {
            when (val res = repo.deleteComment(postId, commentId)) {
                is UiState.Success -> updatePostInList(res.data)
                is UiState.Error -> android.util.Log.e("ClubPostsViewModel", "Error deleting comment: ${res.message}")
                else -> {}
            }
        }
    }

    fun reactToComment(postId: String, commentId: String, emoji: String) {
        viewModelScope.launch {
            when (val res = repo.reactToComment(postId, commentId, emoji)) {
                is UiState.Success -> updatePostInList(res.data)
                is UiState.Error -> android.util.Log.e("ClubPostsViewModel", "Error reacting to comment: ${res.message}")
                else -> {}
            }
        }
    }

    fun replyToComment(postId: String, commentId: String, content: String) {
        viewModelScope.launch {
            when (val res = repo.replyToComment(postId, commentId, content)) {
                is UiState.Success -> updatePostInList(res.data)
                is UiState.Error -> android.util.Log.e("ClubPostsViewModel", "Error replying to comment: ${res.message}")
                else -> {}
            }
        }
    }

    private fun updatePostInList(updatedPost: ClubPostDto) {
        val currentPosts = _uiState.value.posts.toMutableList()
        val index = currentPosts.indexOfFirst { it.id == updatedPost.id }
        if (index != -1) {
            currentPosts[index] = updatedPost
            _uiState.value = _uiState.value.copy(posts = currentPosts)
        }
    }
}

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val repo: ClubPostsRepository
) : ViewModel() {
    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving

    suspend fun create(
        clubId: String,
        content: RequestBody,
        image: MultipartBody.Part?
    ): UiState<ClubPostDto> {
        _saving.value = true
        val res = repo.create(clubId, content, image)
        _saving.value = false
        return res
    }
}

@HiltViewModel
class EditPostViewModel @Inject constructor(
    private val repo: ClubPostsRepository
) : ViewModel() {
    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving

    suspend fun update(id: String, content: RequestBody, image: MultipartBody.Part?): UiState<ClubPostDto> {
        _saving.value = true
        val res = repo.update(id, content, image)
        _saving.value = false
        return res
    }
}

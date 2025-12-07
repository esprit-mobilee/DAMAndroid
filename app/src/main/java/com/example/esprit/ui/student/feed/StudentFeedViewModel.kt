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

    init {
        loadFeed()
    }

    fun loadFeed() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            when (val res = studentRepo.getFeed()) {
                is UiState.Success -> {
                    _uiState.value = StudentFeedUiState(posts = res.data ?: emptyList())
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
                is UiState.Success -> updatePostInList(res.data)
                else -> {} // Handle error
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

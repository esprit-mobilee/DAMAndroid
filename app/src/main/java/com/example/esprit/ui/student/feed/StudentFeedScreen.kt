package com.example.esprit.ui.student.feed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.esprit.ui.components.PostCard

@Composable
fun StudentFeedScreen(
    viewModel: StudentFeedViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (state.error != null) {
            Text(
                text = state.error ?: "Erreur inconnue",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (state.posts.isEmpty()) {
            Text(
                text = "Aucune publication pour le moment.",
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp) // Space for bottom nav
            ) {
                items(state.posts) { post ->
                    PostCard(
                        post = post,
                        clubName = post.clubName ?: "Club", // Assuming clubName is in DTO or fetched
                        clubImage = post.clubImage, // Assuming clubImage is in DTO
                        onEdit = {}, // Students can't edit others' posts
                        onDelete = {}, // Students can't delete others' posts
                        onLike = { viewModel.likePost(post.id) },
                        onDislike = { viewModel.dislikePost(post.id) },
                        onComment = { content -> viewModel.commentPost(post.id, content) }
                    )
                }
            }
        }
    }
}

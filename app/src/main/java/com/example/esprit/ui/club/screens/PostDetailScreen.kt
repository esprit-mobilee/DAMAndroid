package com.example.esprit.ui.club.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.esprit.ui.club.ClubPostsViewModel
import com.example.esprit.ui.components.PostCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    onNavigateBack: () -> Unit,
    viewModel: ClubPostsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    
    // Load post on entering
    LaunchedEffect(postId) {
        viewModel.loadPost(postId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Publication") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (state.loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.error != null) {
                Text(
                    text = state.error ?: "Erreur",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                state.selectedPost?.let { post ->
                    val currentUserId by viewModel.currentUserId.collectAsState()
                    var showDeleteDialog by remember { mutableStateOf(false) }

                    LazyColumn {
                        item {
                            PostCard(
                                post = post,
                                clubName = "", // Maybe fetch club name or not needed
                                clubImage = null, // Maybe needed
                                currentUserId = currentUserId ?: "",
                                canEdit = false, // Assuming detail view is read-only? Or check membership
                                onEdit = {},
                                onDelete = {},
                                onLike = { viewModel.like(post.id) },
                                onDislike = { viewModel.dislike(post.id) },
                                onComment = { content -> viewModel.comment(post.id, content) },
                                onUpdateComment = { commentId, content ->
                                    viewModel.updateComment(post.id, commentId, content)
                                },
                                onDeleteComment = { commentId ->
                                    viewModel.deleteComment(post.id, commentId)
                                },
                                onReactToComment = { commentId, emoji ->
                                    viewModel.reactToComment(post.id, commentId, emoji)
                                },
                                onReplyToComment = { commentId, content ->
                                    viewModel.replyToComment(post.id, commentId, content)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

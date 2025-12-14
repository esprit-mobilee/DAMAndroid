package com.example.esprit.ui.student.feed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedPostId by remember { mutableStateOf<String?>(null) }
    var editContent by remember { mutableStateOf("") }

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
                    val currentUserId by viewModel.currentUserId.collectAsState()
                    PostCard(
                        post = post,
                        clubName = post.clubName ?: "Club",
                        clubImage = post.clubImage,
                        currentUserId = currentUserId ?: "",
                        onEdit = {
                            selectedPostId = post.id
                            editContent = post.content
                            showEditDialog = true
                        },
                        onDelete = {
                            selectedPostId = post.id
                            showDeleteDialog = true
                        },
                        onLike = { viewModel.likePost(post.id) },
                        onDislike = { viewModel.dislikePost(post.id) },
                        onComment = { content -> viewModel.commentPost(post.id, content) },
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

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Modifier la publication") },
            text = {
                OutlinedTextField(
                    value = editContent,
                    onValueChange = { editContent = it },
                    label = { Text("Contenu") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedPostId?.let { viewModel.editPost(it, editContent) }
                        showEditDialog = false
                    }
                ) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer la publication ?") },
            text = { Text("Voulez-vous vraiment supprimer cette publication ? Cette action est irréversible.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedPostId?.let { viewModel.deletePost(it) }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

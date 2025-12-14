package com.example.esprit.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Comment
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.esprit.model.club.ClubPostDto
import com.example.esprit.ui.theme.RedPrimary
import com.example.esprit.util.Constants

@Composable
fun PostCard(
    post: ClubPostDto,
    clubName: String,
    clubImage: String?,
    currentUserId: String,
    canEdit: Boolean = false,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onLike: () -> Unit,
    onDislike: () -> Unit,
    onComment: (String) -> Unit,
    onUpdateComment: (String, String) -> Unit,
    onDeleteComment: (String) -> Unit,
    onReactToComment: (String, String) -> Unit,
    onReplyToComment: (String, String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showComments by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (clubImage != null) {
                        val fullClubImage = if (clubImage.startsWith("http")) {
                            clubImage
                        } else {
                            val baseUrl = Constants.BASE_URL.replace("/api/", "")
                            "$baseUrl$clubImage"
                        }
                        Image(
                            painter = rememberAsyncImagePainter(fullClubImage),
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Gray)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = clubName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = post.createdAt.take(10), // Simple date formatting
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                
                if (canEdit) {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Options")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Modifier") },
                                onClick = {
                                    showMenu = false
                                    onEdit()
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Supprimer", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium
            )

            post.imageUrl?.let { url ->
                val fullUrl = if (url.startsWith("http")) {
                    url
                } else {
                    val baseUrl = Constants.BASE_URL.replace("/api/", "")
                    "$baseUrl$url"
                }

                Spacer(modifier = Modifier.height(12.dp))
                Image(
                    painter = rememberAsyncImagePainter(fullUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFE4E6EB))
            
            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Like
                TextButton(onClick = onLike) {
                    Icon(
                        imageVector = if (post.isLiked) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = "Like",
                        tint = if (post.isLiked) Color(0xFF1877F2) else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${post.likes?.size ?: 0}",
                        color = if (post.isLiked) Color(0xFF1877F2) else Color.Gray
                    )
                }

                // Dislike
                TextButton(onClick = onDislike) {
                    Icon(
                        imageVector = if (post.isDisliked) Icons.Default.ThumbDown else Icons.Outlined.ThumbDown,
                        contentDescription = "Dislike",
                        tint = if (post.isDisliked) RedPrimary else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${post.dislikes?.size ?: 0}",
                        color = if (post.isDisliked) RedPrimary else Color.Gray
                    )
                }

                // Comment
                TextButton(onClick = { showComments = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Comment,
                        contentDescription = "Comment",
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${post.comments.size}",
                        color = Color.Gray
                    )
                }
            }
        }
    }
    
    // Comment Bottom Sheet
    if (showComments) {
        CommentBottomSheet(
            postId = post.id,
            comments = post.comments,
            currentUserId = currentUserId,
            onDismiss = { showComments = false },
            onAddComment = { content ->
                onComment(content)
            },
            onUpdateComment = { commentId, content ->
                onUpdateComment(commentId, content)
            },
            onDeleteComment = { commentId ->
                onDeleteComment(commentId)
            },
            onReactToComment = { commentId, emoji ->
                onReactToComment(commentId, emoji)
            },
            onReplyToComment = { commentId, content ->
                onReplyToComment(commentId, content)
            }
        )
    }
}

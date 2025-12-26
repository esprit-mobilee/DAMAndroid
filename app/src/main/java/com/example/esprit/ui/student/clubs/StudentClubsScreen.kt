package com.example.esprit.ui.student.clubs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.esprit.model.club.ClubHomeDto
import com.example.esprit.ui.components.PostCard
import com.example.esprit.ui.student.events.StudentEventCard
import com.example.esprit.ui.student.events.StudentEventsViewModel
import com.example.esprit.ui.student.feed.StudentFeedViewModel
import com.example.esprit.util.Constants

@Composable
fun StudentClubsScreen(
    clubsViewModel: StudentClubsViewModel = hiltViewModel(),
    feedViewModel: StudentFeedViewModel = hiltViewModel(),
    eventsViewModel: StudentEventsViewModel = hiltViewModel(),
    onNavigateClub: (String) -> Unit,
    onNavigateEventDetail: (String) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    val clubsState by clubsViewModel.uiState.collectAsState()
    val feedState by feedViewModel.uiState.collectAsState()
    val eventsState by eventsViewModel.uiState.collectAsState()

    var showRegistrationDialog by remember { mutableStateOf(false) }
    var selectedEventId by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Fil d'actualité") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Événements") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Tous les clubs") }
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                0 -> {
                    // FEED CONTENT
                    if (feedState.loading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else if (feedState.error != null) {
                        Text(
                            text = feedState.error ?: "Erreur inconnue",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else if (feedState.posts.isEmpty()) {
                        Text(
                            text = "Aucune publication pour le moment.",
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(feedState.posts) { post ->
                                val currentUserId by feedViewModel.currentUserId.collectAsState()
                                PostCard(
                                    post = post,
                                    clubName = post.clubName ?: "Club",
                                    clubImage = post.clubImage,
                                    currentUserId = currentUserId ?: "",
                                    onEdit = {},
                                    onDelete = {},
                                    onLike = { feedViewModel.likePost(post.id) },
                                    onDislike = { feedViewModel.dislikePost(post.id) },
                                    onComment = { content -> feedViewModel.commentPost(post.id, content) },
                                    onUpdateComment = { commentId, content ->
                                        feedViewModel.updateComment(post.id, commentId, content)
                                    },
                                    onDeleteComment = { commentId ->
                                        feedViewModel.deleteComment(post.id, commentId)
                                    },
                                    onReactToComment = { commentId, emoji ->
                                        feedViewModel.reactToComment(post.id, commentId, emoji)
                                    },
                                    onReplyToComment = { commentId, content ->
                                        feedViewModel.replyToComment(post.id, commentId, content)
                                    }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // EVENTS CONTENT
                    if (eventsState.loading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else if (eventsState.error != null) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = eventsState.error ?: "Erreur inconnue",
                                color = MaterialTheme.colorScheme.error
                            )
                            TextButton(onClick = { eventsViewModel.loadEvents() }) {
                                Text("Réessayer")
                            }
                        }
                    } else if (eventsState.events.isEmpty()) {
                        Text(
                            text = "Aucun événement disponible pour le moment.",
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(eventsState.events) { event ->
                                StudentEventCard(
                                    event = event,
                                    onClick = { onNavigateEventDetail(event.id) },
                                    onRegister = {
                                        selectedEventId = event.id
                                        showRegistrationDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
                2 -> {
                    // CLUBS LIST CONTENT
                    if (clubsState.loading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else if (clubsState.error != null) {
                        Text(
                            text = clubsState.error ?: "Erreur inconnue",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(clubsState.clubs) { club ->
                                ClubListItem(
                                    club = club,
                                    onJoin = { clubsViewModel.onJoinClick(club.id) },
                                    onClick = { onNavigateClub(club.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Event Registration Dialog
    if (showRegistrationDialog && selectedEventId != null) {
        val event = eventsState.events.find { it.id == selectedEventId }
        if (event != null) {
            com.example.esprit.ui.components.JoinEventDialog(
                event = event,
                onDismiss = { showRegistrationDialog = false },
                onJoin = { answers ->
                    eventsViewModel.joinEvent(selectedEventId!!, answers)
                    showRegistrationDialog = false
                }
            )
        }
    }

    // Club Join Form Dialog
    if (clubsState.showJoinDialog && clubsState.selectedClubId != null) {
        JoinFormDialog(
            questions = clubsState.joinQuestions,
            onDismiss = { clubsViewModel.dismissDialog() },
            onConfirm = { answers ->
                clubsViewModel.submitJoinRequest(clubsState.selectedClubId!!, answers)
            }
        )
    }
}

@Composable
fun JoinFormDialog(
    questions: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<Map<String, String>>) -> Unit
) {
    // Map of question index to answer text
    val answers = remember { mutableStateMapOf<Int, String>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Questions d'adhésion") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(questions.size) { index ->
                    val question = questions[index]
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = question,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        OutlinedTextField(
                            value = answers[index] ?: "",
                            onValueChange = { answers[index] = it },
                            placeholder = { Text("Votre réponse...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val formattedAnswers = questions.mapIndexed { index, question ->
                        mapOf(
                            "question" to question,
                            "answer" to (answers[index] ?: "")
                        )
                    }
                    onConfirm(formattedAnswers)
                },
                enabled = questions.indices.all { answers[it]?.isNotBlank() == true }
            ) {
                Text("Envoyer la demande")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun ClubListItem(
    club: ClubHomeDto,
    onJoin: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Club Image
            if (club.imageUrl != null) {
                val fullUrl = if (club.imageUrl.startsWith("http")) {
                    club.imageUrl
                } else {
                    val baseUrl = Constants.BASE_URL.replace("/api/", "")
                    "$baseUrl${club.imageUrl}"
                }

                Image(
                    painter = rememberAsyncImagePainter(fullUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = club.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = club.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (!club.description.isNullOrBlank()) {
                    Text(
                        text = club.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 2
                    )
                }
                Text(
                    text = "${club.members?.size ?: club.totalMembers} membres",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Button based on membership status
            when (club.membershipStatus) {
                "PENDING" -> {
                    OutlinedButton(
                        onClick = { },
                        enabled = false,
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("En attente")
                    }
                }
                "MEMBER" -> {
                    Text(
                        text = "Membre",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Green,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                "PRESIDENT" -> {
                    Text(
                        text = "Admin",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                else -> {
                    if (club.joinEnabled) {
                        Button(
                            onClick = onJoin,
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("Rejoindre")
                        }
                    }
                }
            }
        }
    }
}
package com.example.esprit.ui.student.club

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.esprit.ui.components.PostCard
import com.example.esprit.ui.theme.BgGray
import com.example.esprit.ui.theme.RedPrimary
import com.example.esprit.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentClubProfileScreen(
    clubId: String,
    onNavigateBack: () -> Unit,
    onNavigateChat: (String) -> Unit, // ADDED
    viewModel: StudentClubProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(clubId) {
        viewModel.loadClub(clubId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.club?.name ?: "Club") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RedPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = BgGray
    ) { padding ->
        if (state.loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.error ?: "Error loading club",
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            state.club?.let { club ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Club Header
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(16.dp)
                        ) {
                            // Club Image
                            val imageUrl = club.imageUrl?.let {
                                if (it.startsWith("http")) it
                                else "${Constants.BASE_URL.replace("/api/", "")}$it"
                            }

                            if (imageUrl != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(imageUrl),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .align(Alignment.CenterHorizontally),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(Color.LightGray)
                                        .align(Alignment.CenterHorizontally),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = club.name.take(1).uppercase(),
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Club Name
                            Text(
                                text = club.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )

                            // Description
                            if (!club.description.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = club.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Stats Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${club.members?.size ?: club.totalMembers}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Membres",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${club.totalEvents}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Événements",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }

                            // Action Button (Join / Chat / Pending)
                            Spacer(modifier = Modifier.height(16.dp))
                            when (club.membershipStatus) {
                                "MEMBER", "PRESIDENT" -> {
                                    Button(
                                        onClick = { onNavigateChat(club.name) }, // UPDATED
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text("Chat du Club")
                                    }
                                }
                                "PENDING" -> {
                                    OutlinedButton(
                                        onClick = { },
                                        enabled = false,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Demande en attente")
                                    }
                                }
                                else -> {
                                    if (club.joinEnabled) {
                                        Button(
                                            onClick = { viewModel.joinClub(clubId) },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = RedPrimary)
                                        ) {
                                            Text("Rejoindre le club")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Tabs
                    item {
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color.White
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = { Text("Feed") }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = { Text("Events") }
                            )
                            Tab(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                text = { Text("About") }
                            )
                        }
                    }

                    // Tab Content
                    when (selectedTab) {
                        0 -> {
                            // Feed Tab
                            if (state.posts.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Aucune publication",
                                            color = Color.Gray
                                        )
                                    }
                                }
                            } else {
                                items(state.posts) { post ->
                                    PostCard(
                                        post = post,
                                        clubName = club.name,
                                        clubImage = club.imageUrl,
                                        onEdit = {}, // Students can't edit
                                        onDelete = {}, // Students can't delete
                                        onLike = { viewModel.likePost(post.id) },
                                        onDislike = { viewModel.dislikePost(post.id) },
                                        onComment = { content ->
                                            viewModel.commentPost(post.id, content)
                                        }
                                    )
                                }
                            }
                        }
                        1 -> {
                            // Events Tab
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Events coming soon",
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                        2 -> {
                            // About Tab
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White)
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "À propos",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = club.description ?: "Aucune description disponible",
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    if (!club.tags.isNullOrEmpty()) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Tags",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        club.tags.forEach { tag ->
                                            Text(
                                                text = "• $tag",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

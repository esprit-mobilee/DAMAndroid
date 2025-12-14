package com.example.esprit.ui.club.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.Comment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.esprit.model.club.ClubHomeDto
import com.example.esprit.model.club.ClubPostDto
import com.example.esprit.model.club.ClubEventDto
import com.example.esprit.model.club.CommentDto
import com.example.esprit.ui.club.ClubEventsViewModel
import com.example.esprit.ui.club.ClubHomeViewModel
import com.example.esprit.ui.club.ClubPostsViewModel
import com.example.esprit.ui.components.PostCard
import com.example.esprit.ui.components.LocationDisplay
import com.example.esprit.ui.theme.RedPrimary
import com.example.esprit.util.Constants
import com.example.esprit.util.UiState
import kotlinx.coroutines.launch





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubHomeScreen(
    onCreateEvent: () -> Unit,
    onCreatePost: (String) -> Unit,
    onEditPost: (String) -> Unit,
    onNavigatePosts: () -> Unit,
    onNavigateMembers: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateNotifications: () -> Unit,
    onNavigateRequests: () -> Unit,
    onNavigateMessages: () -> Unit,
    onNavigateBack: () -> Unit = {},
    onLogout: () -> Unit,
    onEventClick: (String) -> Unit,
    onEventEdit: (String) -> Unit,
    viewModel: ClubHomeViewModel = hiltViewModel(),
    postsViewModel: ClubPostsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val eventsViewModel: ClubEventsViewModel = hiltViewModel()
    var selectedTab by remember { mutableStateOf(0) } // 0: Feed, 1: Events, 2: About
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.refresh()
        eventsViewModel.load()
    }

    // Load posts when club is loaded
    LaunchedEffect(state.club) {
        state.club?.let { postsViewModel.load(it.id) }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                ClubDrawerContent(
                    club = state.club,
                    onNavigateSettings = {
                        scope.launch { drawerState.close() }
                        onNavigateSettings()
                    },
                    onNavigateMessages = {
                        scope.launch { drawerState.close() }
                        onNavigateMessages()
                    },
                    onNavigateRequests = {
                        scope.launch { drawerState.close() }
                        onNavigateRequests()
                    },
                    onLogout = {
                        scope.launch { drawerState.close() }
                        onLogout()
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Club Profile") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateNotifications) {
                            BadgedBox(
                                badge = {
                                    if (state.unreadCount > 0) {
                                        Badge { Text("${state.unreadCount}") }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    )
                )
            },
            floatingActionButton = {
                if (selectedTab == 1) { // Only show FAB in Events tab
                    FloatingActionButton(
                        onClick = onCreateEvent,
                        containerColor = RedPrimary
                    ) {
                        Text("+", color = Color.White, fontSize = 24.sp)
                    }
                }
            }
        ) { paddingValues ->
            when {
                state.loading -> Box(
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                state.club != null -> ClubHomeContent(
                    club = state.club!!,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    onCreateEvent = onCreateEvent,
                    onCreatePost = { onCreatePost(state.club!!.id) },
                    onEditPost = onEditPost,
                    onNavigatePosts = onNavigatePosts,
                    onNavigateMembers = onNavigateMembers,
                    onNavigateMessages = onNavigateMessages,
                    onEventClick = onEventClick,
                    onEventEdit = onEventEdit,
                    eventsViewModel = eventsViewModel,
                    postsViewModel = postsViewModel,
                    viewModel = viewModel,
                    modifier = Modifier.padding(paddingValues)
                )
                else -> ClubHomeFallback(
                    error = state.error,
                    onRetry = { viewModel.refresh() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}



@Composable
fun ClubHomeContent(
    club: ClubHomeDto,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onCreateEvent: () -> Unit,
    onCreatePost: () -> Unit,
    onEditPost: (String) -> Unit,
    onNavigatePosts: () -> Unit,
    onNavigateMembers: () -> Unit,
    onNavigateMessages: () -> Unit,
    onEventClick: (String) -> Unit,
    onEventEdit: (String) -> Unit,
    eventsViewModel: ClubEventsViewModel,
    postsViewModel: ClubPostsViewModel,
    viewModel: ClubHomeViewModel,
    modifier: Modifier = Modifier
) {
    // selectedTab hoisted to parent
    val scrollState = rememberLazyListState()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Get events state and scope at composable level
    val eventsState by eventsViewModel.uiState.collectAsState()
    val postsState by postsViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()


    // Image picker for profile picture and cover photo
    var isUploadingImage by remember { mutableStateOf(false) }
    var imageType by remember { mutableStateOf<String?>(null) } // "profile" or "cover"

    val imagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            scope.launch {
                isUploadingImage = true
                try {
                    val result = when (imageType) {
                        "profile" -> viewModel.updateProfileImage(club.id, uri, context)
                        "cover" -> viewModel.updateCoverImage(club.id, uri, context)
                        else -> null
                    }

                    when (result) {
                        is UiState.Success -> {
                            val message = if (imageType == "profile")
                                "Photo de profil mise à jour!"
                            else
                                "Photo de couverture mise à jour!"
                            android.widget.Toast.makeText(
                                context,
                                message,
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                        is UiState.Error -> {
                            android.widget.Toast.makeText(
                                context,
                                "Erreur: ${result.message}",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                        else -> {}
                    }
                } catch (e: Exception) {
                    android.widget.Toast.makeText(
                        context,
                        "Erreur: ${e.message}",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                } finally {
                    isUploadingImage = false
                    imageType = null
                }
            }
        }
    }

    // Calculate fade based on scroll position - use safe access with derivedStateOf
    val scrollOffset = remember {
        derivedStateOf {
            if (scrollState.firstVisibleItemIndex == 0) {
                scrollState.firstVisibleItemScrollOffset.toFloat()
            } else {
                200f // Force fade out if scrolled past first item
            }
        }
    }

    // Fade out header when scrolling (fade starts after 200dp scroll)
    val fadeThreshold = 200f
    val targetAlpha = remember {
        derivedStateOf {
            if (scrollOffset.value >= fadeThreshold) {
                0f
            } else {
                (1f - (scrollOffset.value / fadeThreshold)).coerceIn(0f, 1f)
            }
        }
    }

    val headerAlpha = animateFloatAsState(
        targetValue = targetAlpha.value,
        label = "headerAlpha"
    )

    LazyColumn(
        state = scrollState,
        modifier = modifier.fillMaxSize()
    ) {
        // Header section (fades on scroll)
        item {
            Column(
                modifier = Modifier.alpha(
                    try {
                        headerAlpha.value
                    } catch (e: Exception) {
                        1f // Default to visible if error
                    }
                )
            ) {
                // Header with gradient background and profile image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    // Cover photo or gradient background
                    var showFullScreenCover by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (!club.coverImageUrl.isNullOrBlank()) {
                            val fullCoverUrl = if (club.coverImageUrl.startsWith("http")) {
                                club.coverImageUrl
                            } else {
                                val baseUrl = Constants.BASE_URL.replace("/api/", "")
                                if (club.coverImageUrl.startsWith("/")) "$baseUrl${club.coverImageUrl}" else "$baseUrl/${club.coverImageUrl}"
                            }

                            Image(
                                painter = rememberAsyncImagePainter(fullCoverUrl),
                                contentDescription = "Cover photo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { showFullScreenCover = true },
                                contentScale = ContentScale.Crop
                            )

                            // Full screen cover dialog
                            if (showFullScreenCover) {
                                FullScreenImageDialog(
                                    imageUrl = fullCoverUrl,
                                    onDismiss = { showFullScreenCover = false }
                                )
                            }
                        } else {
                            // Gradient background fallback
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                RedPrimary,
                                                Color(0xFF4A90E2)
                                            )
                                        )
                                    )
                            )
                        }

                        // Camera icon for changing cover photo
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .clickable {
                                    imageType = "cover"
                                    imagePickerLauncher.launch("image/*")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Change cover photo",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Profile image (circular, positioned at bottom left)
                    var showFullScreenImage by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 16.dp)
                            .offset(y = 40.dp)
                    ) {
                        if (!club.imageUrl.isNullOrBlank()) {
                            val fullImageUrl = if (club.imageUrl.startsWith("http")) {
                                club.imageUrl
                            } else {
                                val baseUrl = Constants.BASE_URL.replace("/api/", "")
                                if (club.imageUrl.startsWith("/")) "$baseUrl${club.imageUrl}" else "$baseUrl/${club.imageUrl}"
                            }

                            Image(
                                painter = rememberAsyncImagePainter(fullImageUrl),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable { showFullScreenImage = true },
                                contentScale = ContentScale.Crop
                            )

                            // Camera icon overlay for editing
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(RedPrimary)
                                    .clickable {
                                        imageType = "profile"
                                        imagePickerLauncher.launch("image/*")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Change profile picture",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Full screen image dialog
                            if (showFullScreenImage) {
                                FullScreenImageDialog(
                                    imageUrl = fullImageUrl,
                                    onDismiss = { showFullScreenImage = false }
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = club.name.take(1),
                                    color = Color.White,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Camera icon overlay for adding profile picture
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(RedPrimary)
                                    .clickable {
                                        imageType = "profile"
                                        imagePickerLauncher.launch("image/*")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Add profile picture",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(50.dp))

                // Club name and member count
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = club.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${club.totalMembers} Members",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { /* Join club action */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RedPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Join Club")
                    }

                    OutlinedButton(
                        onClick = { onNavigateMessages() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Message")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Tab navigation (sticky, doesn't fade)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TabItem("Feed", selectedTab == 0) { onTabSelected(0) }
                TabItem("Events", selectedTab == 1) { onTabSelected(1) }
                TabItem("About", selectedTab == 2) { onTabSelected(2) }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Content based on selected tab
        when (selectedTab) {
            0 -> {
                item { FeedHeader(clubImage = club.imageUrl, onCreatePost = onCreatePost) }

                when {
                    postsState.loading -> item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    postsState.error != null -> item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(postsState.error ?: "Erreur", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    else -> {
                        if (postsState.posts.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No posts yet", color = Color.Gray)
                                }
                            }
                        } else {
                            items(postsState.posts) { post ->
                                var showDeleteDialog by remember { mutableStateOf(false) }
                                val currentUserId by postsViewModel.currentUserId.collectAsState()

                                PostCard(
                                    post = post,
                                    clubName = club.name,
                                    clubImage = club.imageUrl,
                                    currentUserId = currentUserId ?: "",
                                    canEdit = club.membershipStatus == "PRESIDENT",
                                    onEdit = {
                                        onEditPost(post.id)
                                    },
                                    onDelete = {
                                        showDeleteDialog = true
                                    },
                                    onLike = { postsViewModel.like(post.id) },
                                    onDislike = { postsViewModel.dislike(post.id) },
                                    onComment = { content -> postsViewModel.comment(post.id, content) },
                                    onUpdateComment = { commentId, content ->
                                        postsViewModel.updateComment(post.id, commentId, content)
                                    },
                                    onDeleteComment = { commentId ->
                                        postsViewModel.deleteComment(post.id, commentId)
                                    },
                                    onReactToComment = { commentId, emoji ->
                                        postsViewModel.reactToComment(post.id, commentId, emoji)
                                    },
                                    onReplyToComment = { commentId, content ->
                                        postsViewModel.replyToComment(post.id, commentId, content)
                                    }
                                )

                                if (showDeleteDialog) {
                                    AlertDialog(
                                        onDismissRequest = { showDeleteDialog = false },
                                        title = { Text("Supprimer le post") },
                                        text = { Text("Êtes-vous sûr de vouloir supprimer ce post ?") },
                                        confirmButton = {
                                            TextButton(
                                                onClick = {
                                                    showDeleteDialog = false
                                                    scope.launch {
                                                        postsViewModel.delete(post.id)
                                                        postsViewModel.load(club.id)
                                                    }
                                                }
                                            ) {
                                                Text("Supprimer", color = MaterialTheme.colorScheme.error)
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
                        }
                    }
                }
            }
            1 -> {
                // Events content - render items directly in parent LazyColumn
                when {
                    eventsState.loading -> item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    eventsState.error != null -> item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(eventsState.error ?: "Erreur", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    else -> {
                        items(
                            items = eventsState.events,
                            key = { it.id }
                        ) { event ->
                            EventCardWithActions(
                                event = event,
                                onClick = { onEventClick(event.id) },
                                onEdit = { onEventEdit(event.id) },
                                onDelete = {
                                    scope.launch {
                                        eventsViewModel.delete(event.id)
                                        eventsViewModel.load()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
            2 -> item { AboutTabContent(club = club) }
        }
    }
}

@Composable
fun FeedHeader(
    clubImage: String?,
    onCreatePost: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // "What's on your mind?" Input Area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clickable { onCreatePost() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Club Avatar
                    if (clubImage != null) {
                        val fullImageUrl = if (clubImage.startsWith("http")) {
                            clubImage
                        } else {
                            val baseUrl = Constants.BASE_URL.replace("/api/", "")
                            if (clubImage.startsWith("/")) "$baseUrl$clubImage" else "$baseUrl/$clubImage"
                        }

                        Image(
                            painter = rememberAsyncImagePainter(fullImageUrl),
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

                    Spacer(modifier = Modifier.width(12.dp))

                    // Input Placeholder
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFF0F2F5)) // Facebook-like light gray
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "What's on your mind?",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EventsTabContent(
    eventsViewModel: ClubEventsViewModel,
    onEventClick: (String) -> Unit,
    onEventEdit: (String) -> Unit,
    onCreateEvent: () -> Unit
) {
    val eventsState by eventsViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    when {
        eventsState.loading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        eventsState.error != null -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(eventsState.error ?: "Erreur", color = MaterialTheme.colorScheme.error)
        }
        else -> LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(eventsState.events) { event ->
                var showDeleteDialog by remember { mutableStateOf(false) }

                EventCardWithActions(
                    event = event,
                    onClick = { onEventClick(event.id) },
                    onEdit = { onEventEdit(event.id) },
                    onDelete = {
                        showDeleteDialog = true
                    }
                )

                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Supprimer l'événement") },
                        text = { Text("Êtes-vous sûr de vouloir supprimer cet événement ?") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showDeleteDialog = false
                                    scope.launch {
                                        eventsViewModel.delete(event.id)
                                        eventsViewModel.load()
                                    }
                                }
                            ) {
                                Text("Supprimer", color = MaterialTheme.colorScheme.error)
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
        }
    }
}

@Composable
fun AboutTabContent(club: ClubHomeDto) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Club description card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A1A)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "GDSC Esprit",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = club.description ?: "Join us for an exciting workshop on Flutter development next week. We'll cover the basics and build a simple app together. No prior experience required!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun EventCardWithActions(
    event: com.example.esprit.model.club.ClubEventDto,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    event.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
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

            // Date and Location row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date
                event.date?.let { date ->
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                // Location using new component
                LocationDisplay(
                    location = event.location,
                    textColor = Color.Gray
                )
            }

            event.description?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }

            // Display event image if available
            event.imageUrl?.let { imageUrl ->
                val fullImageUrl = if (imageUrl.startsWith("http")) {
                    imageUrl
                } else {
                    val baseUrl = Constants.BASE_URL.replace("/api/", "")
                    "$baseUrl$imageUrl"
                }

                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = rememberAsyncImagePainter(fullImageUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun TabItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit = {}
) {
    TextButton(onClick = onClick) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = text,
                color = if (selected) RedPrimary else Color.Gray,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
            if (selected) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(2.dp)
                        .background(RedPrimary)
                )
            }
        }
    }
}

@Composable
fun PostAction(icon: String, count: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = icon, fontSize = 18.sp)
        Text(
            text = count,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun ClubHomeFallback(
    error: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = error ?: "Failed to load club information",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = RedPrimary
            )
        ) {
            Text("Retry")
        }
    }
}



@Composable
fun FullScreenImageDialog(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onDismiss() }
        ) {
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun ClubDrawerContent(
    club: ClubHomeDto?,
    onNavigateSettings: () -> Unit,
    onNavigateMessages: () -> Unit,
    onNavigateRequests: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header with club info
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(RedPrimary, Color(0xFFD32F2F))
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // Club profile image
                if (club != null) {
                    val fullImageUrl = club.imageUrl?.let { imageUrl ->
                        if (imageUrl.startsWith("http")) {
                            imageUrl
                        } else {
                            val baseUrl = Constants.BASE_URL.replace("/api/", "")
                            if (imageUrl.startsWith("/")) "$baseUrl$imageUrl" else "$baseUrl/$imageUrl"
                        }
                    }

                    if (fullImageUrl != null) {
                        Image(
                            painter = rememberAsyncImagePainter(fullImageUrl),
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = club.name.take(1),
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = club.name,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${club.totalMembers} Members",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Menu items
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text("Club Settings") },
            selected = false,
            onClick = onNavigateSettings,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Send, contentDescription = null) },
            label = { Text("Messages") },
            selected = false,
            onClick = onNavigateMessages,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        NavigationDrawerItem(
            icon = { Icon(androidx.compose.material.icons.Icons.Default.Person, contentDescription = null) },
            label = { Text("Join Requests") },
            selected = false,
            onClick = onNavigateRequests,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        NavigationDrawerItem(
            icon = {
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            label = { Text("Logout", color = MaterialTheme.colorScheme.error) },
            selected = false,
            onClick = onLogout,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}
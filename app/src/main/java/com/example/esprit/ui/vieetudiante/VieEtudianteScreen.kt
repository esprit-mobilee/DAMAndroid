package com.example.esprit.ui.vieetudiante

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.esprit.model.Club
import com.example.esprit.model.Event
import com.example.esprit.model.Role
import com.example.esprit.ui.shared.ProfileViewModel
import com.example.esprit.util.UiState
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

import com.example.esprit.R
import com.example.esprit.util.Constants


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VieEtudianteScreen(
    eventsViewModel: EventsViewModel,
    clubsViewModel: ClubsViewModel,
    onAddEvent: () -> Unit,
    onAddClub: () -> Unit,
    onEventClick: (String) -> Unit,
    onClubClick: (String) -> Unit,
    onEditEvent: (String) -> Unit,
    onEditClub: (String) -> Unit,
    onDeleteEvent: (String) -> Unit,
    onDeleteClub: (String) -> Unit,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) { profileViewModel.loadMe() }
    val profileState by profileViewModel.uiState.collectAsState()

    val roles = profileState.user?.roles ?: emptyList()
    val singleRole = profileState.user?.role?.uppercase()

    val isAdmin = roles.contains(Role.ADMIN) || singleRole == "ADMIN"
    val isPresident = roles.contains(Role.PRESIDENT) || singleRole == "PRESIDENT"

    // ✅ keep selected tab when navigating to forms and back
    val selectedTab = rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Événements", "Clubs")

    Scaffold(
        floatingActionButton = {
            when (selectedTab.intValue) {
                0 -> if (isAdmin || isPresident) AddButton(onAddEvent)
                1 -> if (isAdmin) AddButton(onAddClub)
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {

            Text(
                "Vie Étudiante",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp)
            )

            TabRow(selectedTabIndex = selectedTab.intValue) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab.intValue == index,
                        onClick = { selectedTab.intValue = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab.intValue) {
                0 -> EventsTab(
                    viewModel = eventsViewModel,
                    onEventClick = onEventClick,
                    onEditEvent = onEditEvent,
                    onDeleteEvent = onDeleteEvent,
                    canModify = isAdmin || isPresident
                )

                1 -> ClubsTab(
                    viewModel = clubsViewModel,
                    onClubClick = onClubClick,
                    onEditClub = onEditClub,
                    onDeleteClub = onDeleteClub,
                    canModify = isAdmin
                )
            }
        }
    }
}

@Composable
private fun AddButton(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = Color(0xFFB388FF),
        shape = CircleShape
    ) {
        Icon(Icons.Default.Add, contentDescription = "Add")
    }
}

// -----------------------------------------------------
//  EVENTS TAB
// -----------------------------------------------------
@Composable
private fun EventsTab(
    viewModel: EventsViewModel,
    onEventClick: (String) -> Unit,
    onEditEvent: (String) -> Unit,
    onDeleteEvent: (String) -> Unit,
    canModify: Boolean
) {
    val uiState by viewModel.events.collectAsState()

    when (uiState) {
        is UiState.Loading ->
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))

        is UiState.Error ->
            Text("Erreur de chargement", modifier = Modifier.padding(16.dp))

        is UiState.Success -> {
            val events = (uiState as UiState.Success<List<Event>>).data

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(events) { event ->
                    EventCard(
                        event = event,
                        onClick = onEventClick,
                        onEditClick = onEditEvent,
                        onDeleteClick = onDeleteEvent,
                        canModify = canModify
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------
//  EVENT CARD WITH DELETE DIALOG
// -----------------------------------------------------
/*@Composable
private fun EventCard(
    event: Event,
    onClick: (String) -> Unit,
    onEditClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    canModify: Boolean
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer l'événement") },
            text = { Text("Êtes-vous sûr de vouloir supprimer cet événement ?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    event.id?.let(onDeleteClick)
                }) { Text("Supprimer") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Annuler") }
            }
        )
    }

    Card(
        onClick = { event.id?.let(onClick) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(Modifier.padding(14.dp)) {

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(event.title ?: "Sans titre", style = MaterialTheme.typography.titleMedium)
                    Text(event.category ?: "", color = Color.Gray)
                    Text(event.description ?: "", color = Color.DarkGray)
                }

                if (canModify) {
                    IconButton(onClick = { event.id?.let(onEditClick) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Modifier")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color.Red)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Row {
                Tag(event.location ?: "Lieu inconnu")
                Spacer(Modifier.width(6.dp))
                Tag(event.date ?: "Date inconnue")
            }
        }
    }
}*/

@Composable
private fun EventCard(
    event: Event,
    onClick: (String) -> Unit,
    onEditClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    canModify: Boolean
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer l'événement") },
            text = { Text("Êtes-vous sûr de vouloir supprimer cet événement ?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    event.id?.let(onDeleteClick)
                }) { Text("Supprimer") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Annuler") }
            }
        )
    }

    // ---------- Construire l’URL complète de l’image ----------
    val fullImageUrl = event.imageUrl?.let { relative ->
        com.example.esprit.util.Constants.BASE_URL
            .removeSuffix("api/")          // "http://IP:3000/"
            .plus(relative.trimStart('/')) // "uploads/events/xxx.jpg"
    }

    Card(
        onClick = { event.id?.let(onClick) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(Modifier.padding(14.dp)) {

            // ---------- Bandeau image en haut (optionnel) ----------
            if (!fullImageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = fullImageUrl,
                    contentDescription = "Image de l'événement",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE0E0E0)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(8.dp))
            }

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        event.title ?: "Sans titre",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(event.category ?: "", color = Color.Gray)
                    Text(event.description ?: "", color = Color.DarkGray)
                }

                if (canModify) {
                    IconButton(onClick = { event.id?.let(onEditClick) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Modifier")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color.Red)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Row {
                Tag(event.location ?: "Lieu inconnu")
                Spacer(Modifier.width(6.dp))
                Tag(event.date ?: "Date inconnue")
            }
        }
    }
}



// -----------------------------------------------------
//  CLUB TAB
// -----------------------------------------------------
@Composable
private fun ClubsTab(
    viewModel: ClubsViewModel,
    onClubClick: (String) -> Unit,
    onEditClub: (String) -> Unit,
    onDeleteClub: (String) -> Unit,
    canModify: Boolean
) {
    val uiState by viewModel.clubs.collectAsState()

    when (uiState) {
        is UiState.Loading ->
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))

        is UiState.Error ->
            Text("Erreur de chargement", modifier = Modifier.padding(16.dp))

        is UiState.Success -> {
            val clubs = (uiState as UiState.Success<List<Club>>).data

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(clubs) { club ->
                    ClubCard(
                        club = club,
                        onClick = onClubClick,
                        onEditClick = onEditClub,
                        onDeleteClick = onDeleteClub,
                        canModify = canModify
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------
//  CLUB CARD WITH DELETE
// -----------------------------------------------------

/*
@Composable
private fun ClubCard(
    club: Club,
    onClick: (String) -> Unit,
    onEditClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    canModify: Boolean
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var lastError by remember { mutableStateOf<String?>(null) }

    // ---------- Confirmation de suppression ----------
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer le club") },
            text = { Text("Êtes-vous sûr de vouloir supprimer ce club ?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    club.id?.let(onDeleteClick)
                }) { Text("Supprimer") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Annuler") }
            }
        )
    }

    // ---------- Construction de l’URL complète ----------
    val fullImageUrl = club.imageUrl?.let { relative ->
        // BASE_URL = "http://192.168.1.105:3000/api/"
        Constants.BASE_URL
            .removeSuffix("api/")          // -> http://192.168.1.105:3000/
            .plus(relative.trimStart('/')) // -> /uploads/clubs/xxx.png
    }

    Card(
        onClick = { club.id?.let(onClick) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(Modifier.padding(14.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ---------- AVATAR / IMAGE ----------
                if (!fullImageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(fullImageUrl)
                            .crossfade(true)
                            .listener(
                                onError = { _, result ->
                                    lastError = result.throwable.message
                                }
                            )
                            .build(),
                        contentDescription = "Logo du club",
                        modifier = Modifier
                            .size(56.dp) // 👈 plus grand
                            .clip(CircleShape)
                            .background(Color(0xFFE0E0E0)),
                        contentScale = ContentScale.Crop      // 👈 on découpe au centre
                    )
                } else {
                    // Fallback: initial du nom
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        val initial = club.name?.firstOrNull()?.uppercaseChar() ?: '?'
                        Text(initial.toString())
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        club.name ?: "Sans nom",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        club.description ?: "",
                        color = Color.DarkGray
                    )
                }

                if (canModify) {
                    IconButton(onClick = { club.id?.let(onEditClick) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Modifier")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Supprimer",
                            tint = Color.Red
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            club.tags?.forEach { tag ->
                Tag(tag)
                Spacer(Modifier.height(4.dp))
            }

            // ---------- Debug visuel (temporaire) ----------
            Text(
                text = "url = ${fullImageUrl ?: "null"}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            lastError?.let {
                Text(
                    text = "error = $it",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Red
                )
            }
        }
    }
}*/

@Composable
private fun ClubCard(
    club: Club,
    onClick: (String) -> Unit,
    onEditClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    canModify: Boolean
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer le club") },
            text = { Text("Êtes-vous sûr de vouloir supprimer ce club ?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    club.id?.let(onDeleteClick)
                }) { Text("Supprimer") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Annuler") }
            }
        )
    }

    // Build final image URL
    val fullImageUrl = club.imageUrl?.let { relative ->
        com.example.esprit.util.Constants.BASE_URL
            .removeSuffix("api/")
            .plus(relative.trimStart('/'))
    }

    Card(
        onClick = { club.id?.let(onClick) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(Modifier.padding(14.dp)) {

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // ------------------------------------------------------
                // IMAGE ONLY — show nothing if no image
                // ------------------------------------------------------
                if (!fullImageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = fullImageUrl,
                        contentDescription = "Club Logo",
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                } else {
                    Spacer(modifier = Modifier.width(0.dp)) // nothing
                }

                Column(Modifier.weight(1f)) {
                    Text(
                        club.name ?: "Sans nom",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        club.description ?: "",
                        color = Color.DarkGray
                    )
                }

                if (canModify) {
                    IconButton(onClick = { club.id?.let(onEditClick) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Modifier")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color.Red)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            club.tags?.forEach { tag ->
                Tag(tag)
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}


@Composable
private fun Tag(text: String) {
    Box(
        modifier = Modifier
            .background(Color(0xFFF1F1F1), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text)
    }
}

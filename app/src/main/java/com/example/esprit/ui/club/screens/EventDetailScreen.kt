package com.example.esprit.ui.club.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.esprit.model.club.ClubEventDto
import com.example.esprit.ui.club.ClubEventsViewModel
import com.example.esprit.ui.components.LocationMapViewer
import com.example.esprit.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    viewModel: ClubEventsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    var showJoinDialog by remember { mutableStateOf(false) }
    var showManageDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    val event = state.events.find { it.id == eventId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détails de l'événement") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    event?.let {
                        TextButton(onClick = { onEdit(it.id) }) {
                            Text("Modifier")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (event == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("Événement introuvable")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                event.imageUrl?.let { imageUrl ->
                    val fullImageUrl = if (imageUrl.startsWith("http")) {
                        imageUrl
                    } else {
                        val baseUrl = Constants.BASE_URL.replace("/api/", "")
                        "$baseUrl$imageUrl"
                    }

                    Image(
                        painter = rememberAsyncImagePainter(fullImageUrl),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfoRow("Date", event.date ?: "Non spécifiée")
                        event.description?.let {
                            InfoRow("Description", it)
                        }
                        event.category?.let {
                            InfoRow("Catégorie", it)
                        }
                    }
                }

                // Location Section
                Text(
                    text = "Lieu",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                LocationMapViewer(
                    location = event.location,
                    modifier = Modifier.fillMaxWidth(),
                    height = 300.dp
                )

                if (event.registrations.isNotEmpty()) {
                    Text(
                        text = "${event.registrations.size} Inscription(s)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (event.registrationOpen) {
                    Button(
                        onClick = { showJoinDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("S'inscrire à l'événement")
                    }
                } else {
                    OutlinedButton(
                        onClick = { },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Inscriptions fermées")
                    }
                }

                // Bouton pour gérer les inscriptions (Affiché pour tout le monde pour l'instant, mais l'API protégera)
                // Idéalement, on vérifierait si l'utilisateur est le créateur
                OutlinedButton(
                    onClick = { showManageDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Voir les participants")
                }
            }
        }
    }

    if (showJoinDialog && event != null) {
        com.example.esprit.ui.components.JoinEventDialog(
            event = event,
            onDismiss = { showJoinDialog = false },
            onJoin = { answers ->
                val body = mutableMapOf<String, Any?>()
                if (answers.isNotEmpty()) {
                    body["answers"] = answers
                }
                viewModel.viewModelScope.launch {
                    val res = viewModel.joinEvent(event.id, body)
                    if (res is com.example.esprit.util.UiState.Success) {
                        viewModel.load() // Reload to update count
                        showJoinDialog = false
                    }
                }
            }
        )
    }

    if (showManageDialog && event != null) {
        ManageRegistrationsDialog(
            eventId = event.id,
            viewModel = viewModel,
            onDismiss = { showManageDialog = false }
        )
    }
}


@Composable
fun ManageRegistrationsDialog(
    eventId: String,
    viewModel: ClubEventsViewModel,
    onDismiss: () -> Unit
) {
    // Load registrations locally or use passed ones? Better fetch fresh.
    var registrations by remember { mutableStateOf<List<com.example.esprit.model.club.EventRegistrationDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(eventId) {
        val res = viewModel.getRegistrations(eventId)
        if (res is com.example.esprit.util.UiState.Success) {
            registrations = res.data
        }
        loading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.95f),
        title = { Text("Gérer les inscriptions") },
        text = {
            if (loading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (registrations.isEmpty()) {
                Text("Aucune inscription pour le moment.")
            } else {
                androidx.compose.foundation.lazy.LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(registrations.size) { index ->
                        val reg = registrations[index]
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (reg.status == "accepted") Color(0xFFE8F5E9)
                                else if (reg.status == "rejected") Color(0xFFFFEBEE)
                                else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, Color.LightGray)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(reg.name ?: "Inconnu", fontWeight = FontWeight.Bold)
                                    Text(reg.status ?: "pending", style = MaterialTheme.typography.labelSmall)
                                }
                                reg.email?.let { Text(it, style = MaterialTheme.typography.bodySmall) }

                                if (!reg.answers.isNullOrEmpty()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text("Réponses :", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    reg.answers.forEach { ans ->
                                        Text("- ${ans.question}: ${ans.answer}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }

                                if (reg.status == "pending") {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        TextButton(
                                            onClick = {
                                                viewModel.viewModelScope.launch {
                                                    viewModel.rejectRegistration(eventId, reg.userId ?: "")
                                                    // Refresh
                                                    val res = viewModel.getRegistrations(eventId)
                                                    if (res is com.example.esprit.util.UiState.Success) registrations = res.data
                                                }
                                            }
                                        ) { Text("Refuser", color = Color.Red) }
                                        TextButton(
                                            onClick = {
                                                viewModel.viewModelScope.launch {
                                                    viewModel.approveRegistration(eventId, reg.userId ?: "")
                                                    // Refresh
                                                    val res = viewModel.getRegistrations(eventId)
                                                    if (res is com.example.esprit.util.UiState.Success) registrations = res.data
                                                }
                                            }
                                        ) { Text("Accepter", color = Color(0xFF2E7D32)) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Fermer") }
        }
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
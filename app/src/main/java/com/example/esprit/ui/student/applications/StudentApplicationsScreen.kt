package com.example.esprit.ui.student.applications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import android.content.Intent
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentApplicationsScreen(
    currentUserId: String,
    onBack: () -> Unit,
    onApplicationClick: (String) -> Unit,
    viewModel: StudentApplicationsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    
    // State for search and filter
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<String?>(null) } // null = All
    
    // State for delete confirmation dialog
    var showDeleteDialog by remember { mutableStateOf(false) }
    var applicationToDelete by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUserId) {
        viewModel.loadApplications(currentUserId)
    }

    // Filter logic
    val filteredApplications = remember(state.applications, searchQuery, selectedStatus) {
        state.applications.filter { app ->
            val matchesSearch = (app.internshipId?.title?.contains(searchQuery, ignoreCase = true) == true) ||
                                (app.internshipId?.company?.contains(searchQuery, ignoreCase = true) == true)
            val matchesStatus = selectedStatus == null || app.status == selectedStatus
            matchesSearch && matchesStatus
        }
    }
    
    // Count applications by status
    val totalCount = state.applications.size
    val acceptedCount = state.applications.count { it.status == "accepted" }
    val rejectedCount = state.applications.count { it.status == "rejected" }
    val pendingCount = state.applications.count { it.status == "pending" || it.status == null }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmer la suppression") },
            text = { Text("Voulez-vous vraiment supprimer cette candidature ?") },
            confirmButton = {
                Button(
                    onClick = {
                        applicationToDelete?.let { id ->
                            viewModel.deleteApplication(id, currentUserId)
                        }
                        showDeleteDialog = false
                        applicationToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444)
                    )
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
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes candidatures") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Rechercher par nom, poste ou entreprise...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedStatus == null,
                    onClick = { selectedStatus = null },
                    label = { Text("Tous ($totalCount)") }
                )
                FilterChip(
                    selected = selectedStatus == "accepted",
                    onClick = { selectedStatus = if (selectedStatus == "accepted") null else "accepted" },
                    label = { Text("Accepté ($acceptedCount)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFE8F5E9),
                        selectedLabelColor = Color(0xFF2E7D32)
                    )
                )
                FilterChip(
                    selected = selectedStatus == "rejected",
                    onClick = { selectedStatus = if (selectedStatus == "rejected") null else "rejected" },
                    label = { Text("Refusé ($rejectedCount)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFFEBEE),
                        selectedLabelColor = Color(0xFFD32F2F)
                    )
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    
                    state.error != null -> {
                        Text(
                            text = state.error ?: "Erreur",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    
                    filteredApplications.isEmpty() -> {
                        Text(
                            text = if (state.applications.isEmpty()) "Aucune candidature" else "Aucun résultat trouvé",
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = filteredApplications,
                                key = { it.id ?: it.hashCode() }
                            ) { application ->
                                ApplicationCard(
                                    application = application,
                                    onClick = {
                                        application.id?.let { onApplicationClick(it) }
                                    },
                                    onDelete = {
                                        application.id?.let { id ->
                                            applicationToDelete = id
                                            showDeleteDialog = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun ApplicationCard(
    application: com.example.esprit.model.Application,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val offer = application.internshipId
    
    // Couleurs et styles selon le statut
    val (statusColor, statusBgColor, statusText, statusIcon) = when (application.status) {
        "accepted" -> Quadruple(
            Color(0xFF2E7D32),
            Color(0xFFE8F5E9),
            "Accepté",
            "✓"
        )
        "rejected" -> Quadruple(
            Color(0xFFD32F2F),
            Color(0xFFFFEBEE),
            "Refusé",
            "✗"
        )
        else -> Quadruple(
            Color(0xFFFF9800),
            Color(0xFFFFF3E0),
            "En attente",
            "⏱"
        )
    }
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header avec gradient et statut
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(
                                statusColor.copy(alpha = 0.1f),
                                statusColor.copy(alpha = 0.05f)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Statut badge moderne
                    Surface(
                        color = statusBgColor,
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = statusIcon,
                                style = MaterialTheme.typography.labelLarge,
                                color = statusColor,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = statusText,
                                color = statusColor,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    // Delete icon button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Supprimer",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            // Contenu principal
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                offer?.let {
                    // Titre du poste avec icône
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFFE3F2FD),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "💼",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = it.title,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 2
                            )
                        }
                    }
                    
                    // Entreprise et localisation
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(start = 48.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🏢",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = it.company,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFF424242),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    it.locationAddress?.let { address ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 48.dp)
                        ) {
                            Text(
                                text = "📍",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = address,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
                
                // Interview Section (if scheduled)
                application.interviewScheduledAt?.let { scheduledAt ->
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = Color(0xFFE0E0E0)
                    )
                    
                    InterviewSection(
                        scheduledAt = scheduledAt,
                        duration = application.interviewDuration,
                        meetingLink = application.interviewMeetingLink,
                        notes = application.interviewNotes
                    )
                }
            }
        }
    }
}

@Composable
private fun InterviewSection(
    scheduledAt: String,
    duration: Int?,
    meetingLink: String?,
    notes: String?
) {
    val context = LocalContext.current
    
    // Format the date/time
    val formattedDateTime = try {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        isoFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val date = isoFormat.parse(scheduledAt)
        
        val displayFormat = SimpleDateFormat("dd/MM/yyyy 'à' HH:mm", Locale.getDefault())
        date?.let { displayFormat.format(it) } ?: scheduledAt
    } catch (e: Exception) {
        // Fallback: try simpler format
        try {
            val simpleFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
            val date = simpleFormat.parse(scheduledAt)
            val displayFormat = SimpleDateFormat("dd/MM/yyyy 'à' HH:mm", Locale.getDefault())
            date?.let { displayFormat.format(it) } ?: scheduledAt
        } catch (e2: Exception) {
            scheduledAt
        }
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Interview header with green badge
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(0xFFE8F5E9),
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✓",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Entretien planifié",
                        color = Color(0xFF2E7D32),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Date and time
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp)
        ) {
            Text(
                text = "📅",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Date : $formattedDateTime",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        
        // Duration if available
        duration?.let {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Text(
                    text = "⏱",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Durée : $it minutes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
        
        // Notes if available
        notes?.let {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Text(
                    text = "📝",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
        
        // Join interview button
        meetingLink?.let { link ->
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1976D2)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VideoCall,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Rejoindre l'entretien",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Helper data class pour les tuples
private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)


<<<<<<< HEAD
package com.example.esprit.ui.admin.applications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.rotate
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminApplicationsListScreen(
    onBack: () -> Unit,
    onApplicationClick: (String) -> Unit,
    viewModel: AdminApplicationsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    
    // State for search and filter
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<String?>(null) } // null = All
    
    // State for delete confirmation dialog
    var showDeleteDialog by remember { mutableStateOf(false) }
    var applicationToDelete by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        viewModel.loadAllApplications()
    }
    
    // Filter logic
    val filteredApplications = remember(state.applications, searchQuery, selectedStatus) {
        state.applications.filter { app ->
            val userName = viewModel.getUserName(app.userId)
            val matchesSearch = (app.internshipId?.title?.contains(searchQuery, ignoreCase = true) == true) ||
                                (app.internshipId?.company?.contains(searchQuery, ignoreCase = true) == true) ||
                                (userName.contains(searchQuery, ignoreCase = true))
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
                            viewModel.deleteApplication(id)
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
                title = { Text("Candidatures") },
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
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = state.error ?: "Erreur",
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(onClick = { viewModel.loadAllApplications() }) {
                                Text("Réessayer")
                            }
                        }
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
                                val userName = viewModel.getUserName(application.userId)
                                AdminApplicationCard(
                                    application = application,
                                    userName = userName,
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
private fun AdminApplicationCard(
    application: com.example.esprit.model.Application,
    userName: String,
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
                    // Avatar et nom de l'étudiant
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = Color(0xFF0077B5),
                            shadowElevation = 2.dp
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userName.take(1).uppercase(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF212121)
                        )
                    }
                    
                    // Statut badge et delete button
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Statut badge moderne
                        Surface(
                            color = statusBgColor,
                            shape = RoundedCornerShape(12.dp),
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = statusIcon,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = statusColor,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = statusText,
                                    color = statusColor,
                                    style = MaterialTheme.typography.labelMedium,
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

=======
package com.example.esprit.ui.admin.applications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.rotate
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminApplicationsListScreen(
    onBack: () -> Unit,
    onApplicationClick: (String) -> Unit,
    viewModel: AdminApplicationsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    
    // State for search and filter
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<String?>(null) } // null = All
    
    // State for delete confirmation dialog
    var showDeleteDialog by remember { mutableStateOf(false) }
    var applicationToDelete by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        viewModel.loadAllApplications()
    }
    
    // Filter logic
    val filteredApplications = remember(state.applications, searchQuery, selectedStatus) {
        state.applications.filter { app ->
            val userName = viewModel.getUserName(app.userId)
            val matchesSearch = (app.internshipId?.title?.contains(searchQuery, ignoreCase = true) == true) ||
                                (app.internshipId?.company?.contains(searchQuery, ignoreCase = true) == true) ||
                                (userName.contains(searchQuery, ignoreCase = true))
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
                            viewModel.deleteApplication(id)
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
                title = { Text("Candidatures") },
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
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = state.error ?: "Erreur",
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(onClick = { viewModel.loadAllApplications() }) {
                                Text("Réessayer")
                            }
                        }
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
                                val userName = viewModel.getUserName(application.userId)
                                AdminApplicationCard(
                                    application = application,
                                    userName = userName,
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
private fun AdminApplicationCard(
    application: com.example.esprit.model.Application,
    userName: String,
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
                    // Avatar et nom de l'étudiant
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = Color(0xFF0077B5),
                            shadowElevation = 2.dp
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userName.take(1).uppercase(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF212121)
                        )
                    }
                    
                    // Statut badge et delete button
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Statut badge moderne
                        Surface(
                            color = statusBgColor,
                            shape = RoundedCornerShape(12.dp),
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = statusIcon,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = statusColor,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = statusText,
                                    color = statusColor,
                                    style = MaterialTheme.typography.labelMedium,
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

>>>>>>> origin/messaging-announcement

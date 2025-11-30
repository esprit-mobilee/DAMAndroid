package com.example.esprit.ui.admin.applications

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
    
    LaunchedEffect(Unit) {
        viewModel.loadAllApplications()
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
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
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
                
                state.applications.isEmpty() -> {
                    Text(
                        text = "Aucune candidature pour l'instant",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.applications) { application ->
                            val userName = viewModel.getUserName(application.userId)
                            AdminApplicationCard(
                                application = application,
                                userName = userName,
                                onClick = {
                                    application.id?.let { onApplicationClick(it) }
                                }
                            )
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
    onClick: () -> Unit
) {
    val offer = application.internshipId
    val statusColor = when (application.status) {
        "accepted" -> Color(0xFF2E7D32) // Vert
        "rejected" -> Color(0xFFD32F2F) // Rouge
        else -> Color(0xFFFF9800) // Orange pour pending
    }
    
    val statusText = when (application.status) {
        "accepted" -> "Accepté"
        "rejected" -> "Refusé"
        else -> "En attente"
    }
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar circulaire
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = Color(0xFFE3F2FD)
            ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.take(1).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0077B5)
                        )
                    }
                }
            
            // Contenu
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Nom de l'étudiant
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                
                // Poste et entreprise
                offer?.let {
                    Text(
                        text = it.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = "${it.company} • ${it.location ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                
                // Statut badge
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = statusColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Flèche
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .rotate(180f),
                tint = Color.Gray
            )
        }
    }
}


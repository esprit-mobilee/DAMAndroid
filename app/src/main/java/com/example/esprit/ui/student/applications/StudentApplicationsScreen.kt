package com.example.esprit.ui.student.applications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentApplicationsScreen(
    currentUserId: String,
    onBack: () -> Unit,
    onApplicationClick: (String) -> Unit,
    viewModel: StudentApplicationsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    
    LaunchedEffect(currentUserId) {
        viewModel.loadApplications(currentUserId)
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
                    Text(
                        text = state.error ?: "Erreur",
                        modifier = Modifier.align(Alignment.Center)
                    )
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
                            ApplicationCard(
                                application = application,
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
private fun ApplicationCard(
    application: com.example.esprit.model.Application,
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Titre et entreprise
            offer?.let {
                Text(
                    text = it.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = it.company,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            
            // Statut
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
    }
}


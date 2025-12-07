package com.example.esprit.ui.demande

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.esprit.ui.demande.components.DocumentRequestCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentRequestListScreen(
    viewModel: DocumentRequestListViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onCreateRequest: () -> Unit = {},
    onRequestClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadRequests()
    }

    LaunchedEffect(uiState.error, uiState.deleteError) {
        val message = uiState.error ?: uiState.deleteError
        if (message != null) {
            snackBarHostState.showSnackbar(message)
            viewModel.clearErrors()
        }
    }

    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Mes demandes",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Rafraîchir")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateRequest,
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Créer une demande",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { innerPadding ->
        when {
            uiState.isLoading && uiState.requests.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 4.dp
                        )
                        Text(
                            "Chargement de vos demandes...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            uiState.requests.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        // Large icon with gradient background
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            MaterialTheme.colorScheme.secondaryContainer
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Text(
                            "Aucune demande pour le moment",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            "Cliquez sur le bouton + pour créer votre première demande",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    // Statistics Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 4.dp
                    ) {
                        StatsCard(
                            totalRequests = uiState.requests.size,
                            approvedCount = uiState.requests.count { it.status == "APPROVED" },
                            pendingCount = uiState.requests.count { it.status == "PENDING" },
                            rejectedCount = uiState.requests.count { it.status == "REJECTED" }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Enhanced search field
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2.dp
                    ) {
                        OutlinedTextField(
                            value = uiState.searchYear,
                            onValueChange = viewModel::updateSearchYear,
                            label = { Text("Rechercher par année") },
                            placeholder = { Text("Ex: 2024") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Rechercher",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                AnimatedVisibility(
                                    visible = uiState.searchYear.isNotBlank(),
                                    enter = fadeIn() + slideInVertically(),
                                    exit = fadeOut() + slideOutVertically()
                                ) {
                                    IconButton(onClick = { viewModel.updateSearchYear("") }) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Effacer",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Liste des demandes filtrées
                    if (uiState.filteredRequests.isEmpty() && uiState.searchYear.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Text(
                                    "Aucune demande trouvée",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "pour l'année \"${uiState.searchYear}\"",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.filteredRequests, key = { it.id }) { request ->
                                DocumentRequestCard(
                                    request = request,
                                    hasFile = uiState.hasFile(request.id),
                                    onClick = {
                                        Log.d("NAV", "Navigating to detail: ${request.id}")
                                        onRequestClick(request.id)
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
fun StatsCard(
    totalRequests: Int,
    approvedCount: Int,
    pendingCount: Int,
    rejectedCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mes statistiques",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = totalRequests.toString(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatItem(
                    icon = Icons.Default.CheckCircle,
                    label = "Approuvées",
                    count = approvedCount,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    icon = Icons.Default.HourglassEmpty,
                    label = "En attente",
                    count = pendingCount,
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    icon = Icons.Default.Cancel,
                    label = "Rejetées",
                    count = rejectedCount,
                    color = Color(0xFFF44336),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

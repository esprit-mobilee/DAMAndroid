package com.example.esprit.ui.admin.requests

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.esprit.model.DocumentRequestItem
import com.example.esprit.util.DateFormatter
import com.example.esprit.ui.components.StatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDocumentRequestListScreen(
    viewModel: AdminDocumentRequestViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0: En attente, 1: Traité

    LaunchedEffect(Unit) {
        viewModel.loadRequests()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Gestion des demandes",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            // Enhanced Tab Row with custom styling
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                CustomTabRow(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    pendingCount = uiState.requests.count { it.status == "PENDING" },
                    processedCount = uiState.requests.count { it.status != "PENDING" }
                )
            }

            val filteredRequests = uiState.requests.filter {
                if (selectedTab == 0) it.status == "PENDING"
                else it.status != "PENDING"
            }.sortedByDescending { it.createdAt }

            if (uiState.isLoading) {
                Box(
                    Modifier.fillMaxSize(),
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
                            "Chargement des demandes...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (filteredRequests.isEmpty()) {
                EmptyStateView(selectedTab = selectedTab)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredRequests) { request ->
                        EnhancedAdminRequestCard(
                            request = request,
                            onClick = {
                                viewModel.selectRequest(request)
                                onNavigateToDetail(request.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomTabRow(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    pendingCount: Int,
    processedCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CustomTab(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            text = "En attente",
            count = pendingCount,
            icon = Icons.Default.HourglassEmpty,
            modifier = Modifier.weight(1f)
        )
        CustomTab(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            text = "Traités",
            count = processedCount,
            icon = Icons.Default.CheckCircle,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun CustomTab(
    selected: Boolean,
    onClick: () -> Unit,
    text: String,
    count: Int,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) 
            MaterialTheme.colorScheme.primaryContainer 
        else 
            MaterialTheme.colorScheme.surface,
        animationSpec = tween(300)
    )
    
    val contentColor by animateColorAsState(
        targetValue = if (selected) 
            MaterialTheme.colorScheme.onPrimaryContainer 
        else 
            MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(300)
    )

    Surface(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp),
        border = if (selected) null else ButtonDefaults.outlinedButtonBorder,
        shadowElevation = if (selected) 4.dp else 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = contentColor
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = contentColor
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = CircleShape,
                color = if (selected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = count.toString(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) 
                        MaterialTheme.colorScheme.onPrimary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EnhancedAdminRequestCard(
    request: DocumentRequestItem,
    onClick: () -> Unit
) {
    val (icon, iconColor) = getDocumentTypeIconAndColor(request.type)
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon with gradient background
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                iconColor.copy(alpha = 0.2f),
                                iconColor.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = iconColor
                )
            }

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = getDocumentTypeLabel(request.type),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    StatusBadge(status = request.status)
                }
                
                // Student info with icon
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = request.user?.fullName ?: "Inconnu",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Year info with icon
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Année ${request.annee}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Date with icon
                request.createdAt?.let {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = DateFormatter.formatDate(it),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Payment status badge
                Spacer(modifier = Modifier.height(4.dp))
                request.user?.let { user ->
                    PaymentStatusBadge(isPaid = user.inscriptionPaid)
                }

            }
        }
    }
}

@Composable
fun EmptyStateView(selectedTab: Int) {
    Box(
        modifier = Modifier.fillMaxSize(),
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
                    imageVector = if (selectedTab == 0) 
                        Icons.Default.HourglassEmpty 
                    else 
                        Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Text(
                text = if (selectedTab == 0) 
                    "Aucune demande en attente" 
                else 
                    "Aucune demande traitée",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = if (selectedTab == 0)
                    "Les nouvelles demandes apparaîtront ici"
                else
                    "Les demandes approuvées ou rejetées apparaîtront ici",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun getDocumentTypeIconAndColor(type: String): Pair<ImageVector, Color> {
    return when (type.lowercase()) {
        "attestation" -> Pair(Icons.Default.FactCheck, Color(0xFF4CAF50))
        "relevé", "releve" -> Pair(Icons.Default.Assignment, Color(0xFF2196F3))
        "convention" -> Pair(Icons.Default.Description, Color(0xFFFF9800))
        else -> Pair(Icons.Default.Description, Color(0xFF9E9E9E))
    }
}

private fun getDocumentTypeLabel(type: String): String {
    return when (type.lowercase()) {
        "attestation" -> "Attestation"
        "relevé", "releve" -> "Relevé de notes"
        "convention" -> "Convention de stage"
        else -> type.replaceFirstChar { it.uppercase() }
    }
}

@Composable
fun PaymentStatusBadge(isPaid: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isPaid) 
                    Color(0xFF4CAF50).copy(alpha = 0.1f)
                else 
                    Color(0xFFFF5722).copy(alpha = 0.1f)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = if (isPaid) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (isPaid) Color(0xFF4CAF50) else Color(0xFFFF5722)
        )
        Text(
            text = if (isPaid) "Frais payés" else "Frais non payés",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = if (isPaid) Color(0xFF4CAF50) else Color(0xFFFF5722)
        )
    }
}


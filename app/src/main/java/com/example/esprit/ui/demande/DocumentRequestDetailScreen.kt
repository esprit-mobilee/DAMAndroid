package com.example.esprit.ui.demande

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.esprit.model.DocumentRequestItem
import com.example.esprit.util.Constants
import com.example.esprit.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentRequestDetailScreen(
    requestId: String,
    viewModel: DocumentRequestDetailViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onViewFile: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(requestId) {
        Log.d("DetailScreen", "LaunchedEffect triggered with requestId: $requestId")
        viewModel.loadRequest(requestId)
    }
    
    LaunchedEffect(uiState.isLoading, uiState.request, uiState.error) {
        Log.d("DetailScreen", "UI State - isLoading: ${uiState.isLoading}, request: ${uiState.request?.id}, error: ${uiState.error}")
    }

    LaunchedEffect(uiState.error, uiState.deleteError) {
        val message = uiState.error ?: uiState.deleteError ?: uiState.pdfGenerationError
        if (message != null) {
            snackBarHostState.showSnackbar(message)
            viewModel.clearErrors()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Détail de la demande",
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
                    IconButton(onClick = { viewModel.refresh(requestId) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualiser")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(innerPadding),
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
                            "Chargement...",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            uiState.request == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            "Demande introuvable",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .verticalScroll(rememberScrollState())
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Request Info Card
                    RequestInfoCard(request = uiState.request!!)

                    if (uiState.request?.status == "REJECTED" && !uiState.request?.rejectionReason.isNullOrBlank()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        "Motif du refus",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                Text(
                                    uiState.request?.rejectionReason.orEmpty(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    // File Section
                    FileSectionCard(
                        request = uiState.request!!,
                        file = uiState.file,
                        isLoadingFile = uiState.isLoadingFile,
                        isGeneratingPdf = uiState.isGeneratingPdf,
                        generatedPdfUri = uiState.generatedPdfUri,
                        generatePdf = { viewModel.generatePdf() },
                        onViewFile = { url ->
                            if (url != null) {
                                val fullUrl = when {
                                    url.startsWith("http://") || url.startsWith("https://") -> url
                                    url.startsWith("/") -> {
                                        val baseUrl = Constants.BASE_URL.removeSuffix("/")
                                        val cleanPath = url.removePrefix("/")
                                        if (baseUrl.endsWith("/")) "$baseUrl$cleanPath" else "$baseUrl/$cleanPath"
                                    }
                                    else -> {
                                        val baseUrl = Constants.BASE_URL.removeSuffix("/")
                                        if (baseUrl.endsWith("/")) "$baseUrl$url" else "$baseUrl/$url"
                                    }
                                }
                                Log.d("DetailScreen", "Original file URL: $url")
                                Log.d("DetailScreen", "Full file URL: $fullUrl")
                                onViewFile(fullUrl)
                            }
                        }
                    )

                    // Delete Button
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        enabled = !uiState.isDeleting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        if (uiState.isDeleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.width(12.dp))
                            Text("Suppression...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text("Supprimer la demande", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text("Confirmer la suppression")
                }
            },
            text = { 
                Text("Êtes-vous sûr de vouloir supprimer cette demande ? Cette action est irréversible.") 
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteRequest(requestId)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Supprimer", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annuler")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // PDF Success Dialog
    if (uiState.showPdfSuccessDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissPdfSuccessDialog() },
            title = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF4CAF50),
                                        Color(0xFF66BB6A)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PictureAsPdf, 
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                    Text("Attestation générée", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Votre attestation de présence a été générée avec succès.")
                    Text(
                        "Cliquez sur 'Voir' pour ouvrir le PDF.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        viewModel.viewPdf(uiState.generatedPdfUri!!)
                        viewModel.dismissPdfSuccessDialog()
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Voir")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissPdfSuccessDialog() }) {
                    Text("Fermer")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun RequestInfoCard(request: DocumentRequestItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
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
                            Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Column {
                        Text(
                            "Informations de la demande",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            request.type.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                com.example.esprit.ui.components.StatusBadge(request.status)
            }
            
            HorizontalDivider()
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoRowWithIcon(
                    icon = Icons.Default.CalendarToday,
                    label = "Année académique",
                    value = request.annee
                )
                request.createdAt?.let {
                    InfoRowWithIcon(
                        icon = Icons.Default.DateRange,
                        label = "Date de création",
                        value = DateFormatter.formatDate(it)
                    )
                }
                request.updatedAt?.let {
                    InfoRowWithIcon(
                        icon = Icons.Default.Update,
                        label = "Dernière mise à jour",
                        value = DateFormatter.formatDate(it)
                    )
                }
            }
        }
    }
}

@Composable
private fun FileSectionCard(
    request: DocumentRequestItem,
    file: com.example.esprit.model.DocumentFileItem?,
    isLoadingFile: Boolean,
    isGeneratingPdf: Boolean,
    generatedPdfUri: android.net.Uri?,
    onViewFile: (String?) -> Unit,
    generatePdf: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    "Fichier",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            when {
                isLoadingFile -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            "Vérification du fichier...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                file?.url != null -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        file.nomFichier?.let {
                            Text(
                                "📄 $it",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Button(
                            onClick = { onViewFile(file.url) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Télécharger/Ouvrir le fichier", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            when (request.status) {
                                "APPROVED" -> "✅ Votre demande a été approuvée"
                                "REJECTED" -> "❌ Demande rejetée - Aucun fichier disponible"
                                else -> "⏳ Fichier non disponible pour le moment"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        if (request.status == "APPROVED") {
                            Text(
                                "Vous pouvez générer votre attestation de présence.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            
                            if (generatedPdfUri != null) {
                                Button(
                                    onClick = { generatePdf() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Voir mon attestation", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = { generatePdf() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    enabled = !isGeneratingPdf,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (isGeneratingPdf) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text("Génération...", fontWeight = FontWeight.Bold)
                                    } else {
                                        Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Générer mon attestation", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            Text(
                                when (request.status) {
                                    "PENDING" -> "Le fichier sera disponible une fois la demande approuvée."
                                    else -> ""
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRowWithIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

package com.example.esprit.ui.demande

import android.util.Log
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.esprit.model.DocumentRequestItem
import com.example.esprit.util.Constants

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
        val message = uiState.error ?: uiState.deleteError
        if (message != null) {
            snackBarHostState.showSnackbar(message)
            viewModel.clearErrors()
        }
    }

    // Removed auto-back LaunchedEffect - it was causing premature navigation
    // The screen will show "Demande introuvable" if request is null after loading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détail de la demande") },
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
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.request == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Demande introuvable")
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Request Info Card
                    RequestInfoCard(request = uiState.request!!)

                    // File Section
                    FileSectionCard(
                        file = uiState.file,
                        isLoadingFile = uiState.isLoadingFile,
                        onViewFile = { url ->
                            if (url != null) {
                                // Construire l'URL complète si nécessaire
                                val fullUrl = when {
                                    url.startsWith("http://") || url.startsWith("https://") -> {
                                        // URL complète, utiliser telle quelle
                                        url
                                    }
                                    url.startsWith("/") -> {
                                        // Chemin absolu relatif, construire l'URL complète
                                        val baseUrl = Constants.BASE_URL.removeSuffix("/")
                                        val cleanPath = url.removePrefix("/")
                                        if (baseUrl.endsWith("/")) {
                                            "$baseUrl$cleanPath"
                                        } else {
                                            "$baseUrl/$cleanPath"
                                        }
                                    }
                                    else -> {
                                        // Chemin relatif, ajouter BASE_URL
                                        val baseUrl = Constants.BASE_URL.removeSuffix("/")
                                        if (baseUrl.endsWith("/")) {
                                            "$baseUrl$url"
                                        } else {
                                            "$baseUrl/$url"
                                        }
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
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isDeleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.size(8.dp))
                        } else {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.size(8.dp))
                        }
                        Text(if (uiState.isDeleting) "Suppression..." else "Supprimer la demande")
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmer la suppression") },
            text = { Text("Êtes-vous sûr de vouloir supprimer cette demande ? Cette action est irréversible.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteRequest(requestId)
                    }
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
}

@Composable
private fun RequestInfoCard(request: DocumentRequestItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Informations de la demande",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(8.dp))
            
            InfoRow(label = "Type", value = request.type.replaceFirstChar { it.uppercase() })
            InfoRow(label = "Année académique", value = request.annee)
            request.createdAt?.let {
                InfoRow(label = "Date de création", value = it)
            }
            request.updatedAt?.let {
                InfoRow(label = "Dernière mise à jour", value = it)
            }
        }
    }
}

@Composable
private fun FileSectionCard(
    file: com.example.esprit.model.DocumentFileItem?,
    isLoadingFile: Boolean,
    onViewFile: (String?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    "Fichier",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            when {
                isLoadingFile -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Text("Vérification du fichier...")
                    }
                }
                file?.url != null -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        file.nomFichier?.let {
                            Text(
                                "Nom : $it",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Button(
                            onClick = { onViewFile(file.url) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.size(8.dp))
                            Text("Télécharger/Ouvrir le fichier")
                        }
                    }
                }
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Fichier non disponible pour le moment",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Le fichier sera disponible une fois la demande traitée.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}


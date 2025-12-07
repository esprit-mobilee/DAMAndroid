package com.example.esprit.ui.demande

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.esprit.connect.ui.theme.EspritTheme
import com.example.esprit.model.DocumentField
import com.example.esprit.model.DocumentRequestItem
import com.example.esprit.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentRequestScreen(
    viewModel: DocumentRequestViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onOpenHistory: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackBarHostState.showSnackbar(it) }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackBarHostState.showSnackbar(it)
            viewModel.clearSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Nouvelle demande",
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
                    IconButton(onClick = onOpenHistory) {
                        Icon(Icons.Default.History, contentDescription = "Historique")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Type Selector with better labels
            Surface(
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 2.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    TypeSelector(
                        types = uiState.availableTypes,
                        selectedType = uiState.selectedType,
                        onTypeSelected = viewModel::selectType
                    )
                }
            }

            // Year field with validation
            Surface(
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 2.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = uiState.annee,
                        onValueChange = viewModel::updateAnnee,
                        label = { Text("Année académique") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        singleLine = true,
                        enabled = uiState.selectedType.lowercase() != "attestation",
                        isError = uiState.annee.isEmpty() && uiState.error != null,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        supportingText = {
                            if (uiState.selectedType.lowercase() == "attestation") {
                                Text(
                                    "L'année est automatiquement définie à 2025 pour les attestations",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            } else if (uiState.annee.isEmpty()) {
                                Text(
                                    "Format suggéré : 2024 ou 2024-2025",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        },
                        placeholder = { Text("Ex: 2024 ou 2024-2025") }
                    )
                }
            }

            uiState.fields
                .filter { it.name != "annee" }
                .forEach { field ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 2.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            DocumentDynamicField(
                                field = field,
                                value = uiState.formValues[field.name].orEmpty(),
                                onValueChange = { viewModel.updateFieldValue(field.name, it) }
                            )
                        }
                    }
                }


            Button(
                onClick = viewModel::submitRequest,
                enabled = !uiState.isLoading && uiState.annee.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(12.dp))
                }
                Text(
                    if (uiState.isLoading) "Création en cours..." else "Créer la demande",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            uiState.created?.let { request ->
                CreatedDocumentCard(
                    request = request,
                    fileUrl = uiState.createdFileUrl,
                    onOpenHistory = onOpenHistory,
                    onViewFile = null // Navigation will be handled separately if needed
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeSelector(
    types: List<String>,
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedType,
            onValueChange = {},
            readOnly = true,
            label = { Text("Type de document") },
            leadingIcon = {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            types.forEach { type ->
                DropdownMenuItem(
                    text = { Text(getDocumentTypeLabel(type)) },
                    onClick = {
                        onTypeSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun DocumentDynamicField(
    field: DocumentField,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(field.label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = field.type != "paragraph",
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        keyboardOptions = when (field.type) {
            "number" -> KeyboardOptions(keyboardType = KeyboardType.Number)
            "date" -> KeyboardOptions(keyboardType = KeyboardType.Number)
            else -> KeyboardOptions.Default
        },
        supportingText = {
            if (field.required) {
                Text("Champ requis", style = MaterialTheme.typography.labelSmall)
            }
        }
    )
}

@Composable
private fun CreatedDocumentCard(
    request: DocumentRequestItem,
    fileUrl: String?,
    onOpenHistory: () -> Unit,
    onViewFile: (() -> Unit)?
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Success header with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
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
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Text(
                    "Demande créée avec succès !",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            // Details
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Type : ${getDocumentTypeLabel(request.type)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "Année : ${request.annee}",
                    style = MaterialTheme.typography.bodyLarge
                )
                request.createdAt?.let { 
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            DateFormatter.formatDate(it),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            // Show file URL if available
            fileUrl?.let { url ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "📄 Fichier disponible",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            url,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        onViewFile?.let {
                            Button(
                                onClick = it,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Voir le fichier")
                            }
                        }
                    }
                }
            } ?: run {
                Text(
                    "⏳ Fichier en attente de génération",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onOpenHistory) {
                    Text(
                        "Voir l'historique",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
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



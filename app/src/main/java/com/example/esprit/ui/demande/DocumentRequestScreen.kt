package com.example.esprit.ui.demande

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.esprit.connect.ui.theme.EspritTheme
import com.example.esprit.model.DocumentField
import com.example.esprit.model.DocumentRequestItem

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
                title = { Text("Demande de document") },
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
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Type Selector with better labels
            TypeSelector(
                types = uiState.availableTypes,
                selectedType = uiState.selectedType,
                onTypeSelected = viewModel::selectType
            )

            // Year field with validation
            OutlinedTextField(
                value = uiState.annee,
                onValueChange = viewModel::updateAnnee,
                label = { Text("Année académique") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true,
                isError = uiState.annee.isEmpty() && uiState.error != null,
                supportingText = {
                    if (uiState.annee.isEmpty()) {
                        Text(
                            "Format suggéré : 2024 ou 2024-2025",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                placeholder = { Text("Ex: 2024 ou 2024-2025") }
            )

            uiState.fields
                .filter { it.name != "annee" }
                .forEach { field ->
                    DocumentDynamicField(
                        field = field,
                        value = uiState.formValues[field.name].orEmpty(),
                        onValueChange = { viewModel.updateFieldValue(field.name, it) }
                    )
                }


            Button(
                onClick = viewModel::submitRequest,
                enabled = !uiState.isLoading && uiState.annee.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (uiState.isLoading) "Création en cours..." else "Créer la demande")
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
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
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
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "✅ Demande créée avec succès",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            
            Text(
                "Type : ${getDocumentTypeLabel(request.type)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "Année : ${request.annee}",
                style = MaterialTheme.typography.bodyMedium
            )
            request.createdAt?.let { 
                Text(
                    "Créé le : ${formatDate(it)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Show file URL if available
            fileUrl?.let { url ->
                Spacer(Modifier.size(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "📄 Fichier disponible",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            url,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        onViewFile?.let {
                            Button(
                                onClick = it,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Voir le fichier")
                            }
                        }
                    }
                }
            } ?: run {
                Text(
                    "⏳ Fichier en attente de génération",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onOpenHistory) {
                    Text("Voir l'historique")
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

private fun formatDate(dateString: String): String {
    return try {
        dateString.split("T")[0] // Just show the date part
    } catch (e: Exception) {
        dateString
    }
}


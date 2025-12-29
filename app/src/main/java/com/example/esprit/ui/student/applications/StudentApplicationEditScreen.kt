<<<<<<< HEAD
package com.example.esprit.ui.student.applications

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentApplicationEditScreen(
    applicationId: String,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: ApplicationDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    
    var cvUri by remember { mutableStateOf<Uri?>(null) }
    var cvFileName by remember { mutableStateOf("") }
    var coverLetter by remember { mutableStateOf("") }
    var cvError by remember { mutableStateOf<String?>(null) }
    
    // Charger l'application existante
    LaunchedEffect(applicationId) {
        viewModel.loadApplication(applicationId)
    }
    
    // Pré-remplir les champs
    LaunchedEffect(state.application) {
        state.application?.let { app ->
            if (coverLetter.isEmpty()) { // Ne remplir que si vide pour éviter les réinitialisations
                coverLetter = app.coverLetter ?: ""
            }
            if (cvFileName.isEmpty()) { // Ne remplir que si vide
                cvFileName = try {
                    app.cvUrl.substringAfterLast("/").takeIf { it.isNotEmpty() } ?: "CV actuel"
                } catch (e: Exception) {
                    "CV actuel"
                }
            }
        }
    }
    
    // Launcher pour choisir un PDF
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            cvUri = it
            cvFileName = it.toString().substringAfterLast("/")
            cvError = null
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modifier la candidature") },
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
                        Button(onClick = { viewModel.loadApplication(applicationId) }) {
                            Text("Réessayer")
                        }
                    }
                }
                
                state.application != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Section CV
                        Text(
                            text = "Votre CV (PDF)",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        OutlinedButton(
                            onClick = { pdfPickerLauncher.launch("application/pdf") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF0077B5)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Choisir un nouveau fichier PDF")
                        }
                        
                        if (cvFileName.isNotEmpty()) {
                            Text(
                                text = "Fichier actuel: $cvFileName",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        
                        // Section Lettre de motivation
                        Text(
                            text = "Lettre de motivation",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        OutlinedTextField(
                            value = coverLetter,
                            onValueChange = { coverLetter = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 150.dp),
                            placeholder = { Text("Écrivez votre lettre de motivation...") },
                            maxLines = 10
                        )
                        
                        state.error?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        // Bouton Enregistrer
                        Button(
                            onClick = {
                                val newCvUrl = cvUri?.toString()
                                viewModel.updateApplication(
                                    applicationId = applicationId,
                                    cvUrl = newCvUrl,
                                    coverLetter = coverLetter.ifBlank { null },
                                    onSuccess = onSuccess
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = state.application != null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0077B5)
                            )
                        ) {
                            Text("Enregistrer les modifications")
                        }
                    }
                }
            }
        }
    }
}

=======
package com.example.esprit.ui.student.applications

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentApplicationEditScreen(
    applicationId: String,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: ApplicationDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    
    var cvUri by remember { mutableStateOf<Uri?>(null) }
    var cvFileName by remember { mutableStateOf("") }
    var coverLetter by remember { mutableStateOf("") }
    var cvError by remember { mutableStateOf<String?>(null) }
    
    // Charger l'application existante
    LaunchedEffect(applicationId) {
        viewModel.loadApplication(applicationId)
    }
    
    // Pré-remplir les champs
    LaunchedEffect(state.application) {
        state.application?.let { app ->
            if (coverLetter.isEmpty()) { // Ne remplir que si vide pour éviter les réinitialisations
                coverLetter = app.coverLetter ?: ""
            }
            if (cvFileName.isEmpty()) { // Ne remplir que si vide
                cvFileName = try {
                    app.cvUrl.substringAfterLast("/").takeIf { it.isNotEmpty() } ?: "CV actuel"
                } catch (e: Exception) {
                    "CV actuel"
                }
            }
        }
    }
    
    // Launcher pour choisir un PDF
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            cvUri = it
            cvFileName = it.toString().substringAfterLast("/")
            cvError = null
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modifier la candidature") },
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
                        Button(onClick = { viewModel.loadApplication(applicationId) }) {
                            Text("Réessayer")
                        }
                    }
                }
                
                state.application != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Section CV
                        Text(
                            text = "Votre CV (PDF)",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        OutlinedButton(
                            onClick = { pdfPickerLauncher.launch("application/pdf") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF0077B5)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Choisir un nouveau fichier PDF")
                        }
                        
                        if (cvFileName.isNotEmpty()) {
                            Text(
                                text = "Fichier actuel: $cvFileName",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        
                        // Section Lettre de motivation
                        Text(
                            text = "Lettre de motivation",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        OutlinedTextField(
                            value = coverLetter,
                            onValueChange = { coverLetter = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 150.dp),
                            placeholder = { Text("Écrivez votre lettre de motivation...") },
                            maxLines = 10
                        )
                        
                        state.error?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        // Bouton Enregistrer
                        Button(
                            onClick = {
                                val newCvUrl = cvUri?.toString()
                                viewModel.updateApplication(
                                    applicationId = applicationId,
                                    cvUrl = newCvUrl,
                                    coverLetter = coverLetter.ifBlank { null },
                                    onSuccess = onSuccess
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = state.application != null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0077B5)
                            )
                        ) {
                            Text("Enregistrer les modifications")
                        }
                    }
                }
            }
        }
    }
}

>>>>>>> origin/messaging-announcement

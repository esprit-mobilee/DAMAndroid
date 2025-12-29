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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import com.example.esprit.ui.shared.internships.InternshipApplicationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentApplyScreen(
    internshipId: String,
    currentUserId: String,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: InternshipApplicationsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    
    var cvUri by remember { mutableStateOf<Uri?>(null) }
    var cvFileName by remember { mutableStateOf("") }
    var coverLetter by remember { mutableStateOf("") }
    var cvError by remember { mutableStateOf<String?>(null) }
    
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
    
    // Réinitialiser le succès après navigation
    LaunchedEffect(state.applySuccess) {
        if (state.applySuccess) {
            onSuccess()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Postuler au stage") },
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
                    contentColor = Color(0xFF0077B5) // Bleu LinkedIn
                )
            ) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Choisir un fichier PDF")
            }
            
            if (cvFileName.isNotEmpty()) {
                Text(
                    text = "Fichier sélectionné: $cvFileName",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            cvError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
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
            // Bouton Postuler
            val context = LocalContext.current
            Button(
                onClick = {
                    if (cvUri == null) {
                        cvError = "Veuillez sélectionner un CV"
                        return@Button
                    }
                    
                    // Créer un fichier temporaire à partir de l'URI
                    val file = try {
                        val inputStream = context.contentResolver.openInputStream(cvUri!!)
                        val tempFile = java.io.File.createTempFile("cv_upload", ".pdf", context.cacheDir)
                        tempFile.outputStream().use { outputStream ->
                            inputStream?.copyTo(outputStream)
                        }
                        tempFile
                    } catch (e: Exception) {
                        cvError = "Erreur lors de la lecture du fichier: ${e.message}"
                        return@Button
                    }
                    
                    viewModel.apply(
                        userId = currentUserId,
                        internshipId = internshipId,
                        cvUrl = cvUri.toString(),
                        coverLetter = coverLetter.ifBlank { null },
                        cvFile = file
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isApplying,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444) // Rouge
                )
            ) {
                if (state.isApplying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (state.isApplying) "Envoi..." else "Postuler")
            }
            
            state.applyError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import com.example.esprit.ui.shared.internships.InternshipApplicationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentApplyScreen(
    internshipId: String,
    currentUserId: String,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: InternshipApplicationsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    
    var cvUri by remember { mutableStateOf<Uri?>(null) }
    var cvFileName by remember { mutableStateOf("") }
    var coverLetter by remember { mutableStateOf("") }
    var cvError by remember { mutableStateOf<String?>(null) }
    
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
    
    // Réinitialiser le succès après navigation
    LaunchedEffect(state.applySuccess) {
        if (state.applySuccess) {
            onSuccess()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Postuler au stage") },
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
                    contentColor = Color(0xFF0077B5) // Bleu LinkedIn
                )
            ) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Choisir un fichier PDF")
            }
            
            if (cvFileName.isNotEmpty()) {
                Text(
                    text = "Fichier sélectionné: $cvFileName",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            cvError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
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
            // Bouton Postuler
            val context = LocalContext.current
            Button(
                onClick = {
                    if (cvUri == null) {
                        cvError = "Veuillez sélectionner un CV"
                        return@Button
                    }
                    
                    // Créer un fichier temporaire à partir de l'URI
                    val file = try {
                        val inputStream = context.contentResolver.openInputStream(cvUri!!)
                        val tempFile = java.io.File.createTempFile("cv_upload", ".pdf", context.cacheDir)
                        tempFile.outputStream().use { outputStream ->
                            inputStream?.copyTo(outputStream)
                        }
                        tempFile
                    } catch (e: Exception) {
                        cvError = "Erreur lors de la lecture du fichier: ${e.message}"
                        return@Button
                    }
                    
                    viewModel.apply(
                        userId = currentUserId,
                        internshipId = internshipId,
                        cvUrl = cvUri.toString(),
                        coverLetter = coverLetter.ifBlank { null },
                        cvFile = file
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isApplying,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444) // Rouge
                )
            ) {
                if (state.isApplying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (state.isApplying) "Envoi..." else "Postuler")
            }
            
            state.applyError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

>>>>>>> origin/messaging-announcement

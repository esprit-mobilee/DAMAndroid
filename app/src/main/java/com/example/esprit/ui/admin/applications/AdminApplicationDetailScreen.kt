package com.example.esprit.ui.admin.applications

import android.app.DownloadManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.IOException
import kotlin.io.copyTo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.esprit.model.Application
import com.example.esprit.ui.student.applications.ApplicationDetailViewModel
import com.example.esprit.util.Constants
import kotlinx.coroutines.launch
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminApplicationDetailScreen(
    applicationId: String,
    onBack: () -> Unit,
    onScheduleInterview: (String, String) -> Unit = { _, _ -> },  // (applicationId, userEmail)
    viewModel: ApplicationDetailViewModel = hiltViewModel(),
    adminViewModel: AdminApplicationsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(applicationId) {
        viewModel.loadApplication(applicationId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Candidature") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                    ) {
                        val userName = state.userName ?: state.application!!.userId ?: "Étudiant inconnu"
                        val userEmail = state.userEmail ?: ""
                        AdminApplicationDetailContent(
                            application = state.application!!,
                            userName = userName,
                            onScheduleInterview = { onScheduleInterview(applicationId, userEmail) },
                            onDownloadCv = { cvUrl ->
                                scope.launch {
                                    try {
                                        Log.d("AdminAppDetail", "CV URL: $cvUrl")
                                        
                                        val downloadUrl = when {
                                            cvUrl.startsWith("http://") || cvUrl.startsWith("https://") -> {
                                                cvUrl
                                            }
                                            cvUrl.startsWith("content://") -> {
                                                // Pour les URIs de contenu, copier le fichier vers Downloads
                                                try {
                                                    val uri = Uri.parse(cvUrl)
                                                    val contentResolver: ContentResolver = context.contentResolver
                                                    
                                                    // Obtenir le nom du fichier
                                                    val cursor = contentResolver.query(uri, null, null, null, null)
                                                    var fileName = "CV_${userName.replace(" ", "_")}.pdf"
                                                    cursor?.use {
                                                        if (it.moveToFirst()) {
                                                            val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                                                            if (nameIndex >= 0) {
                                                                val name = it.getString(nameIndex)
                                                                if (!name.isNullOrBlank()) {
                                                                    fileName = name
                                                                }
                                                            }
                                                        }
                                                    }
                                                    
                                                    // Copier le fichier vers Downloads
                                                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                                    val destFile = File(downloadsDir, fileName)
                                                    
                                                    contentResolver.openInputStream(uri)?.use { inputStream ->
                                                        FileOutputStream(destFile).use { outputStream ->
                                                            inputStream.copyTo(outputStream)
                                                        }
                                                    }
                                                    
                                                    snackbarHostState.showSnackbar(
                                                        message = "CV téléchargé: $fileName",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                } catch (e: Exception) {
                                                    Log.e("AdminAppDetail", "Error copying content URI", e)
                                                    // Fallback: ouvrir le fichier
                                                    val uri = Uri.parse(cvUrl)
                                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                                        setDataAndType(uri, "application/pdf")
                                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    }
                                                    if (intent.resolveActivity(context.packageManager) != null) {
                                                        context.startActivity(intent)
                                                        snackbarHostState.showSnackbar("Ouverture du CV...")
                                                    } else {
                                                        snackbarHostState.showSnackbar("Erreur: ${e.message}", duration = SnackbarDuration.Long)
                                                    }
                                                }
                                                return@launch
                                            }
                                            else -> {
                                                val baseUrl = Constants.BASE_URL.removeSuffix("api/")
                                                val fullUrl = baseUrl + cvUrl.trimStart('/')
                                                Log.d("AdminAppDetail", "Constructed URL: $fullUrl")
                                                fullUrl
                                            }
                                        }
                                        
                                        // Vérifier que l'URL est valide
                                        if (downloadUrl.isBlank()) {
                                            snackbarHostState.showSnackbar("URL du CV invalide", duration = SnackbarDuration.Long)
                                            return@launch
                                        }
                                        
                                        Log.d("AdminAppDetail", "Starting download from: $downloadUrl")
                                        
                                        // Vérifier que l'URI est valide
                                        val uri = try {
                                            Uri.parse(downloadUrl)
                                        } catch (e: Exception) {
                                            Log.e("AdminAppDetail", "Invalid URI: $downloadUrl", e)
                                            snackbarHostState.showSnackbar("URL invalide: $downloadUrl", duration = SnackbarDuration.Long)
                                            return@launch
                                        }
                                        
                                        // Télécharger le fichier avec DownloadManager
                                        try {
                                            val request = DownloadManager.Request(uri)
                                                .setTitle("CV - $userName")
                                                .setDescription("Téléchargement du CV")
                                                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "CV_${userName.replace(" ", "_")}.pdf")
                                                .setAllowedOverMetered(true)
                                                .setAllowedOverRoaming(true)
                                            
                                            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
                                            if (downloadManager != null) {
                                                val downloadId = downloadManager.enqueue(request)
                                                Log.d("AdminAppDetail", "Download started with ID: $downloadId")
                                                
                                                // Afficher un message de confirmation
                                                snackbarHostState.showSnackbar(
                                                    message = "Téléchargement du CV démarré ✓\nVérifiez vos téléchargements",
                                                    duration = SnackbarDuration.Short
                                                )
                                            } else {
                                                // Fallback: ouvrir dans le navigateur
                                                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                }
                                                if (intent.resolveActivity(context.packageManager) != null) {
                                                    context.startActivity(intent)
                                                    snackbarHostState.showSnackbar("Ouverture du CV dans le navigateur...")
                                                } else {
                                                    snackbarHostState.showSnackbar("Impossible de télécharger le CV", duration = SnackbarDuration.Long)
                                                }
                                            }
                                        } catch (e: Exception) {
                                            Log.e("AdminAppDetail", "Error creating download request", e)
                                            // Fallback: ouvrir dans le navigateur
                                            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            if (intent.resolveActivity(context.packageManager) != null) {
                                                context.startActivity(intent)
                                                snackbarHostState.showSnackbar("Ouverture du CV dans le navigateur...")
                                            } else {
                                                throw e
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e("AdminAppDetail", "Error downloading CV", e)
                                        // Afficher l'erreur à l'utilisateur
                                        snackbarHostState.showSnackbar(
                                            message = "Erreur: ${e.message ?: "Impossible de télécharger le CV"}",
                                            duration = SnackbarDuration.Long
                                        )
                                    }
                                }
                            },
                            onAccept = {
                                adminViewModel.updateApplicationStatus(applicationId, "accepted")
                            },
                            onReject = {
                                adminViewModel.updateApplicationStatus(applicationId, "rejected")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminApplicationDetailContent(
    application: Application,
    userName: String,
    onScheduleInterview: () -> Unit,
    onDownloadCv: (String) -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    val offer = application.internshipId
    val statusColor = when (application.status) {
        "accepted" -> Color(0xFF2E7D32) // Vert
        "rejected" -> Color(0xFFD32F2F) // Rouge
        else -> Color(0xFFFF9800) // Orange
    }
    
    val statusText = when (application.status) {
        "accepted" -> "Accepté"
        "rejected" -> "Refusé"
        else -> "En attente"
    }
    
    val currentStep = when (application.status) {
        "accepted", "rejected" -> 2
        "pending" -> 1
        else -> 0
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Candidat - Style LinkedIn
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar circulaire
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = Color(0xFFFEEBEE)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = Color(0xFFCE0033)
                        )
                    }
                }
                
                // Nom du candidat
                Text(
                    text = userName,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF000000)
                )
                
                // Badge Candidat
                Surface(
                    color = Color(0xFFFEEBEE),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "Candidat",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = Color(0xFFCE0033),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        // Barre de progression
        ProgressSteps(currentStep = currentStep)
        
        // Section Offre de stage
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                offer?.let {
                    Text(
                        text = it.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = it.company,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                    if (!it.locationAddress.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.Gray
                            )
                            Text(
                                text = it.locationAddress!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
        
        // Section CV
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "CV",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                
                Button(
                    onClick = { onDownloadCv(application.cvUrl) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F) // Rouge
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Télécharger le CV")
                }
            }
        }
        
        // Section Lettre de motivation
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Lettre de motivation",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                
                Text(
                    text = "Message du candidat :",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                
                Text(
                    text = application.coverLetter ?: "Aucune lettre de motivation",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        
        // Boutons d'action - Style LinkedIn
        if (application.status != "accepted" && application.status != "rejected") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32) // Vert
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Accepter", fontWeight = FontWeight.Bold)
                }
                
                Button(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F) // Rouge
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Refuser", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Afficher le statut final
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = statusColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Statut: $statusText",
                    modifier = Modifier.padding(16.dp),
                    color = statusColor,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Bouton Planifier un entretien (uniquement si accepté et pas encore d'entretien)
            if (application.status == "accepted" && application.interviewScheduledAt == null) {
                Spacer(Modifier.height(8.dp))
                
                Button(
                    onClick = onScheduleInterview,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2) // Bleu
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Planifier un entretien", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ProgressSteps(currentStep: Int) {
    val steps = listOf("Soumis", "En attente", "Décision finale")
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, step ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    // Point
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = if (index <= currentStep) Color(0xFFCE0033) else Color.Gray,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (index < currentStep) {
                            Text(
                                text = "✓",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall
                            )
                        } else {
                            Text(
                                text = "${index + 1}",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(4.dp))
                    
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (index <= currentStep) Color(0xFFCE0033) else Color.Gray
                    )
                }
                
                // Ligne entre les points
                if (index < steps.size - 1) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(
                                color = if (index < currentStep) Color(0xFFCE0033) else Color.Gray
                            )
                    )
                }
            }
        }
    }
}


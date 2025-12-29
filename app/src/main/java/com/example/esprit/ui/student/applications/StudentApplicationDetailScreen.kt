<<<<<<< HEAD
package com.example.esprit.ui.student.applications

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.esprit.model.Application
import com.example.esprit.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentApplicationDetailScreen(
    applicationId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    viewModel: ApplicationDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(applicationId) {
        viewModel.loadApplication(applicationId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détails") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit(applicationId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Modifier")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Supprimer",
                            tint = Color.Red
                        )
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
                    Column {
                        // Afficher l'erreur en haut si elle existe
                        state.error?.let { error ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Text(
                                    text = error,
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                        
                        ApplicationDetailContent(
                            application = state.application!!,
                            onDownloadCv = { cvUrl ->
                                try {
                                    val uri = when {
                                        // URI de contenu Android (content://...)
                                        cvUrl.startsWith("content://") -> {
                                            Uri.parse(cvUrl)
                                        }
                                        // URL HTTP complète
                                        cvUrl.startsWith("http://") || cvUrl.startsWith("https://") -> {
                                            Uri.parse(cvUrl)
                                        }
                                        // Chemin relatif - construire l'URL complète
                                        else -> {
                                            val baseUrl = Constants.BASE_URL.removeSuffix("api/")
                                            Uri.parse(baseUrl + cvUrl.trimStart('/'))
                                        }
                                    }
                                    
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, "application/pdf")
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    
                                    // Vérifier si une application peut gérer cette intention
                                    if (intent.resolveActivity(context.packageManager) != null) {
                                        context.startActivity(intent)
                                    } else {
                                        // Fallback: ouvrir avec un sélecteur d'applications
                                        val chooser = Intent.createChooser(intent, "Ouvrir le CV")
                                        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(chooser)
                                    }
                                } catch (e: Exception) {
                                    // Afficher l'erreur à l'utilisateur
                                    viewModel.loadApplication(applicationId) // Recharger pour afficher l'erreur
                                }
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Afficher l'erreur si elle existe
    state.error?.let { error ->
        LaunchedEffect(error) {
            // L'erreur sera affichée dans le contenu
        }
    }
    
    // Dialog de confirmation de suppression
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer la candidature") },
            text = { Text("Êtes-vous sûr de vouloir supprimer cette candidature ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteApplication(applicationId) {
                            onBack()
                        }
                    }
                ) {
                    Text("Supprimer", color = Color.Red)
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
private fun ApplicationDetailContent(
    application: Application,
    onDownloadCv: (String) -> Unit
) {
    val offer = application.internshipId
    val statusColor = when (application.status) {
        "accepted" -> Color(0xFF2E7D32)
        "rejected" -> Color(0xFFD32F2F)
        else -> Color(0xFFFF9800)
    }
    
    val statusText = when (application.status) {
        "accepted" -> "Accepté"
        "rejected" -> "Refusé"
        else -> "En attente"
    }
    
    // Déterminer l'étape actuelle
    val currentStep = when (application.status) {
        "accepted", "rejected" -> 2 // Décision finale
        "pending" -> 1 // En attente
        else -> 0 // Soumis
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card avec les infos de l'offre
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFEEBEE) // Rouge clair Esprit
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
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                
                // Statut badge
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
        
        // Barre de progression
        ProgressSteps(currentStep = currentStep)
        
        // Section CV
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
                    text = "CV",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                
                Button(
                    onClick = { onDownloadCv(application.cvUrl) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFCE0033) // Rouge Esprit
                    )
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
                    text = application.coverLetter ?: "Aucune lettre de motivation",
                    style = MaterialTheme.typography.bodyMedium
                )
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

=======
package com.example.esprit.ui.student.applications

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.esprit.model.Application
import com.example.esprit.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentApplicationDetailScreen(
    applicationId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    viewModel: ApplicationDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(applicationId) {
        viewModel.loadApplication(applicationId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détails") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit(applicationId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Modifier")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Supprimer",
                            tint = Color.Red
                        )
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
                    Column {
                        // Afficher l'erreur en haut si elle existe
                        state.error?.let { error ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Text(
                                    text = error,
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                        
                        ApplicationDetailContent(
                            application = state.application!!,
                            onDownloadCv = { cvUrl ->
                                try {
                                    val uri = when {
                                        // URI de contenu Android (content://...)
                                        cvUrl.startsWith("content://") -> {
                                            Uri.parse(cvUrl)
                                        }
                                        // URL HTTP complète
                                        cvUrl.startsWith("http://") || cvUrl.startsWith("https://") -> {
                                            Uri.parse(cvUrl)
                                        }
                                        // Chemin relatif - construire l'URL complète
                                        else -> {
                                            val baseUrl = Constants.BASE_URL.removeSuffix("api/")
                                            Uri.parse(baseUrl + cvUrl.trimStart('/'))
                                        }
                                    }
                                    
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, "application/pdf")
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    
                                    // Vérifier si une application peut gérer cette intention
                                    if (intent.resolveActivity(context.packageManager) != null) {
                                        context.startActivity(intent)
                                    } else {
                                        // Fallback: ouvrir avec un sélecteur d'applications
                                        val chooser = Intent.createChooser(intent, "Ouvrir le CV")
                                        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(chooser)
                                    }
                                } catch (e: Exception) {
                                    // Afficher l'erreur à l'utilisateur
                                    viewModel.loadApplication(applicationId) // Recharger pour afficher l'erreur
                                }
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Afficher l'erreur si elle existe
    state.error?.let { error ->
        LaunchedEffect(error) {
            // L'erreur sera affichée dans le contenu
        }
    }
    
    // Dialog de confirmation de suppression
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer la candidature") },
            text = { Text("Êtes-vous sûr de vouloir supprimer cette candidature ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteApplication(applicationId) {
                            onBack()
                        }
                    }
                ) {
                    Text("Supprimer", color = Color.Red)
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
private fun ApplicationDetailContent(
    application: Application,
    onDownloadCv: (String) -> Unit
) {
    val offer = application.internshipId
    val statusColor = when (application.status) {
        "accepted" -> Color(0xFF2E7D32)
        "rejected" -> Color(0xFFD32F2F)
        else -> Color(0xFFFF9800)
    }
    
    val statusText = when (application.status) {
        "accepted" -> "Accepté"
        "rejected" -> "Refusé"
        else -> "En attente"
    }
    
    // Déterminer l'étape actuelle
    val currentStep = when (application.status) {
        "accepted", "rejected" -> 2 // Décision finale
        "pending" -> 1 // En attente
        else -> 0 // Soumis
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card avec les infos de l'offre
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFEEBEE) // Rouge clair Esprit
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
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                
                // Statut badge
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
        
        // Barre de progression
        ProgressSteps(currentStep = currentStep)
        
        // Section CV
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
                    text = "CV",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                
                Button(
                    onClick = { onDownloadCv(application.cvUrl) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFCE0033) // Rouge Esprit
                    )
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
                    text = application.coverLetter ?: "Aucune lettre de motivation",
                    style = MaterialTheme.typography.bodyMedium
                )
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

>>>>>>> origin/messaging-announcement

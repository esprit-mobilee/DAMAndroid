package com.example.esprit.ui.shared.internships

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.example.esprit.model.InternshipOffer
import com.example.esprit.repository.InternshipOfferRepository
import com.example.esprit.util.Constants
import com.example.esprit.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ---------------------------------------------------------------------
// UI STATE + DETAIL VIEWMODEL
// ---------------------------------------------------------------------

data class InternshipDetailUiState(
    val isLoading: Boolean = true,
    val offer: InternshipOffer? = null,
    val error: String? = null
)

@HiltViewModel
class InternshipOfferDetailViewModel @Inject constructor(
    private val repository: InternshipOfferRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // "id" comes from the nav route internships/details/{id}
    private val offerId: String = savedStateHandle["id"] ?: ""

    private val _uiState = MutableStateFlow(InternshipDetailUiState(isLoading = true))
    val uiState: StateFlow<InternshipDetailUiState> = _uiState

    init {
        loadOffer()
    }

    private fun loadOffer() {
        viewModelScope.launch {
            _uiState.value = InternshipDetailUiState(isLoading = true)
            when (val res = repository.getOfferById(offerId)) {
                is Resource.Success ->
                    _uiState.value = InternshipDetailUiState(
                        isLoading = false,
                        offer = res.data
                    )

                is Resource.Error ->
                    _uiState.value = InternshipDetailUiState(
                        isLoading = false,
                        error = res.message
                    )

                else -> {}
            }
        }
    }
}

// ---------------------------------------------------------------------
// DETAIL SCREEN
// ---------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InternshipOfferDetailScreen(
    onBack: () -> Unit,
    currentUserId: String,              // identifiant étudiant (ex: HT12345)
    isAdmin: Boolean,
    onApplyClick: ((String) -> Unit)? = null,  // Pour naviguer vers l'écran de postulation
    onViewApplicationsClick: (() -> Unit)? = null,  // Pour naviguer vers les candidatures
    viewModel: InternshipOfferDetailViewModel = hiltViewModel(),
    favoriteViewModel: FavoriteViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val favoriteState by favoriteViewModel.uiState.collectAsState()
    
    // Vérifier si c'est un favori quand l'offre est chargée
    LaunchedEffect(state.offer?.id) {
        if (!isAdmin && state.offer?.id != null) {
            favoriteViewModel.checkFavorite(state.offer!!.id!!)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Détails du stage") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    if (!isAdmin && state.offer?.id != null) {
                        IconButton(
                        onClick = {
                            favoriteViewModel.toggleFavorite(state.offer!!.id!!)
                        }
                        ) {
                            Icon(
                                imageVector = if (favoriteState.isFavorite) {
                                    Icons.Default.Favorite
                                } else {
                                    Icons.Default.FavoriteBorder
                                },
                                contentDescription = "Ajouter aux favoris",
                                tint = if (favoriteState.isFavorite) {
                                    Color(0xFFD32F2F) // Rouge
                                } else {
                                    Color.Gray
                                }
                            )
                        }
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
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                state.error != null -> Text(
                    text = state.error ?: "Erreur",
                    modifier = Modifier.align(Alignment.Center)
                )

                state.offer != null -> InternshipDetailContent(
                    offer = state.offer!!,
                    currentUserId = currentUserId,
                    isAdmin = isAdmin,
                    onApplyClick = onApplyClick,
                    onViewApplicationsClick = onViewApplicationsClick
                )
            }
        }
    }
}

// ---------------------------------------------------------------------
// CONTENT
// ---------------------------------------------------------------------

@Composable
private fun InternshipDetailContent(
    offer: InternshipOffer,
    currentUserId: String,
    isAdmin: Boolean,
    onApplyClick: ((String) -> Unit)? = null,
    onViewApplicationsClick: (() -> Unit)? = null,
    applicationsViewModel: InternshipApplicationsViewModel = hiltViewModel()
) {
    val fullLogoUrl = offer.logoUrl?.let { relative ->
        Constants.BASE_URL
            .removeSuffix("api/")
            .plus(relative.trimStart('/'))
    }

    // AI Summary State
    var isSummarizing by remember { mutableStateOf(false) }
    var summaryText by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Mock AI Function
    fun summarizeDescription() {
        coroutineScope.launch {
            isSummarizing = true
            kotlinx.coroutines.delay(1500) // Simulate network delay
            
            // Simulation d'un résumé basé sur la description réelle
            val description = offer.description
            val summary = if (description.length > 100) {
                // Prend les deux premières phrases ou les 150 premiers caractères
                val sentences = description.split(Regex("(?<=[.!?])\\s+"))
                if (sentences.size >= 2) {
                    sentences.take(2).joinToString(" ")
                } else {
                    description.take(150) + "..."
                }
            } else {
                description
            }
            
            summaryText = "Résumé de la description : $summary"
            isSummarizing = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!fullLogoUrl.isNullOrBlank()) {
            AsyncImage(
                model = fullLogoUrl,
                contentDescription = "Logo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE0E0E0)),
                contentScale = ContentScale.Crop
            )
        }

        Text(
            text = offer.title,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = offer.company,
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray
        )
        
        // Location if available
        offer.location?.let { location ->
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
                    text = location,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        
        // Description section with AI Summarize Icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Description",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            
            IconButton(onClick = { summarizeDescription() }) {
                if (isSummarizing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Résumer avec l'IA",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        Text(
            text = offer.description,
            style = MaterialTheme.typography.bodyMedium
        )

        // Display Summary if available
        AnimatedVisibility(visible = summaryText != null) {
            summaryText?.let { summary ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Résumé IA", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = summary,
                            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Detailed attributes list
        DetailAttributeRow(
            icon = Icons.Default.AccessTime,
            label = "Durée",
            value = "${offer.duration} semaines"
        )
        
        offer.tags?.takeIf { it.isNotEmpty() }?.let { tags ->
            DetailAttributeRow(
                icon = Icons.Default.Label,
                label = "Tags",
                value = tags.joinToString(", ")
            )
        }
        
        offer.internshipType?.let { type ->
            DetailAttributeRow(
                icon = Icons.Default.Work,
                label = "Type de stage",
                value = type
            )
        }
        
        offer.procedure?.let { proc ->
            DetailAttributeRow(
                icon = Icons.Default.List,
                label = "Procédure",
                value = proc
            )
        }
        
        offer.interviewProcess?.let { process ->
            DetailAttributeRow(
                icon = Icons.Default.Person,
                label = "Processus d'entretien",
                value = process
            )
        }
        
        offer.startDate?.let { dateStr ->
            DetailAttributeRow(
                icon = Icons.Default.CalendarToday,
                label = "Début",
                value = formatDate(dateStr)
            )
        }
        
        offer.positionsAvailable?.let { positions ->
            DetailAttributeRow(
                icon = Icons.Default.Work,
                label = "Places disponibles",
                value = positions.toString()
            )
        }
        
        offer.applicationsCount?.let { count ->
            DetailAttributeRow(
                icon = Icons.Default.Person,
                label = "Postulés",
                value = count.toString()
            )
        }
        
        offer.interviewDetails?.let { details ->
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Détails d'entretien",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = details,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Spacer(Modifier.height(24.dp))

        val offerId = offer.id ?: ""

        if (isAdmin) {
            AdminApplicationsSection(
                offerId = offerId,
                viewModel = applicationsViewModel
            )
        } else {
            StudentActionButtons(
                offerId = offerId,
                onApplyClick = { onApplyClick?.invoke(offerId) },
                onViewApplicationsClick = onViewApplicationsClick
            )
        }
    }
}

// Detail Attribute Row Component
@Composable
private fun DetailAttributeRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(20.dp),
            tint = Color.Gray
        )
        Text(
            text = "$label : $value",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// Helper function to format date
private fun formatDate(dateStr: String): String {
    return try {
        // Try to parse ISO date format
        val isoFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
        val date = isoFormat.parse(dateStr)
        val displayFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        date?.let { displayFormat.format(it) } ?: dateStr
    } catch (e: Exception) {
        dateStr
    }
}

// ---------------------------------------------------------------------
// SECTION ÉTUDIANT : BOUTONS D'ACTION
// ---------------------------------------------------------------------

@Composable
private fun StudentActionButtons(
    offerId: String,
    onApplyClick: () -> Unit,
    onViewApplicationsClick: (() -> Unit)?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Bouton Postuler
        Button(
            onClick = onApplyClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD32F2F) // Rouge comme LinkedIn
            )
        ) {
            Text(
                text = "Postuler",
                color = Color.White
            )
        }
        
        // Bouton Voir mes candidatures
        onViewApplicationsClick?.let {
            OutlinedButton(
                onClick = it,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFD32F2F)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Voir mes candidatures")
            }
        }
    }
}

// ---------------------------------------------------------------------
// SECTION ÉTUDIANT : APPLIQUER (ancienne version - gardée pour référence)
// ---------------------------------------------------------------------

@Composable
private fun StudentApplySection(
    offerId: String,
    currentUserId: String,
    viewModel: InternshipApplicationsViewModel
) {
    val appsState by viewModel.uiState.collectAsState()

    var cvUrl by remember { mutableStateOf("") }
    var coverLetter by remember { mutableStateOf("") }
    var cvError by remember { mutableStateOf<String?>(null) }

    // Launcher pour choisir un PDF de CV
    val cvPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            cvUrl = it.toString()
            cvError = null
        }
    }

    // Launcher pour un PDF de lettre de motivation (optionnel)
    val letterPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            coverLetter = it.toString()
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Postuler à ce stage",
            style = MaterialTheme.typography.titleMedium
        )

        // ----- CV -----
        OutlinedTextField(
            value = cvUrl,
            onValueChange = {
                cvUrl = it
                cvError = null
            },
            label = { Text("Lien ou fichier CV") },
            isError = cvError != null,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { cvPickerLauncher.launch("application/pdf") }) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Joindre un CV (PDF)"
                    )
                }
            },
            supportingText = {
                cvError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        )

        // ----- Lettre de motivation -----
        OutlinedTextField(
            value = coverLetter,
            onValueChange = { coverLetter = it },
            label = { Text("Lettre de motivation (texte ou lien / fichier)") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp),
            trailingIcon = {
                IconButton(onClick = { letterPickerLauncher.launch("application/pdf") }) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Joindre une lettre (PDF)"
                    )
                }
            }
        )

        Button(
            onClick = {
                if (cvUrl.isBlank()) {
                    cvError = "Le CV est obligatoire (lien ou fichier)"
                    return@Button
                }

                viewModel.apply(
                    userId = currentUserId,
                    internshipId = offerId,
                    cvUrl = cvUrl,
                    coverLetter = coverLetter.ifBlank { null }
                )
            },
            enabled = !appsState.isApplying,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (appsState.isApplying) "Envoi..." else "Postuler")
        }

        appsState.applyError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (appsState.applySuccess) {
            Text(
                text = "Votre candidature a été envoyée.",
                color = Color(0xFF2E7D32),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// ---------------------------------------------------------------------
// SECTION ADMIN : LISTE DES CANDIDATURES
// ---------------------------------------------------------------------

@Composable
private fun AdminApplicationsSection(
    offerId: String,
    viewModel: InternshipApplicationsViewModel
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(offerId) {
        viewModel.loadForInternship(offerId)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Candidatures",
            style = MaterialTheme.typography.titleMedium
        )

        when {
            state.isLoading -> {
                CircularProgressIndicator()
            }

            state.error != null -> {
                Text(
                    text = state.error ?: "Erreur lors du chargement des candidatures",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            state.applications.isEmpty() -> {
                Text(
                    text = "Aucune candidature pour l'instant.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            else -> {
                state.applications.forEach { app ->
                    ApplicationRow(
                        applicantText = app.userId ?: "Étudiant inconnu",
                        cvUrl = app.cvUrl,
                        status = app.status,
                        onAccept = {
                            app.id?.let { id ->
                                viewModel.changeStatusForAdmin(
                                    applicationId = id,
                                    internshipId = offerId,
                                    newStatus = "accepted"
                                )
                            }
                        },
                        onReject = {
                            app.id?.let { id ->
                                viewModel.changeStatusForAdmin(
                                    applicationId = id,
                                    internshipId = offerId,
                                    newStatus = "rejected"
                                )
                            }
                        }
                    )
                    Spacer(Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
private fun ApplicationRow(
    applicantText: String,
    cvUrl: String?,
    status: String?,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF7F7F7), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Text(
            text = applicantText,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "CV : ${cvUrl ?: "Non disponible"}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Statut : ${status ?: "pending"}",
            style = MaterialTheme.typography.bodySmall
        )
    }
}


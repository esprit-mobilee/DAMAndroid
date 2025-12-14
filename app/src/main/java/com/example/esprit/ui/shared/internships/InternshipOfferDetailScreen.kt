package com.example.esprit.ui.shared.internships

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    viewModel: InternshipOfferDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Détails du stage") },
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
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                state.error != null -> Text(
                    text = state.error ?: "Erreur",
                    modifier = Modifier.align(Alignment.Center)
                )

                state.offer != null -> InternshipDetailContent(
                    offer = state.offer!!,
                    currentUserId = currentUserId,
                    isAdmin = isAdmin
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
    applicationsViewModel: InternshipApplicationsViewModel = hiltViewModel()
) {
    val fullLogoUrl = offer.logoUrl?.let { relative ->
        Constants.BASE_URL
            .removeSuffix("api/")
            .plus(relative.trimStart('/'))
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

        Spacer(Modifier.height(8.dp))
        Text(
            text = offer.description,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(16.dp))

        ChipsRow(offer)

        Spacer(Modifier.height(24.dp))

        val offerId = offer.id ?: ""

        if (isAdmin) {
            AdminApplicationsSection(
                offerId = offerId,
                viewModel = applicationsViewModel
            )
        } else {
            StudentApplySection(
                offerId = offerId,
                currentUserId = currentUserId,
                viewModel = applicationsViewModel
            )
        }
    }
}

// TAGS

@Composable
private fun ChipsRow(offer: InternshipOffer) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ChipTag(offer.location?.address ?: "Lieu inconnu")
        ChipTag("${offer.duration} sem.")
        offer.salary?.let { ChipTag("$it DT") }
    }
}

@Composable
private fun ChipTag(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color(0xFFF2F2F2)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

// ---------------------------------------------------------------------
// SECTION ÉTUDIANT : APPLIQUER (texte + icône trombone)
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

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onReject,
                modifier = Modifier.weight(1f)
            ) {
                Text("Refuser")
            }
            Button(
                onClick = onAccept,
                modifier = Modifier.weight(1f)
            ) {
                Text("Accepter")
            }
        }
    }
}

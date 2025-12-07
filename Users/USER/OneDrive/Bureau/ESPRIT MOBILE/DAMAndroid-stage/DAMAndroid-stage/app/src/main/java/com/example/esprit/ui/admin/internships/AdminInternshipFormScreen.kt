package com.example.esprit.ui.admin.internships

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminInternshipFormScreen(
    offerId: String? = null,
    onDone: () -> Unit,
    viewModel: AdminInternshipViewModel = hiltViewModel()
) {
    val currentOffer by viewModel.currentOffer.collectAsState()
    val context = LocalContext.current

    // Charger si édition
    LaunchedEffect(offerId) {
        if (offerId != null) viewModel.loadOfferById(offerId)
    }

    var title by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf<com.example.esprit.model.Location?>(null) }
    var duration by remember { mutableStateOf("8") }
    var salary by remember { mutableStateOf("") }

    // erreurs
    var titleError by remember { mutableStateOf<String?>(null) }
    var companyError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var durationError by remember { mutableStateOf<String?>(null) }

    // logo
    var existingLogoUrl by remember { mutableStateOf<String?>(null) }
    var selectedLogoUri by remember { mutableStateOf<Uri?>(null) }
    
    // Location picker state
    var showLocationPicker by remember { mutableStateOf(false) }

    // Pré-remplir si édition
    LaunchedEffect(currentOffer) {
        currentOffer?.let { offer ->
            title = offer.title
            company = offer.company
            description = offer.description
            location = offer.location
            duration = offer.duration.toString()
            salary = offer.salary?.toString() ?: ""
            existingLogoUrl = offer.logoUrl
        }
    }

    // Sélection image
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) selectedLogoUri = uri
    }

    fun buildLogoPart(uri: Uri?): MultipartBody.Part? {
        if (uri == null) return null
        return try {
            val contentResolver = context.contentResolver
            val type = contentResolver.getType(uri) ?: "image/*"
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
            val req = bytes.toRequestBody(type.toMediaType())
            MultipartBody.Part.createFormData(
                "logo", "logo_${System.currentTimeMillis()}.jpg", req
            )
        } catch (e: Exception) {
            null
        }
    }
    
    if (showLocationPicker) {
        com.example.esprit.ui.components.LocationPickerDialog(
            initialLocation = location,
            onLocationSelected = { loc ->
                location = loc
                locationError = null
                showLocationPicker = false
            },
            onDismiss = { showLocationPicker = false }
        )
    }

    // --------------------------------------------------------------------
    //  SCAFFOLD - TOP BAR
    // --------------------------------------------------------------------
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (offerId == null)
                            "Ajouter une offre de stage"
                        else
                            "Modifier une offre de stage"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onDone() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { innerPadding ->

        // --------------------------------------------------------------
        // 🟣  FORMULAIRE SCROLLABLE
        // --------------------------------------------------------------
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ---------- TITRE ----------
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    titleError = null
                },
                label = { Text("Titre") },
                modifier = Modifier.fillMaxWidth(),
                isError = titleError != null,
                supportingText = {
                    titleError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            )

            // ---------- ENTREPRISE ----------
            OutlinedTextField(
                value = company,
                onValueChange = {
                    company = it
                    companyError = null
                },
                label = { Text("Entreprise") },
                modifier = Modifier.fillMaxWidth(),
                isError = companyError != null,
                supportingText = {
                    companyError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            )

            // ---------- DESCRIPTION ----------
            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                    descriptionError = null
                },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                isError = descriptionError != null,
                supportingText = {
                    descriptionError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            )

            // ---------- LIEU (Picker) ----------
            OutlinedTextField(
                value = location?.address ?: "",
                onValueChange = {}, // Read-only via text field, change via click
                label = { Text("Lieu") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLocationPicker = true },
                enabled = false, // Disable typing, handle click on parent or trailing icon
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                trailingIcon = {
                    IconButton(onClick = { showLocationPicker = true }) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Select Location")
                    }
                },
                isError = locationError != null,
                supportingText = {
                    locationError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            )

            // ---------- DURATION ----------
            OutlinedTextField(
                value = duration,
                onValueChange = {
                    duration = it
                    durationError = null
                },
                label = { Text("Durée (semaines)") },
                modifier = Modifier.fillMaxWidth(),
                isError = durationError != null,
                supportingText = {
                    durationError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            )

            OutlinedTextField(
                value = salary,
                onValueChange = { salary = it },
                label = { Text("Salaire (optionnel)") },
                modifier = Modifier.fillMaxWidth()
            )

            // ---------- LOGO ----------
            Text("Logo (optionnel)")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color(0xFFF2F2F2), RoundedCornerShape(12.dp))
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                val painter = when {
                    selectedLogoUri != null -> rememberAsyncImagePainter(selectedLogoUri)
                    !existingLogoUrl.isNullOrBlank() -> rememberAsyncImagePainter(existingLogoUrl)
                    else -> null
                }

                if (painter != null) {
                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(8.dp)
                    )
                } else {
                    Text("Touchez pour choisir une image")
                }
            }

            Spacer(Modifier.height(16.dp))

            // ----------------------------------------------------------------
            //  BUTTON : ENREGISTRER
            // ----------------------------------------------------------------
            Button(
                onClick = {
                    titleError = null
                    companyError = null
                    descriptionError = null
                    locationError = null
                    durationError = null

                    var hasError = false

                    if (title.isBlank()) {
                        titleError = "Le titre est obligatoire"
                        hasError = true
                    }
                    if (company.isBlank()) {
                        companyError = "Entreprise obligatoire"
                        hasError = true
                    }
                    if (description.isBlank()) {
                        descriptionError = "Description obligatoire"
                        hasError = true
                    }
                    if (location == null) {
                        locationError = "Lieu obligatoire"
                        hasError = true
                    }

                    val durationInt = duration.toIntOrNull()
                    if (durationInt == null || durationInt <= 0) {
                        durationError = "Durée invalide"
                        hasError = true
                    }

                    if (hasError) return@Button

                    val salaryInt = salary.toIntOrNull()
                    val logoPart = buildLogoPart(selectedLogoUri)

                    if (offerId == null) {
                        viewModel.createOffer(
                            title, company, description, location,
                            durationInt!!, salaryInt, logoPart, onDone
                        )
                    } else {
                        viewModel.updateOffer(
                            offerId, title, company, description, location,
                            durationInt!!, salaryInt, logoPart, onDone
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enregistrer")
            }

            TextButton(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Annuler")
            }
        }
    }
}

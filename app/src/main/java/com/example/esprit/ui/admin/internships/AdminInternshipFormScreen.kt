package com.example.esprit.ui.admin.internships

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.esprit.model.LocationData
import com.example.esprit.ui.components.MapLocationPickerScreen
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdminInternshipFormScreen(
    offerId: String? = null,
    onDone: () -> Unit,
    viewModel: AdminInternshipViewModel = hiltViewModel()
) {
    val currentOffer by viewModel.currentOffer.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Charger si édition
    LaunchedEffect(offerId) {
        if (offerId != null) viewModel.loadOfferById(offerId)
    }

    // Form State
    var title by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var locationAddress by remember { mutableStateOf("") }
    var locationLatitude by remember { mutableStateOf<Double?>(null) }
    var locationLongitude by remember { mutableStateOf<Double?>(null) }
    var duration by remember { mutableStateOf("8") }
    var salary by remember { mutableStateOf("") }
    
    // New fields
    var tags by remember { mutableStateOf("") } // Keep for compatibility if needed, but mainly use selectedTags
    var selectedTags by remember { mutableStateOf<List<String>>(emptyList()) }
    var internshipType by remember { mutableStateOf("") }
    var procedure by remember { mutableStateOf("") }
    var interviewProcess by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var interviewDetails by remember { mutableStateOf("") }
    var positionsAvailable by remember { mutableStateOf("") }
    
    // Logo
    var existingLogoUrl by remember { mutableStateOf<String?>(null) }
    var selectedLogoUri by remember { mutableStateOf<Uri?>(null) }

    // Errors
    var titleError by remember { mutableStateOf<String?>(null) }
    var companyError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var durationError by remember { mutableStateOf<String?>(null) }

    // Wizard State
    var currentStep by remember { mutableIntStateOf(0) }
    val totalSteps = 3
    
    // Map Picker State
    var showMapPicker by remember { mutableStateOf(false) }

    // Helper function to format date for display
    fun formatDateForDisplay(dateStr: String): String {
        return try {
            val date = java.time.Instant.parse(dateStr).atZone(ZoneId.systemDefault()).toLocalDate()
            date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        } catch (e: Exception) {
            dateStr
        }
    }

    // Pré-remplir si édition
    LaunchedEffect(currentOffer) {
        currentOffer?.let { offer ->
            title = offer.title
            company = offer.company
            description = offer.description
            locationAddress = offer.location?.address ?: ""
            locationLatitude = offer.location?.latitude
            locationLongitude = offer.location?.longitude
            duration = offer.duration.toString()
            salary = offer.salary?.toString() ?: ""
            existingLogoUrl = offer.logoUrl
            selectedTags = offer.tags ?: emptyList()
            tags = offer.tags?.joinToString(", ") ?: ""
            internshipType = offer.internshipType ?: ""
            procedure = offer.procedure ?: ""
            interviewProcess = offer.interviewProcess ?: ""
            startDate = offer.startDate?.let { formatDateForDisplay(it) } ?: ""
            interviewDetails = offer.interviewDetails ?: ""
            positionsAvailable = offer.positionsAvailable?.toString() ?: ""
        }
    }

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
            MultipartBody.Part.createFormData("image", "logo_${System.currentTimeMillis()}.jpg", req)
        } catch (e: Exception) {
            null
        }
    }

    fun validateStep1(): Boolean {
        var isValid = true
        if (title.isBlank()) { titleError = "Requis"; isValid = false } else titleError = null
        if (company.isBlank()) { companyError = "Requis"; isValid = false } else companyError = null
        if (description.isBlank()) { descriptionError = "Requis"; isValid = false } else descriptionError = null
        if (locationAddress.isBlank()) { locationError = "Requis"; isValid = false } else locationError = null
        return isValid
    }

    fun validateStep2(): Boolean {
        var isValid = true
        val durationInt = duration.toIntOrNull()
        if (durationInt == null || durationInt <= 0) { durationError = "Invalide"; isValid = false } else durationError = null
        return isValid
    }

    fun submitForm() {
        if (uiState.isSaving) return

        val salaryInt = salary.toIntOrNull()
        val logoPart = buildLogoPart(selectedLogoUri)
        val tagsList = selectedTags.ifEmpty { tags.split(",").map { it.trim() }.filter { it.isNotEmpty() } }
        val internshipTypeStr = internshipType.ifBlank { null }
        val procedureStr = procedure.ifBlank { null }
        val interviewProcessStr = interviewProcess.ifBlank { null }
        val startDateStr = if (startDate.isNotBlank()) {
            try {
                val localDate = java.time.LocalDate.parse(startDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toString()
            } catch (e: Exception) { null }
        } else null
        val interviewDetailsStr = interviewDetails.ifBlank { null }
        val positionsAvailableInt = positionsAvailable.toIntOrNull()

        if (offerId == null) {
            viewModel.createOffer(
                title, company, description, locationAddress, locationLatitude, locationLongitude,
                duration.toInt(), salaryInt, logoPart,
                tagsList, internshipTypeStr, procedureStr, interviewProcessStr,
                startDateStr, interviewDetailsStr, positionsAvailableInt, onDone
            )
        } else {
            viewModel.updateOffer(
                offerId, title, company, description, locationAddress, locationLatitude, locationLongitude,
                duration.toInt(), salaryInt, logoPart,
                tagsList, internshipTypeStr, procedureStr, interviewProcessStr,
                startDateStr, interviewDetailsStr, positionsAvailableInt, onDone
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (offerId == null) "Nouvelle Offre" else "Modifier l'Offre") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (currentStep > 0) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Précédent")
                        }
                        Spacer(Modifier.width(16.dp))
                    } else {
                        Spacer(Modifier.weight(1f))
                        Spacer(Modifier.width(16.dp))
                    }

                    Button(
                        onClick = {
                            when (currentStep) {
                                0 -> if (validateStep1()) currentStep++
                                1 -> if (validateStep2()) currentStep++
                                2 -> submitForm()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                            Text(if (currentStep == totalSteps - 1) "Terminer" else "Suivant")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            // Stepper
            StepIndicator(currentStep = currentStep, totalSteps = totalSteps)

            // Content
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> width } + fadeOut()
                    }.using(SizeTransform(clip = false))
                },
                label = "stepTransition"
            ) { step ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            when (step) {
                                0 -> Step1BasicInfo(
                                    title = title, onTitleChange = { title = it; titleError = null }, titleError = titleError,
                                    company = company, onCompanyChange = { company = it; companyError = null }, companyError = companyError,
                                    description = description, onDescriptionChange = { description = it; descriptionError = null }, descriptionError = descriptionError,
                                    locationAddress = locationAddress, onLocationChange = { locationAddress = it; locationError = null }, locationError = locationError,
                                    onMapPickerClick = { showMapPicker = true },
                                    selectedLogoUri = selectedLogoUri, existingLogoUrl = existingLogoUrl, onLogoClick = { imagePickerLauncher.launch("image/*") }
                                )
                                1 -> Step2Details(
                                    duration = duration, onDurationChange = { duration = it; durationError = null }, durationError = durationError,
                                    salary = salary, onSalaryChange = { salary = it },
                                    selectedTags = selectedTags, onTagsChange = { selectedTags = it },
                                    internshipType = internshipType, onTypeChange = { internshipType = it },
                                    positionsAvailable = positionsAvailable, onPositionsChange = { positionsAvailable = it }
                                )
                                2 -> Step3Process(
                                    procedure = procedure, onProcedureChange = { procedure = it },
                                    interviewProcess = interviewProcess, onProcessChange = { interviewProcess = it },
                                    startDate = startDate, onDateChange = { startDate = it },
                                    interviewDetails = interviewDetails, onDetailsChange = { interviewDetails = it }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(80.dp)) // Bottom padding for FAB/Buttons
                }
            }
        }
    }
    
    // Map Picker Dialog
    if (showMapPicker) {
        MapLocationPickerScreen(
            initialLocation = if (locationAddress.isNotBlank() && locationLatitude != null && locationLongitude != null) {
                LocationData(locationAddress, locationLatitude, locationLongitude)
            } else null,
            onLocationSelected = { selectedLocation ->
                locationAddress = selectedLocation.address ?: ""
                locationLatitude = selectedLocation.latitude
                locationLongitude = selectedLocation.longitude
                locationError = null
            },
            onBack = { showMapPicker = false }
        )
    }
}

@Composable
fun StepIndicator(currentStep: Int, totalSteps: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp, horizontal = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until totalSteps) {
            val isCompleted = i < currentStep
            val isCurrent = i == currentStep
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCompleted || isCurrent) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            width = 2.dp,
                            color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = "${i + 1}",
                            color = if (isCurrent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = when(i) {
                        0 -> "Infos"
                        1 -> "Détails"
                        else -> "Process"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (i < totalSteps - 1) {
                Divider(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .offset(y = (-10).dp), // Align with circle center
                    color = if (i < currentStep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    thickness = 2.dp
                )
            }
        }
    }
}

@Composable
fun Step1BasicInfo(
    title: String, onTitleChange: (String) -> Unit, titleError: String?,
    company: String, onCompanyChange: (String) -> Unit, companyError: String?,
    description: String, onDescriptionChange: (String) -> Unit, descriptionError: String?,
    locationAddress: String, onLocationChange: (String) -> Unit, locationError: String?,
    onMapPickerClick: () -> Unit,
    selectedLogoUri: Uri?, existingLogoUrl: String?, onLogoClick: () -> Unit
) {
    Text("Informations Générales", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    
    ModernTextField(value = title, onValueChange = onTitleChange, label = "Titre du stage", icon = Icons.Default.WorkOutline, error = titleError)
    ModernTextField(value = company, onValueChange = onCompanyChange, label = "Entreprise", icon = Icons.Default.Business, error = companyError)
    
    // Location Picker Button
    OutlinedButton(
        onClick = onMapPickerClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (locationError != null) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f) else Color.Transparent
        ),
        border = BorderStroke(
            1.dp,
            if (locationError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = if (locationError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (locationAddress.isBlank()) "Sélectionner sur la carte" else locationAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (locationAddress.isBlank()) Color.Gray else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                Icons.Default.Map,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
    if (locationError != null) {
        Text(
            text = locationError,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
        )
    }
    
    
    OutlinedTextField(
        value = description,
        onValueChange = onDescriptionChange,
        label = { Text("Description") },
        modifier = Modifier.fillMaxWidth().height(120.dp),
        isError = descriptionError != null,
        supportingText = { descriptionError?.let { Text(it) } },
        shape = RoundedCornerShape(12.dp)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(onClick = onLogoClick)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        val painter = when {
            selectedLogoUri != null -> rememberAsyncImagePainter(selectedLogoUri)
            !existingLogoUrl.isNullOrBlank() -> rememberAsyncImagePainter(existingLogoUrl)
            else -> null
        }

        if (painter != null) {
            Image(painter = painter, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
            Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Ajouter un logo", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Step2Details(
    duration: String, onDurationChange: (String) -> Unit, durationError: String?,
    salary: String, onSalaryChange: (String) -> Unit,
    selectedTags: List<String>, onTagsChange: (List<String>) -> Unit,
    internshipType: String, onTypeChange: (String) -> Unit,
    positionsAvailable: String, onPositionsChange: (String) -> Unit
) {
    Text("Détails de l'offre", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

    ModernTextField(value = duration, onValueChange = onDurationChange, label = "Durée (semaines)", icon = Icons.Default.Schedule, error = durationError, keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
    ModernTextField(value = salary, onValueChange = onSalaryChange, label = "Salaire (optionnel)", icon = Icons.Default.AttachMoney, keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
    ModernTextField(value = positionsAvailable, onValueChange = onPositionsChange, label = "Places disponibles", icon = Icons.Default.Group, keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)

    // Type Dropdown
    val types = listOf("Stage d'été", "Stage d'hiver", "Stage PFE", "Stage d'observation", "Stage professionnel")
    DropdownField(label = "Type de stage", options = types, selectedOption = internshipType, onOptionSelected = onTypeChange, icon = Icons.Default.Category)

    // Tags
    Text("Compétences requises", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
    val availableTags = listOf("Flutter", "React", "Node.js", "Angular", "Vue.js", "Python", "Java", "Spring Boot", "Django", "Laravel", "PHP", "JavaScript", "TypeScript", "Swift", "Kotlin", "Android", "iOS", "MongoDB", "PostgreSQL", "MySQL")
    
    // Simple Multi-select implementation
    var tagsExpanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = tagsExpanded,
        onExpandedChange = { tagsExpanded = !tagsExpanded }
    ) {
        OutlinedTextField(
            value = if (selectedTags.isEmpty()) "Sélectionner des tags" else "${selectedTags.size} sélectionné(s)",
            onValueChange = {},
            readOnly = true,
            label = { Text("Tags") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tagsExpanded) },
            leadingIcon = { Icon(Icons.Default.Label, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = tagsExpanded,
            onDismissRequest = { tagsExpanded = false }
        ) {
            availableTags.forEach { tag ->
                val isSelected = selectedTags.contains(tag)
                DropdownMenuItem(
                    text = { Text(tag) },
                    onClick = {
                        val newTags = if (isSelected) selectedTags - tag else selectedTags + tag
                        onTagsChange(newTags)
                    },
                    trailingIcon = { if (isSelected) Icon(Icons.Default.Check, contentDescription = null) }
                )
            }
        }
    }
    
    // Chips display
    if (selectedTags.isNotEmpty()) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            selectedTags.forEach { tag ->
                InputChip(
                    selected = true,
                    onClick = { onTagsChange(selectedTags - tag) },
                    label = { Text(tag) },
                    trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step3Process(
    procedure: String, onProcedureChange: (String) -> Unit,
    interviewProcess: String, onProcessChange: (String) -> Unit,
    startDate: String, onDateChange: (String) -> Unit,
    interviewDetails: String, onDetailsChange: (String) -> Unit
) {
    Text("Processus & Dates", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

    val procedures = listOf("Appel téléphonique", "Email", "Candidature en ligne", "Appel + test technique", "Entretien direct")
    DropdownField(label = "Procédure de candidature", options = procedures, selectedOption = procedure, onOptionSelected = onProcedureChange, icon = Icons.Default.Assignment)

    val processes = listOf("Appel RH", "Test technique", "Entretien technique", "Entretien avec manager", "Appel + test + entretien")
    DropdownField(label = "Processus d'entretien", options = processes, selectedOption = interviewProcess, onOptionSelected = onProcessChange, icon = Icons.Default.People)

    // Date Picker
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        onDateChange(date)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Annuler") } }
        ) { DatePicker(state = datePickerState) }
    }

    OutlinedTextField(
        value = startDate,
        onValueChange = {},
        readOnly = true,
        label = { Text("Date de démarrage") },
        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
        modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
        enabled = false, // Disable typing, but clickable works on Box usually, here we rely on the modifier
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    )
    
    ModernTextField(value = interviewDetails, onValueChange = onDetailsChange, label = "Lien ou détails d'entretien", icon = Icons.Default.Link)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    error: String? = null,
    keyboardType: androidx.compose.ui.text.input.KeyboardType = androidx.compose.ui.text.input.KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        modifier = Modifier.fillMaxWidth(),
        isError = error != null,
        supportingText = { error?.let { Text(it) } },
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    icon: ImageVector
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedOption.ifEmpty { "Sélectionner" },
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            leadingIcon = { Icon(icon, contentDescription = null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

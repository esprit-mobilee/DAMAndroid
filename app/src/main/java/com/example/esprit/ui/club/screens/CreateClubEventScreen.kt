package com.example.esprit.ui.club.screens

import android.app.TimePickerDialog
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.esprit.repository.EventPayload
import com.example.esprit.ui.club.CreateEventViewModel
import com.example.esprit.util.MultipartUtil
import com.example.esprit.util.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Locale
import java.util.TimeZone

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateClubEventScreen(
    onBack: () -> Unit,
    onCreated: () -> Unit,
    viewModel: CreateEventViewModel = hiltViewModel()
) {
    val saving by viewModel.saving.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf<com.example.esprit.model.Location?>(null) }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var questions by remember { mutableStateOf(listOf<String>()) }
    
    // Location picker state
    var showLocationPicker by remember { mutableStateOf(false) }
    
    // Validation errors
    var titleError by remember { mutableStateOf<String?>(null) }
    var startDateError by remember { mutableStateOf<String?>(null) }
    var endDateError by remember { mutableStateOf<String?>(null) }

    // Date and time pickers - using Material 3 DatePicker
    val now = System.currentTimeMillis()
    val oneHourLater = now + (60 * 60 * 1000) // 1 hour in milliseconds
    
    // Start date/time state
    var startDateMillis by remember { mutableStateOf(now) }
    var startTime by remember { mutableStateOf(LocalTime.now()) }
    var showStartDateTimePicker by remember { mutableStateOf(false) }
    
    // End date/time state
    var endDateMillis by remember { mutableStateOf(oneHourLater) }
    var endTime by remember { mutableStateOf(LocalTime.now().plusHours(1)) }
    var showEndDateTimePicker by remember { mutableStateOf(false) }
    
    // Date picker states
    val startDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = startDateMillis
    )
    val endDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = endDateMillis
    )
    
    // Update picker states when showing
    LaunchedEffect(showStartDateTimePicker) {
        if (showStartDateTimePicker) {
            startDatePickerState.selectedDateMillis = startDateMillis
        }
    }
    
    LaunchedEffect(showEndDateTimePicker) {
        if (showEndDateTimePicker) {
            endDatePickerState.selectedDateMillis = endDateMillis
        }
    }
    
    // Format dates for display - user-friendly format
    fun formatDateForDisplay(millis: Long, time: LocalTime): String {
        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
        val dayName = when (date.dayOfWeek.value) {
            1 -> "Lun"
            2 -> "Mar"
            3 -> "Mer"
            4 -> "Jeu"
            5 -> "Ven"
            6 -> "Sam"
            7 -> "Dim"
            else -> ""
        }
        val monthName = when (date.monthValue) {
            1 -> "Jan"
            2 -> "Fév"
            3 -> "Mar"
            4 -> "Avr"
            5 -> "Mai"
            6 -> "Juin"
            7 -> "Juil"
            8 -> "Aoû"
            9 -> "Sep"
            10 -> "Oct"
            11 -> "Nov"
            12 -> "Déc"
            else -> ""
        }
        return "$dayName ${date.dayOfMonth} $monthName ${date.year} à ${String.format(Locale.US, "%02d:%02d", time.hour, time.minute)}"
    }
    
    fun formatDateForBackend(millis: Long, time: LocalTime): String {
        // Combine date from millis with selected time
        val localDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
        
        // Format as ISO 8601: 2025-12-10T09:00:00Z
        // Using the selected date/time directly (backend will interpret it)
        return String.format(
            Locale.US, 
            "%04d-%02d-%02dT%02d:%02d:%02dZ",
            localDate.year,
            localDate.monthValue,
            localDate.dayOfMonth,
            time.hour,
            time.minute,
            time.second
        )
    }
    
    val startDateString = formatDateForDisplay(startDateMillis, startTime)
    val endDateString = formatDateForDisplay(endDateMillis, endTime)

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createEvent() {
        // Reset errors
        titleError = null
        startDateError = null
        endDateError = null
        
        var hasError = false
        
        // Validate title
        if (title.isBlank()) {
            titleError = "Le titre est obligatoire"
            hasError = true
        }
        
        // Validate dates
        val startLocalDate = Instant.ofEpochMilli(startDateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        val endLocalDate = Instant.ofEpochMilli(endDateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        val startDateTime = startLocalDate.atTime(startTime)
        val endDateTime = endLocalDate.atTime(endTime)
        
        if (endDateTime.isBefore(startDateTime) || endDateTime.isEqual(startDateTime)) {
            endDateError = "La date de fin doit être postérieure à la date de début"
            hasError = true
        }
        
        if (hasError) {
            return
        }

        val locationPart = if (location != null) {
            val json = com.google.gson.Gson().toJson(location)
            MultipartUtil.textPart(json)
        } else null

        val formQuestionsPart = if (questions.isNotEmpty()) {
            val validQuestions = questions.filter { it.isNotBlank() }
            if (validQuestions.isNotEmpty()) {
                val json = com.google.gson.Gson().toJson(validQuestions)
                MultipartUtil.textPart(json)
            } else null
        } else null

        val payload = EventPayload(
            title = MultipartUtil.textPart(title.trim()),
            startDate = MultipartUtil.textPart(formatDateForBackend(startDateMillis, startTime)),
            endDate = MultipartUtil.textPart(formatDateForBackend(endDateMillis, endTime)),
            location = locationPart,
            description = MultipartUtil.nullableTextPart(description.trim().takeIf { it.isNotBlank() }),
            category = MultipartUtil.nullableTextPart(category.trim().takeIf { it.isNotBlank() }),
            formQuestions = formQuestionsPart,
            image = imageUri?.let { MultipartUtil.uriToImagePart(context, it) }
        )

        scope.launch {
            when (val res = viewModel.create(payload)) {
                is UiState.Success -> {
                    snackbarHostState.showSnackbar("Événement créé avec succès")
                    kotlinx.coroutines.delay(500) // Small delay to show snackbar
                    onCreated()
                }
                is UiState.Error -> {
                    val errorMsg = res.message ?: "Erreur lors de la création"
                    
                    // Parse backend errors and map to fields
                    val lowerError = errorMsg.lowercase(Locale.getDefault())
                    
                    if (lowerError.contains("title") || lowerError.contains("titre")) {
                        if (lowerError.contains("obligatoire") || lowerError.contains("required") || lowerError.contains("not empty")) {
                            titleError = "Le titre est obligatoire"
                        } else {
                            titleError = "Le titre est invalide"
                        }
                    }
                    
                    if (lowerError.contains("startdate") || lowerError.contains("date de début") || lowerError.contains("start date")) {
                        if (lowerError.contains("format") || lowerError.contains("iso") || lowerError.contains("8601")) {
                            startDateError = "Format de date invalide. Format attendu: ISO 8601"
                        } else if (lowerError.contains("obligatoire") || lowerError.contains("required")) {
                            startDateError = "La date de début est obligatoire"
                        } else {
                            startDateError = "La date de début est invalide"
                        }
                    }
                    
                    if (lowerError.contains("enddate") || lowerError.contains("date de fin") || lowerError.contains("end date")) {
                        if (lowerError.contains("format") || lowerError.contains("iso") || lowerError.contains("8601")) {
                            endDateError = "Format de date invalide. Format attendu: ISO 8601"
                        } else if (lowerError.contains("postérieure") || lowerError.contains("after") || lowerError.contains("before")) {
                            endDateError = "La date de fin doit être postérieure à la date de début"
                        } else if (lowerError.contains("obligatoire") || lowerError.contains("required")) {
                            endDateError = "La date de fin est obligatoire"
                        } else {
                            endDateError = "La date de fin est invalide"
                        }
                    }
                    
                    // Also show in snackbar for general errors
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = errorMsg,
                            duration = SnackbarDuration.Long
                        )
                    }
                }
                UiState.Loading -> Unit
            }
        }
    }
    
    if (showLocationPicker) {
        com.example.esprit.ui.components.LocationPickerDialog(
            initialLocation = location,
            onLocationSelected = { loc ->
                location = loc
                showLocationPicker = false
            },
            onDismiss = { showLocationPicker = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nouvel événement") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { 
                    title = it
                    titleError = null // Clear error when user types
                },
                label = { Text("Titre de l'événement") },
                modifier = Modifier.fillMaxWidth(),
                isError = titleError != null,
                supportingText = {
                    titleError?.let { 
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
            
            // Location Picker
            OutlinedTextField(
                value = location?.address ?: "",
                onValueChange = {}, // Read-only via text field
                label = { Text("Lieu") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLocationPicker = true },
                enabled = false,
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
                }
            )
            
            // Start Date & Time Picker - UI Friendly Card
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                DateTimePickerCard(
                    label = "Date et heure de début",
                    dateTimeString = startDateString,
                    onClick = { 
                        showStartDateTimePicker = true
                        startDateError = null // Clear error when user opens picker
                    },
                    isError = startDateError != null
                )
                if (startDateError != null) {
                    Text(
                        text = startDateError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
            
            // End Date & Time Picker - UI Friendly Card
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                DateTimePickerCard(
                    label = "Date et heure de fin",
                    dateTimeString = endDateString,
                    onClick = { 
                        showEndDateTimePicker = true
                        endDateError = null // Clear error when user opens picker
                    },
                    isError = endDateError != null
                )
                if (endDateError != null) {
                    Text(
                        text = endDateError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
            
            // Combined Date & Time Picker Dialogs
            if (showStartDateTimePicker) {
                DateTimePickerDialog(
                    title = "Date et heure de début",
                    datePickerState = startDatePickerState,
                    initialTime = startTime,
                    onDismiss = { showStartDateTimePicker = false },
                    onConfirm = { dateMillis, time ->
                        startDateMillis = dateMillis
                        startTime = time
                        showStartDateTimePicker = false
                    }
                )
            }
            
            if (showEndDateTimePicker) {
                DateTimePickerDialog(
                    title = "Date et heure de fin",
                    datePickerState = endDatePickerState,
                    initialTime = endTime,
                    onDismiss = { showEndDateTimePicker = false },
                    onConfirm = { dateMillis, time ->
                        endDateMillis = dateMillis
                        endTime = time
                        showEndDateTimePicker = false
                    }
                )
            }
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                maxLines = 5
            )
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Catégorie (optionnelle)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Form Questions Section
            Text(
                "Formulaire d'inscription",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (questions.isEmpty()) {
                        Text(
                            "Aucune question ajoutée. Les étudiants pourront s'inscrire sans formulaire.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    questions.forEachIndexed { index, question ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = question,
                                onValueChange = { newValue ->
                                    questions = questions.toMutableList().also { it[index] = newValue }
                                },
                                label = { Text("Question ${index + 1}") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            IconButton(
                                onClick = {
                                    questions = questions.toMutableList().also { it.removeAt(index) }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Supprimer",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { questions = questions + "" },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Ajouter une question")
                    }
                }
            }

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable { imagePickerLauncher.launch("image/*") },
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        imageUri != null -> Image(
                            painter = rememberAsyncImagePainter(model = imageUri),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                        else -> Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Ajouter l'affiche de l'événement",
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            TextButton(onClick = { imageUri = null }) {
                Text("Retirer l'image")
            }

            Button(
                onClick = { createEvent() },
                enabled = !saving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (saving) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier
                            .height(20.dp)
                            .padding(end = 8.dp)
                    )
                }
                Text("Créer l'événement")
            }
        }
    }
}

@Composable
private fun DateTimePickerCard(
    label: String,
    dateTimeString: String,
    onClick: () -> Unit,
    isError: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isError) 
                MaterialTheme.colorScheme.errorContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isError) {
            BorderStroke(
                1.dp, 
                MaterialTheme.colorScheme.error
            )
        } else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = dateTimeString,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@RequiresApi(Build.VERSION_CODES.O)
private fun DateTimePickerDialog(
    title: String,
    datePickerState: DatePickerState,
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (Long, LocalTime) -> Unit
) {
    val context = LocalContext.current
    var selectedTime by remember { mutableStateOf(initialTime) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { dateMillis ->
                        showTimePicker = true
                    } ?: onDismiss()
                }
            ) {
                Text("Continuer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
    
    if (showTimePicker) {
        LaunchedEffect(Unit) {
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    selectedTime = LocalTime.of(hourOfDay, minute)
                    datePickerState.selectedDateMillis?.let { dateMillis ->
                        onConfirm(dateMillis, selectedTime)
                    }
                    showTimePicker = false
                },
                selectedTime.hour,
                selectedTime.minute,
                true
            ).show()
        }
    }
}

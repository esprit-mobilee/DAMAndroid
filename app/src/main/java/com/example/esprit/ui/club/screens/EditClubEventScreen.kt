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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.esprit.repository.EventPayload
import com.example.esprit.ui.club.ClubEventsViewModel
import com.example.esprit.ui.club.EditEventViewModel
import com.example.esprit.util.Constants
import com.example.esprit.util.MultipartUtil
import com.example.esprit.util.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone

@Composable
private fun DateTimePickerCard(
    label: String,
    dateTimeString: String,
    onClick: () -> Unit,
    isError: Boolean = false,
    errorText: String? = null
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
            errorText?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
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

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditClubEventScreen(
    eventId: String,
    onBack: () -> Unit,
    onUpdated: () -> Unit,
    editViewModel: EditEventViewModel = hiltViewModel(),
    eventsViewModel: ClubEventsViewModel = hiltViewModel()
) {
    val saving by editViewModel.saving.collectAsState()
    val eventsState by eventsViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        eventsViewModel.load()
    }

    val event = eventsState.events.find { it.id == eventId }

    // Initialize state from event
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf<com.example.esprit.model.Location?>(null) }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var hasImageChanged by remember { mutableStateOf(false) }
    
    // Location picker state
    var showLocationPicker by remember { mutableStateOf(false) }

    // Parse dates from event
    val now = System.currentTimeMillis()
    var startDateMillis by remember { mutableStateOf(now) }
    var startTime by remember { mutableStateOf(LocalTime.now()) }
    var showStartDateTimePicker by remember { mutableStateOf(false) }

    var endDateMillis by remember { mutableStateOf(now + (60 * 60 * 1000)) }
    var endTime by remember { mutableStateOf(LocalTime.now().plusHours(1)) }
    var showEndDateTimePicker by remember { mutableStateOf(false) }

    // Update state when event is loaded
    LaunchedEffect(event) {
        event?.let {
            title = it.title
            location = it.location
            description = it.description ?: ""
            category = it.category ?: ""
            
            // Parse start date
            it.startDate?.let { startDateStr ->
                try {
                    val instant = Instant.parse(startDateStr)
                    startDateMillis = instant.toEpochMilli()
                    startTime = instant.atZone(ZoneId.systemDefault()).toLocalTime()
                } catch (e: Exception) {
                    // Keep defaults
                }
            }
            
            // Parse end date
            it.endDate?.let { endDateStr ->
                try {
                    val instant = Instant.parse(endDateStr)
                    endDateMillis = instant.toEpochMilli()
                    endTime = instant.atZone(ZoneId.systemDefault()).toLocalTime()
                } catch (e: Exception) {
                    // Keep defaults
                }
            }
        }
    }

    val startDatePickerState = rememberDatePickerState(initialSelectedDateMillis = startDateMillis)
    val endDatePickerState = rememberDatePickerState(initialSelectedDateMillis = endDateMillis)

    // Update date picker states when dates change
    LaunchedEffect(startDateMillis) {
        startDatePickerState.selectedDateMillis = startDateMillis
    }
    LaunchedEffect(endDateMillis) {
        endDatePickerState.selectedDateMillis = endDateMillis
    }

    // Validation errors
    var titleError by remember { mutableStateOf<String?>(null) }
    var startDateError by remember { mutableStateOf<String?>(null) }
    var endDateError by remember { mutableStateOf<String?>(null) }

    fun formatDateForDisplay(millis: Long, time: LocalTime): String {
        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
        val dayOfWeek = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.FRENCH)
        val month = date.month.getDisplayName(java.time.format.TextStyle.SHORT, Locale.FRENCH)
        return "$dayOfWeek ${date.dayOfMonth} $month ${date.year} à ${String.format("%02d:%02d", time.hour, time.minute)}"
    }

    fun formatDateForBackend(millis: Long, time: LocalTime): String {
        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
        val dateTime = date.atTime(time)
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toString()
    }

    val startDateString = formatDateForDisplay(startDateMillis, startTime)
    val endDateString = formatDateForDisplay(endDateMillis, endTime)

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            hasImageChanged = true
        }
    }

    fun updateEvent() {
        titleError = null
        startDateError = null
        endDateError = null
        var hasError = false

        if (title.isBlank()) {
            titleError = "Le titre est obligatoire"
            hasError = true
        }

        val startLocalDate = Instant.ofEpochMilli(startDateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        val endLocalDate = Instant.ofEpochMilli(endDateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        val startDateTime = startLocalDate.atTime(startTime)
        val endDateTime = endLocalDate.atTime(endTime)

        if (endDateTime.isBefore(startDateTime) || endDateTime.isEqual(startDateTime)) {
            endDateError = "La date de fin doit être postérieure à la date de début"
            hasError = true
        }

        if (hasError) return

        val locationPart = if (location != null) {
            val json = com.google.gson.Gson().toJson(location)
            MultipartUtil.textPart(json)
        } else null

        val payload = EventPayload(
            title = MultipartUtil.textPart(title.trim()),
            startDate = MultipartUtil.textPart(formatDateForBackend(startDateMillis, startTime)),
            endDate = MultipartUtil.textPart(formatDateForBackend(endDateMillis, endTime)),
            location = locationPart,
            description = MultipartUtil.nullableTextPart(description.trim().takeIf { it.isNotBlank() }),
            category = MultipartUtil.nullableTextPart(category.trim().takeIf { it.isNotBlank() }),
            image = if (hasImageChanged) imageUri?.let { MultipartUtil.uriToImagePart(context, it) } else null
        )

        scope.launch {
            when (val res = editViewModel.update(eventId, payload)) {
                is UiState.Success -> {
                    snackbarHostState.showSnackbar("Événement modifié avec succès", duration = SnackbarDuration.Long)
                    delay(500)
                    onUpdated()
                }
                is UiState.Error -> {
                    val errorMsg = res.message ?: "Erreur lors de la modification"
                    snackbarHostState.showSnackbar(errorMsg, duration = SnackbarDuration.Long)
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
                title = { Text("Modifier l'événement") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (event == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Événement introuvable")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; titleError = null },
                    label = { Text("Titre de l'événement") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = titleError != null,
                    supportingText = { titleError?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
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

                DateTimePickerCard(
                    label = "Date et heure de début",
                    dateTimeString = startDateString,
                    onClick = { showStartDateTimePicker = true },
                    isError = startDateError != null,
                    errorText = startDateError
                )

                DateTimePickerCard(
                    label = "Date et heure de fin",
                    dateTimeString = endDateString,
                    onClick = { showEndDateTimePicker = true },
                    isError = endDateError != null,
                    errorText = endDateError
                )

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
                        .heightIn(min = 100.dp),
                    maxLines = 5
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Catégorie") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Image section
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { imagePickerLauncher.launch("image/*") },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (imageUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(imageUri),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            TextButton(onClick = { imageUri = null; hasImageChanged = true }) {
                                 Text("Supprimer l'image")
                            }
                        } else if (event.imageUrl != null) {
                            val fullImageUrl = if (event.imageUrl!!.startsWith("http")) {
                                event.imageUrl!!
                            } else {
                                val baseUrl = Constants.BASE_URL.replace("/api/", "")
                                "$baseUrl${event.imageUrl}"
                            }
                            Image(
                                painter = rememberAsyncImagePainter(fullImageUrl),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            TextButton(onClick = { imageUri = null; hasImageChanged = true }) {
                                Text("Changer l'image")
                            }
                        } else {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text("Ajouter une image")
                        }
                    }
                }

                Button(
                    onClick = { updateEvent() },
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
                    Text("Modifier l'événement")
                }
            }
        }
    }
}


package com.example.esprit.ui.vieetudiante

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.example.esprit.model.Event
import com.example.esprit.ui.shared.ProfileViewModel
import com.example.esprit.util.UiState
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.time.Instant
import java.time.ZoneId

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventFormScreen(
    eventsViewModel: EventsViewModel,
    eventId: String?,                     // 👉 null = create, non-null = edit
    onSaved: () -> Unit,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // load current user once
    LaunchedEffect(Unit) {
        profileViewModel.loadMe()
    }
    val profileState by profileViewModel.uiState.collectAsState()
    val currentUser = profileState.user

    // existing event (when editing)
    val eventsState by eventsViewModel.events.collectAsState()
    val eventToEdit: Event? =
        (eventsState as? UiState.Success<List<Event>>)
            ?.data
            ?.find { it.id == eventId }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    var dateIso by remember { mutableStateOf("") }
    var dateDisplay by remember { mutableStateOf("") }

    var formError by remember { mutableStateOf<String?>(null) }

    // ---------- image state ----------
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            imageUri = uri
        }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    // When we are in edit mode, fill fields once the event is available
    LaunchedEffect(eventId, eventToEdit) {
        if (eventId != null && eventToEdit != null) {
            title = eventToEdit.title.orEmpty()
            description = eventToEdit.description.orEmpty()
            location = eventToEdit.location.orEmpty()
            category = eventToEdit.category.orEmpty()

            val backendDate = eventToEdit.date.orEmpty()
            dateIso = backendDate
            dateDisplay = if ("T" in backendDate) {
                backendDate.substringBefore("T") // e.g. "2025-12-10"
            } else {
                backendDate
            }

            // NOTE: on edit we keep existing image on backend if user does not pick a new one.
            // We do NOT pre-load remote URL into a Uri, only preview is handled server-side in the list.
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            val localDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()

                            dateDisplay = localDate.toString()

                            val instant = localDate
                                .atTime(9, 0)
                                .atZone(ZoneId.systemDefault())
                                .toInstant()

                            dateIso = instant.toString()
                        }
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Annuler")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            if (eventId == null) "Créer un événement" else "Modifier l'événement",
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Titre") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )

        OutlinedTextField(
            value = dateDisplay,
            onValueChange = { /* read only */ },
            label = { Text("Date") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .clickable { showDatePicker = true },
            enabled = false,
            readOnly = true
        )

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Lieu") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )

        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Catégorie") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )

        // --------------------------------------------------
        // IMAGE BANNER (optional)
        // --------------------------------------------------
        Text(
            text = "Image (optionnelle)",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                .clickable { imagePickerLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Image de l'événement",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Text("Touchez pour choisir une image")
            }
        }

        Button(
            onClick = {
                formError = null

                val finalDate = if (dateIso.isNotBlank()) {
                    dateIso
                } else {
                    Instant.now().toString()
                }

                // Convert Uri -> File (in cache) if user picked an image
                val imageFile: File? = imageUri?.let { uri ->
                    copyUriToCache(context, uri)
                }

                if (eventId == null) {
                    // CREATE
                    val organizerId = currentUser?.id
                    if (organizerId == null) {
                        formError = "Utilisateur non chargé, réessayez."
                        return@Button
                    }

                    eventsViewModel.createEvent(
                        title = title,
                        description = description.ifBlank { null },
                        dateIso = finalDate,
                        location = location.ifBlank { null },
                        organizerId = organizerId,
                        category = category.ifBlank { null },
                        imageFile = imageFile,
                        onDone = onSaved
                    )
                } else {
                    // UPDATE
                    eventsViewModel.updateEvent(
                        id = eventId,
                        title = title,
                        description = description.ifBlank { null },
                        dateIso = finalDate,
                        location = location.ifBlank { null },
                        category = category.ifBlank { null },
                        imageFile = imageFile,
                        onDone = onSaved
                    )
                }
            },
            modifier = Modifier.padding(top = 16.dp),
            enabled = title.isNotBlank()
        ) {
            Text(if (eventId == null) "Enregistrer" else "Mettre à jour")
        }

        formError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

/**
 * Copie un Uri (galerie) dans le cache de l’app et retourne un File.
 * Retourne null si quelque chose se passe mal.
 */
private fun copyUriToCache(context: Context, uri: Uri): File? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        if (inputStream != null) {
            val file = File(
                context.cacheDir,
                "event_${System.currentTimeMillis()}.jpg"
            )
            FileOutputStream(file).use { output ->
                inputStream.use { input ->
                    input.copyTo(output)
                }
            }
            file
        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

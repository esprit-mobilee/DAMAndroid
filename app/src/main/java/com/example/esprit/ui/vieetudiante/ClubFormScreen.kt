package com.example.esprit.ui.vieetudiante

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.esprit.model.Club
import com.example.esprit.util.Constants
import com.example.esprit.util.MultipartUtil

@Composable
fun ClubFormScreen(
    clubsViewModel: ClubsViewModel,
    clubId: String? = null,
    onSaved: () -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var presidentIdentifiant by remember { mutableStateOf("") }
    var tagsRaw by remember { mutableStateOf("") }

    var loaded by remember { mutableStateOf(false) }

    // image states
    var pickedImageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImageUrl by remember { mutableStateOf<String?>(null) }

    // gallery picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        pickedImageUri = uri
    }

    // load existing club when editing
    LaunchedEffect(clubId) {
        if (clubId != null && !loaded) {
            clubsViewModel.loadClubById(
                id = clubId,
                onSuccess = { club: Club ->
                    name = club.name ?: ""
                    description = club.description ?: ""
                    presidentIdentifiant = club.president?.toString() ?: ""
                    tagsRaw = club.tags?.joinToString(", ") ?: ""
                    existingImageUrl = club.imageUrl
                    loaded = true
                }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = if (clubId == null) "Créer un club" else "Modifier le club",
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nom du club") },
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
            value = presidentIdentifiant,
            onValueChange = { presidentIdentifiant = it },
            label = { Text("Identifiant Président (ex: PR001)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )

        OutlinedTextField(
            value = tagsRaw,
            onValueChange = { tagsRaw = it },
            label = { Text("Tags (séparés par des virgules)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )

        // ------------------------------------------------------
        // IMAGE PICKER + PREVIEW
        // ------------------------------------------------------
        Text(
            text = "Logo du club",
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .border(
                    BorderStroke(1.dp, androidx.compose.ui.graphics.Color.LightGray),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                )
                .clickable { imagePickerLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            when {
                pickedImageUri != null -> {
                    AsyncImage(
                        model = pickedImageUri,
                        contentDescription = "Logo choisi",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                existingImageUrl != null -> {
                    // backend returns e.g. "/uploads/clubs/xxx.jpg"
                    val base = Constants.BASE_URL.removeSuffix("api/")
                    val fullUrl = base + existingImageUrl

                    AsyncImage(
                        model = fullUrl,
                        contentDescription = "Logo actuel",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                else -> {
                    Text("Touchez pour sélectionner une image")
                }
            }
        }

        // ------------------------------------------------------
        // SAVE BUTTON
        // ------------------------------------------------------
        Button(
            onClick = {
                val tags = tagsRaw
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

                val imagePart = pickedImageUri?.let {
                    MultipartUtil.uriToImagePart(context, it, "image")
                }

                if (clubId == null) {
                    // CREATE
                    clubsViewModel.createClub(
                        name = name,
                        description = description.ifBlank { null },
                        presidentIdentifiant = presidentIdentifiant.ifBlank { null },
                        tags = tags,
                        imagePart = imagePart,
                        onDone = onSaved
                    )
                } else {
                    // UPDATE
                    clubsViewModel.updateClub(
                        id = clubId,
                        name = name,
                        description = description.ifBlank { null },
                        presidentIdentifiant = presidentIdentifiant.ifBlank { null },
                        tags = tags,
                        imagePart = imagePart, // null if user did not pick a new image
                        onDone = onSaved
                    )
                }
            },
            modifier = Modifier.padding(top = 20.dp),
            enabled = name.isNotBlank()
        ) {
            Text(if (clubId == null) "Créer" else "Mettre à jour")
        }
    }
}

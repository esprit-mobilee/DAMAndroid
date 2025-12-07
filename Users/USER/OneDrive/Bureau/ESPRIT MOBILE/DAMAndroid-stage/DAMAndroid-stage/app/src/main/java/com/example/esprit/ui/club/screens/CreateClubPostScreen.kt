package com.example.esprit.ui.club.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.esprit.ui.club.CreatePostViewModel
import com.example.esprit.ui.club.EditPostViewModel
import com.example.esprit.util.MultipartUtil
import com.example.esprit.util.UiState
import com.example.esprit.util.Constants
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateClubPostScreen(
    clubId: String = "",
    postId: String? = null,
    initialContent: String = "",
    initialImageUrl: String? = null,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    createViewModel: CreatePostViewModel = hiltViewModel(),
    editViewModel: EditPostViewModel = hiltViewModel()
) {
    val isEditMode = postId != null
    val saving by if (isEditMode) editViewModel.saving.collectAsState() else createViewModel.saving.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var content by remember { mutableStateOf(initialContent) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImageUrl by remember { mutableStateOf(initialImageUrl) }
    
    // Debug logging
    LaunchedEffect(Unit) {
        android.util.Log.d("CreateClubPostScreen", "isEditMode: $isEditMode")
        android.util.Log.d("CreateClubPostScreen", "initialContent: $initialContent")
        android.util.Log.d("CreateClubPostScreen", "initialImageUrl: $initialImageUrl")
        android.util.Log.d("CreateClubPostScreen", "content state: $content")
        android.util.Log.d("CreateClubPostScreen", "existingImageUrl state: $existingImageUrl")
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        if (uri != null) {
            existingImageUrl = null // Clear existing image when new one is selected
        }
    }

    fun submitPost() {
        if (!isEditMode && clubId.isBlank()) {
            scope.launch { snackbarHostState.showSnackbar("Club introuvable") }
            return
        }
        if (content.isBlank()) {
            scope.launch { snackbarHostState.showSnackbar("Le contenu est obligatoire") }
            return
        }

        scope.launch {
            val payloadContent = MultipartUtil.textPart(content.trim())
            val imagePart = imageUri?.let { MultipartUtil.uriToImagePart(context, it) }
            
            val res = if (isEditMode && postId != null) {
                editViewModel.update(postId, payloadContent, imagePart)
            } else {
                createViewModel.create(clubId, payloadContent, imagePart)
            }
            
            when (res) {
                is UiState.Success -> {
                    val message = if (isEditMode) "Publication modifiée" else "Publication créée"
                    snackbarHostState.showSnackbar(message)
                    onSuccess()
                }
                is UiState.Error -> snackbarHostState.showSnackbar(res.message ?: "Erreur lors de la publication")
                UiState.Loading -> Unit
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Modifier la publication" else "Nouvelle publication") },
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
        if (!isEditMode && clubId.isBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Impossible de créer une publication sans club associé.",
                    textAlign = TextAlign.Center
                )
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Quoi de neuf ?") },
                placeholder = { Text("Partagez un message avec vos membres...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp),
                supportingText = {
                    Text(text = "${content.length}/500")
                },
                maxLines = 6
            )

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
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
                        existingImageUrl != null -> {
                            val fullUrl = if (existingImageUrl!!.startsWith("http")) {
                                existingImageUrl
                            } else {
                                val baseUrl = Constants.BASE_URL.replace("/api/", "")
                                "$baseUrl$existingImageUrl"
                            }
                            Image(
                                painter = rememberAsyncImagePainter(model = fullUrl),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
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
                                text = "Ajouter une photo ou vidéo",
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            RowWithActions(
                onPickImage = { imagePickerLauncher.launch("image/*") },
                onResetImage = { 
                    imageUri = null
                    existingImageUrl = null
                }
            )

            Button(
                onClick = { submitPost() },
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
                Text(if (isEditMode) "Modifier" else "Publier")
            }
        }
    }
}

@Composable
private fun RowWithActions(
    onPickImage: () -> Unit,
    onResetImage: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = onPickImage) {
            Text("Choisir une image")
        }
        TextButton(onClick = onResetImage) {
            Text("Retirer l'image")
        }
    }
}


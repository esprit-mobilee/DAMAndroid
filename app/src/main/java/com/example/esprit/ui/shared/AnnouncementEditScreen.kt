package com.example.esprit.ui.shared


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.esprit.ui.shared.AnnouncementsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementEditScreen(
    navController: NavHostController,
    announcementId: String,
    vm: AnnouncementsViewModel = hiltViewModel()
) {
    val announcements by vm.announcements.collectAsState()
    val existing = announcements.firstOrNull { it.id == announcementId }

    if (existing == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Modifier l’annonce") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFD9352A),
                        titleContentColor = Color.White
                    )
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Annonce introuvable")
            }
        }
        return
    }

    var title by remember { mutableStateOf(existing.title) }
    var content by remember { mutableStateOf(existing.content) }
    var audience by remember { mutableStateOf(existing.audience) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modifier l’annonce") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFD9352A),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5)),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Titre") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Contenu") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            )

            OutlinedTextField(
                value = audience,
                onValueChange = { audience = it },
                label = { Text("Audience") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    vm.updateAnnouncement(existing.id, title, content, audience)
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD9352A),
                    contentColor = Color.White
                )
            ) {
                Text("Enregistrer les modifications")
            }
        }
    }
}

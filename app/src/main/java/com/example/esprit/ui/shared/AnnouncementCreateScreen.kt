package com.example.esprit.ui.shared


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.esprit.ui.shared.AnnouncementsViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementCreateScreen(
    navController: NavHostController,
    vm: AnnouncementsViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var audience by remember { mutableStateOf("Tous") }

    val audienceOptions = listOf("Tous", "Étudiants", "Administration")
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nouvelle annonce") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFD9352A),
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            // Titre
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Titre") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFD9352A),
                    focusedLabelColor = Color(0xFFD9352A)
                )
            )

            // Contenu
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Contenu") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFD9352A),
                    focusedLabelColor = Color(0xFFD9352A)
                )
            )

            // Picker Audience
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = audience,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Audience") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    audienceOptions.forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = {
                                audience = it
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Button
            Button(
                onClick = {
                    vm.addAnnouncement(title, content, audience)
                    navController.popBackStack()
                },
                enabled = title.isNotBlank() && content.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD9352A),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Publier l’annonce")
            }
        }
    }
}


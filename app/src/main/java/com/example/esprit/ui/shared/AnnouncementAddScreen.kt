package com.example.esprit.ui.shared



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun AnnouncementAddScreen(
    navController: NavHostController,
    vm: AnnouncementsViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var audience by remember { mutableStateOf("Tous") }

    val audiences = listOf("Tous", "Étudiants", "Administration")

    var audienceExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nouvelle annonce") },
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
                .fillMaxSize()
                .background(Color(0xFFF8F8F8)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(20.dp))

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // TITRE
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

                    // CONTENU
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

                    // AUDIENCE - DROPDOWN PREMIUM
                    ExposedDropdownMenuBox(
                        expanded = audienceExpanded,
                        onExpandedChange = { audienceExpanded = !audienceExpanded }
                    ) {
                        OutlinedTextField(
                            value = audience,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Audience") },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = audienceExpanded)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFD9352A),
                                focusedLabelColor = Color(0xFFD9352A)
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = audienceExpanded,
                            onDismissRequest = { audienceExpanded = false }
                        ) {
                            audiences.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        audience = option
                                        audienceExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(30.dp))

            val isEnabled = title.isNotBlank() && content.isNotBlank()

            Button(
                onClick = {
                    vm.addAnnouncement(title, content, audience)
                    navController.popBackStack()
                },
                enabled = isEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(55.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEnabled) Color(0xFFD9352A) else Color.LightGray,
                    contentColor = Color.White
                )
            ) {
                Text("Publier l’annonce", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

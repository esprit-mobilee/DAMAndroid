package com.example.esprit.ui.shared


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.esprit.ui.shared.AnnouncementCardPremium
import com.example.esprit.ui.shared.AnnouncementsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementsScreen(
    navController: NavHostController,
    vm: AnnouncementsViewModel = hiltViewModel()
) {
    val announcements by vm.announcements.collectAsState()

    var search by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("Tous") }

    // tri par date DESC
    val sorted = announcements.sortedByDescending { it.createdAt }

    val filtered = sorted.filter {
        (filter == "Tous" || it.audience.equals(filter, true)) &&
                (it.title.contains(search, true) || it.content.contains(search, true))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Annonces officielles") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFD9352A),
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { navController.navigate("announcement_add") }) {
                        Icon(Icons.Default.Add, contentDescription = "Ajouter", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(12.dp)
        ) {

            // SEARCH BAR
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text("Rechercher…", color = Color.Black) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = Color(0xFFD9352A),
                    unfocusedBorderColor = Color.Gray
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(14.dp))

            // FILTERS
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                listOf("Tous", "Étudiants", "Administration").forEach { item ->
                    FilterChip(
                        selected = filter == item,
                        onClick = { filter = item },
                        label = {
                            Text(
                                text = item,
                                color = if (filter == item) Color.White else Color.Black
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFD9352A),
                            containerColor = Color(0xFFEAEAEA)
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // LISTE DES ANNONCES
            LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items(filtered) { ann ->
                    AnnouncementCardPremium(
                        ann = ann,
                        onEdit = { navController.navigate("announcement_edit/${ann.id}") },
                        onDelete = { vm.deleteAnnouncement(ann.id) },
                        onOpen = { navController.navigate("announcement_details/${ann.id}") }
                    )
                }
            }
        }
    }
}

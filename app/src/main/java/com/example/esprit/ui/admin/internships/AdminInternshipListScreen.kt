package com.example.esprit.ui.admin.internships

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.esprit.model.InternshipOffer
import com.example.esprit.ui.nav.Destinations
import com.example.esprit.util.Constants
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminInternshipListScreen(
    navController: NavHostController,
    onAddClick: () -> Unit,
    onEditClick: (String) -> Unit,
    viewModel: AdminInternshipViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // auto-refresh when coming back from create / edit
    LaunchedEffect(navController) {
        val handle = navController.currentBackStackEntry?.savedStateHandle
        handle?.getStateFlow("refreshInternships", false)
            ?.collectLatest { refresh ->
                if (refresh) {
                    viewModel.loadOffers()
                    handle.set("refreshInternships", false)
                }
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Internship Offers") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(Destinations.ADMIN_HOME) {
                            popUpTo(Destinations.ADMIN_HOME) { inclusive = true }
                        }
                    }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back to home"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = Color(0xFFEF4444),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.error != null -> {
                    Text(
                        text = uiState.error ?: "Erreur",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.offers) { offer ->
                            var showDeleteDialog by remember { mutableStateOf(false) }

                            if (showDeleteDialog) {
                                AlertDialog(
                                    onDismissRequest = { showDeleteDialog = false },
                                    title = { Text("Supprimer l'offre") },
                                    text = { Text("Êtes-vous sûr de vouloir supprimer cette offre de stage ?") },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            showDeleteDialog = false
                                            offer.id?.let { viewModel.deleteOffer(it) }
                                        }) { Text("Supprimer") }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showDeleteDialog = false }) { Text("Annuler") }
                                    }
                                )
                            }

                            com.example.esprit.ui.components.ModernInternshipCard(
                                offer = offer,
                                onClick = {
                                    offer.id?.let { id ->
                                        val route = Destinations.INTERNSHIP_DETAILS
                                            .replace("{id}", id)
                                        navController.navigate(route)
                                    }
                                },
                                isAdmin = false  // Don't show edit/delete in list
                            )
                        }
                    }
                }
            }
        }
    }
}

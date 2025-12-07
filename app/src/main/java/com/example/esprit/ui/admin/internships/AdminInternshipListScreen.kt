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
            FloatingActionButton(onClick = onAddClick) {
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.offers) { offer ->
                            InternshipOfferCard(
                                offer = offer,
                                onClick = {
                                    offer.id?.let { id ->
                                        val route = Destinations.INTERNSHIP_DETAILS
                                            .replace("{id}", id)
                                        navController.navigate(route)
                                    }
                                },
                                onEdit = { offer.id?.let { onEditClick(it) } },
                                onDelete = { offer.id?.let { viewModel.deleteOffer(it) } }
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ----------------------------------------------------------------------
   Card – same style as EventCard, but clickable to details
   ---------------------------------------------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InternshipOfferCard(
    offer: InternshipOffer,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer l'offre") },
            text = { Text("Êtes-vous sûr de vouloir supprimer cette offre de stage ?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) { Text("Supprimer") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Annuler") }
            }
        )
    }

    val fullLogoUrl = offer.logoUrl?.let { relative ->
        Constants.BASE_URL
            .removeSuffix("api/")
            .plus(relative.trimStart('/'))
    }

    Card(
        onClick = onClick,   // 👈 card clickable → details
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(Modifier.padding(14.dp)) {

            if (!fullLogoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = fullLogoUrl,
                    contentDescription = "Logo de l'entreprise",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE0E0E0)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = offer.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = offer.company,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = offer.description,
                        color = Color.DarkGray,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Modifier")
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = Color.Red
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Row {
                Tag(offer.location?.address ?: "Lieu inconnu")
                Spacer(Modifier.width(6.dp))
                Tag("${offer.duration} sem.")
                offer.salary?.let {
                    Spacer(Modifier.width(6.dp))
                    Tag("$it DT")
                }
            }
        }
    }
}

@Composable
private fun Tag(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color(0xFFF2F2F2)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

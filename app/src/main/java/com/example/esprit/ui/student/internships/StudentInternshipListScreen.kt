package com.example.esprit.ui.student.internships

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.esprit.model.InternshipOffer
import com.example.esprit.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentInternshipListScreen(
    onOfferClick: (String) -> Unit,
    onFavoritesClick: () -> Unit,
    onSearchClick: () -> Unit,
    onNavigateAIChat: () -> Unit,
    viewModel: StudentInternshipViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Stages") },
                actions = {
                    IconButton(onClick = onFavoritesClick) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "Mes favoris",
                            tint = Color(0xFFD32F2F)
                        )
                    }
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Rechercher"
                        )
                    }
                }
            )
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
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.offers) { offer ->
                            StudentInternshipCard(
                                offer = offer,
                                onClick = {
                                    offer.id?.let { onOfferClick(it) }
                                }
                            )
                        }
                        
                        // AI Chat Button at the bottom
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .clickable { onNavigateAIChat() },
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                shadowElevation = 2.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "✨",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = "Assistant Stage (IA)",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color(0xFF1976D2)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ----------------------------------------------------------------------
   Carte d’offre de stage côté étudiant
   (même style que l’admin, mais SANS icônes edit / delete).
   ---------------------------------------------------------------------- */

@Composable
fun StudentInternshipCard(
    offer: InternshipOffer,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // URL complète du logo (comme pour EventCard / admin side)
    val fullLogoUrl = offer.logoUrl?.let { relative ->
        Constants.BASE_URL
            .removeSuffix("api/")          // "http://IP:3000/"
            .plus(relative.trimStart('/')) // "uploads/internship-offers/xxx.jpg"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(Modifier.padding(14.dp)) {

            // Bandeau image en haut
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

            // Titre / entreprise / description
            Column(Modifier.fillMaxWidth()) {
                Text(
                    text = offer.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = offer.company,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = offer.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(10.dp))

            // Tags (lieu, durée, salaire)
            Row {
                Tag(text = offer.location ?: "Lieu inconnu")
                Spacer(Modifier.width(6.dp))
                Tag(text = "${offer.duration} sem.")
                offer.salary?.let { sal ->
                    Spacer(Modifier.width(6.dp))
                    Tag(text = "$sal DT")
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
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

package com.example.esprit.ui.vieetudiante

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.esprit.model.Club
import com.example.esprit.util.UiState

@Composable
fun ClubDetailsScreen(
    clubId: String,
    clubsViewModel: ClubsViewModel
) {
    val state by clubsViewModel.clubs.collectAsState()
    val club: Club? = (state as? UiState.Success<List<Club>>)
        ?.data
        ?.find { it.id == clubId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (club == null) {
            Text("Club introuvable")
        } else {
            Text(
                text = club.name ?: "Nom non spécifié",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = club.description ?: "Aucune description disponible",
                modifier = Modifier.padding(top = 12.dp)
            )

            // Safely handle nullable president object or id
            Text(
                text = when (club.president) {
                    is String -> "Président ID : ${club.president}"
                    is Map<*, *> -> "Président : ${(club.president as? Map<*, *>)?.get("identifiant") ?: "N/A"}"
                    else -> "Président non défini"
                },
                modifier = Modifier.padding(top = 8.dp)
            )

            // Tags display
            val tags = club.tags?.takeIf { it.isNotEmpty() }?.joinToString(", ")
                ?: "Aucun tag défini"
            Text(
                text = "Tags : $tags",
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = "Actif : ${club.isActive ?: false}",
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}


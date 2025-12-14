package com.example.esprit.ui.shared

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementListScreen() {
    // pour l’instant on met des données statiques
    val announcements = listOf(
        "Nouveau club IA ouvert à l’inscription",
        "Hackathon interne ESPRIT – deadline vendredi",
        "Formations soft skills disponibles"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Annonces") }
            )
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp)
        ) {
            items(announcements.size) { index ->
                Card(
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .fillMaxSize()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = announcements[index],
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Publié par la vie étudiante",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

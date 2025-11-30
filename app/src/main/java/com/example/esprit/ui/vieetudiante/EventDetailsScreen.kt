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
import com.example.esprit.model.Event
import com.example.esprit.util.UiState

@Composable
fun EventDetailsScreen(
    eventId: String,
    eventsViewModel: EventsViewModel
) {
    val state by eventsViewModel.events.collectAsState()
    val event: Event? = (state as? UiState.Success<List<Event>>)
        ?.data
        ?.find { it.id == eventId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (event == null) {
            Text("Évènement introuvable")
        } else {
            Text(
                text = event.title ?: "Évènement sans titre",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = event.date ?: "Date non spécifiée",
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = event.location ?: "Lieu non spécifié",
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = event.category ?: "Catégorie non spécifiée",
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = event.description ?: "Aucune description disponible",
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

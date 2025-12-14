package com.example.esprit.ui.club.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.esprit.ui.club.ClubEventsViewModel
import com.example.esprit.model.club.ClubEventDto
import com.example.esprit.util.Constants

@Composable
fun ClubEventsScreen(
    onCreateEvent: () -> Unit,
    onOpenEvent: (String) -> Unit,
    viewModel: ClubEventsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateEvent) { Text("+") }
        }
    ) { padding ->
        when {
            state.loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text(state.error ?: "Erreur") }
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.events) { event ->
                    EventCard(event) { onOpenEvent(event.id) }
                }
            }
        }
    }
}

@Composable
private fun EventCard(event: ClubEventDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(event.title, style = MaterialTheme.typography.titleMedium)
            Text("${event.date} - ${event.location?.address ?: "Lieu inconnu"}", style = MaterialTheme.typography.bodySmall)
            event.description?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            
            // Display event image if available
            event.imageUrl?.let { imageUrl ->
                val fullImageUrl = if (imageUrl.startsWith("http")) {
                    imageUrl
                } else {
                    // Construct full URL from relative path
                    val baseUrl = Constants.BASE_URL.replace("/api/", "")
                    "$baseUrl$imageUrl"
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = rememberAsyncImagePainter(fullImageUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

package com.example.esprit.ui.shared



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.esprit.ui.shared.AnnouncementsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementDetailsScreen(
    navController: NavHostController,
    announcementId: String,
    vm: AnnouncementsViewModel = hiltViewModel()
) {
    val announcements by vm.announcements.collectAsState()
    val ann = announcements.find { it.id == announcementId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(ann?.title ?: "Annonce") },
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

        if (ann == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Annonce introuvable")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .background(Color(0xFFF5F5F5)),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tag + date
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = ann.audience,
                        color = Color.White,
                        fontSize = MaterialTheme.typography.labelSmall.fontSize,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(Color(0xFFD9352A), shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )

                    Text(
                        text = ann.createdAt.take(10),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                }

                Text(
                    text = ann.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Divider()

                Text(
                    text = ann.content,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

package com.example.esprit.ui.student.home

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.esprit.ui.components.ActionGridItem
import com.example.esprit.ui.components.HeroCard

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudentDashboard(
    onNavigateTimetable: () -> Unit,
    onNavigateAbsences: () -> Unit,
    onNavigateAnnouncements: () -> Unit,
    onNavigateStages: () -> Unit,
    onNavigateClubs: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        HeroCard("Bienvenue dans votre Espace")
        Spacer(Modifier.height(16.dp))

        FlowRow(
            maxItemsInEachRow = 3,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionGridItem("Emploi du temps", onClick = onNavigateTimetable)
            ActionGridItem("Absences", onClick = onNavigateAbsences)
            ActionGridItem("Examens") { /* TODO */ }
            ActionGridItem("Résultats") { /* TODO */ }
            ActionGridItem("Stages", onClick = onNavigateStages)
            ActionGridItem("Annonces", onClick = onNavigateAnnouncements)
            ActionGridItem("Clubs", onClick = onNavigateClubs)
        }
    }
}

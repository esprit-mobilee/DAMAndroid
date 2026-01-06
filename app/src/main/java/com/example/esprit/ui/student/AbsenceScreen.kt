package com.example.esprit.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.esprit.model.AbsenceItem
import com.example.esprit.ui.theme.BgGray
import com.example.esprit.ui.theme.RedPrimary
import com.example.esprit.ui.theme.TextGray
import com.example.esprit.util.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbsenceScreen(
    viewModel: AbsenceViewModel = hiltViewModel()
) {
    // observe ui state from VM
    val uiState by viewModel.uiState.collectAsState()

    // load once
    LaunchedEffect(Unit) {
        viewModel.fetchAbsences()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vos absences", color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RedPrimary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(BgGray),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(color = RedPrimary)
                }

                is UiState.Error -> {
                    Text(
                        text = "Erreur : ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }

                is UiState.Success -> {
                    val absences = state.data
                    if (absences.isEmpty()) {
                        Text(
                            text = "Aucune absence enregistrée 🎉",
                            color = TextGray,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(absences) { absence ->
                                AbsenceCard(absence)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AbsenceCard(absence: AbsenceItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(absence.course, fontWeight = FontWeight.Bold)
            Text("Date : ${absence.date}", color = TextGray)
            Text(
                text = if (absence.justified) "Justifiée ✅" else "Non justifiée ❌",
                color = if (absence.justified)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            )
        }
    }
}

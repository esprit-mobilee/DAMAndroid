package com.example.esprit.ui.shared

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController              // ← IMPORT OBLIGATOIRE
import com.example.esprit.ui.student.StudentViewModel

@Composable
fun AnnouncementListScreen(
    navController: NavHostController,
    viewModel: StudentViewModel = hiltViewModel()
) {
    Text("Annonces (à implémenter)")
}

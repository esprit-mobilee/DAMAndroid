package com.example.esprit.ui.shared

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.esprit.ui.student.StudentViewModel

@Composable
fun AnnouncementListScreen(
    viewModel: StudentViewModel = hiltViewModel() // ou un SharedViewModel séparé
) {
    // tu peux faire un call dédié dans le VM, même structure que timetable
    Text("Annonces (à implémenter)")
}

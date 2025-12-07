package com.example.esprit.ui.club.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.esprit.ui.club.ClubSettingsViewModel

@Composable
fun ClubSettingsScreen(
    onLogout: () -> Unit,
    viewModel: ClubSettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var questions by remember { mutableStateOf(listOf<String>()) }

    LaunchedEffect(Unit) {
        viewModel.load()
    }
    LaunchedEffect(state.club) {
        state.club?.let {
            name = it.name
            description = it.description.orEmpty()
            tags = it.tags?.joinToString(", ") ?: ""
            questions = it.joinFormQuestions ?: emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(androidx.compose.foundation.rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Parametres du club", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nom") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            value = tags,
            onValueChange = { tags = it },
            label = { Text("Tags (séparés par des virgules)") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = { viewModel.update(name, description, tags) }, modifier = Modifier.fillMaxWidth()) { Text("Mettre a jour") }

        Divider()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Autoriser les demandes d'adhésion", style = MaterialTheme.typography.titleMedium)
            Switch(
                checked = state.club?.joinEnabled == true,
                onCheckedChange = { viewModel.toggleJoin() }
            )
        }
        
        Text("Questions du formulaire", style = MaterialTheme.typography.titleMedium)
        
        questions.forEachIndexed { index, question ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = question,
                    onValueChange = { newText ->
                        questions = questions.toMutableList().also { it[index] = newText }
                    },
                    label = { Text("Question ${index + 1}") },
                    modifier = Modifier.weight(1f)
                )
                androidx.compose.material3.IconButton(
                    onClick = {
                        questions = questions.toMutableList().also { it.removeAt(index) }
                    }
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        
        OutlinedButton(
            onClick = { questions = questions + "" },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ajouter une question")
        }
        
        Button(
            onClick = { viewModel.updateJoinQuestions(questions.filter { it.isNotBlank() }) },
            modifier = Modifier.fillMaxWidth(),
            enabled = questions.isNotEmpty()
        ) {
            Text("Sauvegarder les questions")
        }

        Divider()
        
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        state.message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        
        // Bouton de déconnexion
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Se déconnecter", color = MaterialTheme.colorScheme.error)
        }
    }
}

package com.example.esprit.ui.shared


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAnnouncementsScreen(
    senderId: String,           // ID du user ACTUEL (admin/prof)
    onSaved: () -> Unit = {},   // callback quand save => navigate
    viewModel: AiViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {

        // AUDIENCE DROPDOWN
        var expanded by remember { mutableStateOf(false) }
        val options = listOf("students", "administrative staff", "both")
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = state.audience,
                onValueChange = {},
                readOnly = true,
                label = { Text("Target Audience") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            viewModel.setAudience(selectionOption)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.instruction,
            onValueChange = viewModel::setInstruction,
            label = { Text("Instruction (ex: remind about the exam tomorrow)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // GENERATE BUTTON
        Button(
            onClick = { viewModel.generate() },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("✨ Générer 3 annonces IA")
        }

        // LOADING
        if (state.isLoading) {
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        // ERROR
        state.error?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = it, color = Color.Red)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // LISTE DES 3 ANNONCES
        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(state.generated) { index, item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { viewModel.select(index) },
                    border = if (state.selectedIndex == index)
                        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    else null
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(item.title, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(item.content)
                    }
                }
            }
        }

        // SAVE BUTTON
        Button(
            onClick = {
                viewModel.save(senderId) { success ->
                    if (success) onSaved()
                }
            },
            enabled = state.selectedIndex != null && !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("💾 Sauvegarder l'annonce sélectionnée")
        }
    }
}

package com.example.esprit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.esprit.model.club.ClubEventDto

@Composable
fun JoinEventDialog(
    event: ClubEventDto,
    onDismiss: () -> Unit,
    onJoin: (List<Map<String, String>>) -> Unit
) {
    // Si pas de questions, on confirme juste
    val questions = event.formQuestions ?: emptyList()
    var answers by remember { mutableStateOf(List(questions.size) { "" }) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Inscription à ${event.title}") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (questions.isEmpty()) {
                    Text("Voulez-vous confirmer votre inscription ?")
                } else {
                    questions.forEachIndexed { index, question ->
                        OutlinedTextField(
                            value = answers[index],
                            onValueChange = { newVal ->
                                answers = answers.toMutableList().also { it[index] = newVal }
                            },
                            label = { Text(question) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val formattedAnswers = questions.mapIndexed { index, q ->
                        mapOf("question" to q, "answer" to answers[index])
                    }
                    onJoin(formattedAnswers)
                },
                enabled = questions.isEmpty() || answers.all { it.isNotBlank() }
            ) {
                Text("Confirmer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
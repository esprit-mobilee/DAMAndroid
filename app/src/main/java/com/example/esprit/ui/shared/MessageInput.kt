package com.example.esprit.ui.shared

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.esprit.utils.PermissionUtils

@Composable
fun MessageInput(
    onSend: (String) -> Unit,
    onStartRecord: () -> Unit,
    onStopRecord: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {

        // Champ texte
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Tapez votre message...") }
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Bouton Micro / Stop
        IconButton(
            onClick = {
                if (!isRecording) {     // start recording
                    if (PermissionUtils.hasPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        )
                    ) {
                        onStartRecord()
                        isRecording = true
                    } else {
                        PermissionUtils.requestPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        )
                    }
                } else {                // stop recording
                    onStopRecord()
                    isRecording = false
                }
            }
        ) {
            Icon(
                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = if (isRecording) "Stop" else "Record"
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Bouton Envoyer texte
        IconButton(
            onClick = {
                if (text.isNotBlank()) {
                    onSend(text)
                    text = ""
                }
            }
        ) {
            Icon(Icons.Default.Send, contentDescription = "Send")
        }
    }
}

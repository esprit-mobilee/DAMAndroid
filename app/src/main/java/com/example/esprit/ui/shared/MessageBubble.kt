package com.example.esprit.ui.shared

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.esprit.model.Message
import com.example.esprit.ui.theme.RedPrimary

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: Message,
    onReact: (String) -> Unit
) {
    Column(
        horizontalAlignment = if (message.isMine)
            Alignment.End else Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {

        Surface(
            color = if (message.isMine) RedPrimary else Color.White,
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(10.dp),
                color = if (message.isMine) Color.White else Color.Black
            )
        }

        // ❤️ REACTIONS SOUS LE MESSAGE
        if (message.reactions.isNotEmpty()) {
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                message.reactions.forEach { reaction ->
                    Text(text = reaction.emoji)
                }
            }
        }
    }
}

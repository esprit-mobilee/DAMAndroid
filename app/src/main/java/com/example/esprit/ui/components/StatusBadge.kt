package com.example.esprit.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun StatusBadge(status: String) {
    val (color, text) = when (status) {
        "APPROVED" -> Color(0xFF4CAF50) to "Approuvé"
        "REJECTED" -> Color(0xFFF44336) to "Rejeté"
        else -> Color(0xFFFF9800) to "En attente"
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        contentColor = color,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

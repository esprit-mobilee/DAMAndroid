package com.example.esprit.ui.shared



import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.example.esprit.model.AnnouncementDto

@Composable
fun AnnouncementCardPremium(
    ann: AnnouncementDto,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onOpen: () -> Unit
) {
    val accentColor = when (ann.audience.lowercase()) {
        "étudiants" -> Color(0xFFD9352A)
        "administration" -> Color(0xFFF4A261)
        else -> Color(0xFFC8A2FF)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFFEEEE),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(12.dp)) {

            // Bande verticale
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .fillMaxHeight()
                    .background(accentColor, RoundedCornerShape(8.dp))
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {

                Text(
                    text = ann.audience,
                    color = accentColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = ann.title,
                    color = Color(0xFF222222),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
                )

                Text(
                    text = ann.content,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3
                )

                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = (ann.createdAt ?: "Aucune date").take(10),
                        color = Color.DarkGray,
                        style = MaterialTheme.typography.labelSmall
                    )

                    Text(
                        text = "Lire plus →",
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onOpen() }
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = Color(0xFFFF9800))
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color.Red)
                }
            }
        }
    }
}

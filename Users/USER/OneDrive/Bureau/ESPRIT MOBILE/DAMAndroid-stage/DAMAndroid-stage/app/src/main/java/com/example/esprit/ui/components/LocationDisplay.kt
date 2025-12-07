package com.example.esprit.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.esprit.model.Location

/**
 * Compact location display component for list views
 * Shows address in a user-friendly format with location icon
 */
@Composable
fun LocationDisplay(
    location: Location?,
    modifier: Modifier = Modifier,
    showCoordinates: Boolean = false,
    textColor: Color = Color.Gray
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Location",
            tint = textColor,
            modifier = Modifier.size(16.dp)
        )
        
        when {
            // Has address - show it
            !location?.address.isNullOrBlank() -> {
                Text(
                    text = location?.address ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor
                )
            }
            // Has coordinates but no address - show formatted coordinates
            location?.latitude != null && location.longitude != null && showCoordinates -> {
                Text(
                    text = formatCoordinates(location.latitude, location.longitude),
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor
                )
            }
            // No location data
            else -> {
                Text(
                    text = "Lieu non spécifié",
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Light
                )
            }
        }
    }
}

/**
 * Format coordinates in a user-friendly way
 * Example: 36.81°N, 10.18°E
 */
private fun formatCoordinates(latitude: Double, longitude: Double): String {
    val latDirection = if (latitude >= 0) "N" else "S"
    val lonDirection = if (longitude >= 0) "E" else "W"
    
    return "%.2f°%s, %.2f°%s".format(
        kotlin.math.abs(latitude),
        latDirection,
        kotlin.math.abs(longitude),
        lonDirection
    )
}

package com.example.esprit.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.esprit.model.InternshipOffer
import com.example.esprit.util.Constants

object EspritCardColors {
    // Charte Graphique Esprit (Red Theme)
    val GradientStart = Color(0xFFB71C1C) // Deep Red
    val GradientEnd = Color(0xFFD32F2F)   // Esprit Red
    val TitleText = Color(0xFF1F2937)
    val SubtitleText = Color(0xFF4B5563)
    val BodyText = Color(0xFF6B7280)
    val ChipBg = Color(0xFFFFEBEE)        // Light Red Tint
    val PrimaryAccent = Color(0xFFD32F2F) // Esprit Red
}

@Composable
fun ModernInternshipCard(
    offer: InternshipOffer,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isAdmin: Boolean = false,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        label = "card_scale"
    )

    val fullLogoUrl = offer.logoUrl?.let { relative ->
        Constants.BASE_URL
            .removeSuffix("api/")
            .plus(relative.trimStart('/'))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            
            // --- TOP SECTON: Cover + Floating Logo ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp) // Hauteur augmentée pour plus d'impact visuel
            ) {
                // 1. Cover Background (Logo Image or Red Gradient)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(105.dp) // Background cover height
                        .background(
                             brush = Brush.horizontalGradient(
                                colors = listOf(
                                    EspritCardColors.GradientStart,
                                    EspritCardColors.GradientEnd
                                )
                            )
                        )
                ) {
                    // Try to show the logo as a background cover if available
                    if (!fullLogoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = fullLogoUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(0.2f), // Subtle background effect
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Pattern / Decoration
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.1f)) // Slight dim
                    )
                    
                    // Initiales "Esprit" style top-right text (Decorative)
                    Text(
                        text = offer.company.take(2).uppercase(),
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .offset(x = 20.dp, y = 10.dp)
                    )

                    // Admin Actions on top right
                    if (isAdmin) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SmallActionBtn(icon = Icons.Default.Edit, onClick = onEdit, tint = EspritCardColors.PrimaryAccent)
                            SmallActionBtn(icon = Icons.Default.Delete, onClick = onDelete, tint = Color.Red)
                        }
                    }
                }

                // 2. Floating Logo (Overlapping & LARGER)
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart) 
                        .padding(start = 24.dp) 
                        .size(90.dp) // Largeur augmentée (90dp)
                        .shadow(10.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(3.dp, Color.White)
                ) {
                    if (!fullLogoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = fullLogoUrl,
                            contentDescription = "Logo",
                            modifier = Modifier.fillMaxSize().padding(4.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color(0xFFF3F4F6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = offer.company.take(1).uppercase(),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = EspritCardColors.PrimaryAccent
                            )
                        }
                    }
                }
            }

            // --- CONTENT SECTION ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp)
            ) {
                // Titre
                Text(
                    text = offer.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = EspritCardColors.TitleText,
                    lineHeight = 28.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Entreprise
                Text(
                    text = offer.company,
                    style = MaterialTheme.typography.titleMedium,
                    color = EspritCardColors.SubtitleText,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    text = offer.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = EspritCardColors.BodyText,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(20.dp))

                // --- FOOTER CHIPS ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!offer.locationAddress.isNullOrBlank()) {
                        EspritChip(
                            icon = Icons.Default.LocationOn,
                            text = offer.locationAddress!!,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                    
                    EspritChip(
                        icon = Icons.Default.AccessTime,
                        text = "${offer.duration} sem."
                    )
                    
                    if (offer.salary != null && offer.salary > 0) {
                        EspritChip(
                            icon = Icons.Default.AttachMoney,
                            text = "${offer.salary} DT",
                            highlight = true
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun SmallActionBtn(
    icon: ImageVector,
    onClick: (() -> Unit)?,
    tint: Color
) {
    if (onClick == null) return
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.size(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun EspritChip(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    // Red-themed chips
    val bgColor = if (highlight) Color(0xFFFFF7ED) else EspritCardColors.ChipBg
    val contentColor = if (highlight) Color(0xFFD32F2F) else Color(0xFFB71C1C) // Red tones
    val borderColor = if (highlight) Color.Transparent else Color.Transparent

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


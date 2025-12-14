package com.example.esprit.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Facebook-style emoji reaction picker with animations
 */
@Composable
fun EmojiReactionPicker(
    onEmojiSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val emojis = listOf("👍", "❤️", "😂", "😮", "😢", "😡")
    
    // Animation for popup appearance
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    Surface(
        modifier = modifier.scale(scale),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            emojis.forEachIndexed { index, emoji ->
                EmojiReactionButton(
                    emoji = emoji,
                    onClick = { onEmojiSelected(emoji) },
                    delay = index * 30L
                )
            }
        }
    }
}

@Composable
private fun EmojiReactionButton(
    emoji: String,
    onClick: () -> Unit,
    delay: Long = 0L
) {
    var visible by remember { mutableStateOf(false) }
    var pressed by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delay)
        visible = true
    }
    
    val scale by animateFloatAsState(
        targetValue = when {
            pressed -> 1.3f
            visible -> 1f
            else -> 0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "emoji_scale"
    )
    
    Box(
        modifier = Modifier
            .size(40.dp)
            .scale(scale)
            .background(
                color = if (pressed) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else Color.Transparent,
                shape = CircleShape
            )
            .clickable {
                pressed = true
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = 24.sp
        )
    }
    
    // Reset pressed state after animation
    LaunchedEffect(pressed) {
        if (pressed) {
            kotlinx.coroutines.delay(150)
            pressed = false
        }
    }
}

/**
 * Display reactions on a comment with count
 */
@Composable
fun CommentReactions(
    reactions: List<Pair<String, Int>>, // emoji to count
    currentUserReaction: String?,
    onReactionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (reactions.isEmpty()) return
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        reactions.forEach { (emoji, count) ->
            val isSelected = emoji == currentUserReaction
            
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clickable { onReactionClick(emoji) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = emoji,
                        fontSize = 14.sp
                    )
                    if (count > 1) {
                        Text(
                            text = count.toString(),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

package com.example.esprit.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.esprit.util.VoiceRecognitionHelper

/**
 * Custom dialog for voice recognition with real-time feedback
 * Shows listening animation, partial results, and clear error messages
 */
@Composable
fun VoiceRecognitionDialog(
    onResult: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    // State
    var state by remember { mutableStateOf(VoiceState.INITIALIZING) }
    var partialText by remember { mutableStateOf("") }
    var errorInfo by remember { mutableStateOf<VoiceRecognitionHelper.VoiceError?>(null) }
    var audioLevel by remember { mutableFloatStateOf(0f) }
    
    // Voice recognition helper
    val voiceHelper = remember {
        VoiceRecognitionHelper(
            context = context,
            onResult = { text ->
                state = VoiceState.SUCCESS
                onResult(text)
            },
            onError = { error ->
                state = VoiceState.ERROR
                errorInfo = error
            },
            onPartialResult = { text ->
                partialText = text
            },
            onReadyForSpeech = {
                state = VoiceState.LISTENING
            },
            onEndOfSpeech = {
                state = VoiceState.PROCESSING
            },
            onRmsChanged = { rms ->
                // RMS value typically ranges from 0 to 10
                audioLevel = (rms / 10f).coerceIn(0f, 1f)
            }
        )
    }
    
    // Start listening when dialog opens
    LaunchedEffect(Unit) {
        if (VoiceRecognitionHelper.isAvailable(context)) {
            // Small delay to ensure SpeechRecognizer is fully initialized
            kotlinx.coroutines.delay(300)
            
            // Call startListening on main thread
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                voiceHelper.startListening()
            }
            
            // Timeout fallback - if still initializing after 5 seconds, show error
            kotlinx.coroutines.delay(5000)
            if (state == VoiceState.INITIALIZING) {
                state = VoiceState.ERROR
                errorInfo = VoiceRecognitionHelper.VoiceError(
                    code = -3,
                    title = "Délai dépassé",
                    message = "Le service de reconnaissance vocale ne répond pas",
                    suggestion = "Vérifiez que l'application Google est installée et à jour, puis réessayez"
                )
            }
        } else {
            state = VoiceState.ERROR
            errorInfo = VoiceRecognitionHelper.VoiceError(
                code = -1,
                title = "Service non disponible",
                message = "La reconnaissance vocale n'est pas disponible",
                suggestion = "Installez l'application Google depuis le Play Store"
            )
        }
    }
    
    // Cleanup when dialog closes
    DisposableEffect(Unit) {
        onDispose {
            voiceHelper.destroy()
        }
    }
    
    Dialog(
        onDismissRequest = {
            voiceHelper.cancel()
            onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = {
                            voiceHelper.cancel()
                            onDismiss()
                        }
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Fermer",
                            tint = Color.Gray
                        )
                    }
                }
                
                // Microphone icon with animation
                AnimatedMicrophoneIcon(
                    state = state,
                    audioLevel = audioLevel
                )
                
                // Status text
                Text(
                    text = when (state) {
                        VoiceState.INITIALIZING -> "Préparation..."
                        VoiceState.LISTENING -> "Parlez maintenant"
                        VoiceState.PROCESSING -> "Traitement..."
                        VoiceState.SUCCESS -> "Terminé !"
                        VoiceState.ERROR -> errorInfo?.title ?: "Erreur"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = when (state) {
                        VoiceState.ERROR -> MaterialTheme.colorScheme.error
                        VoiceState.SUCCESS -> Color(0xFF4CAF50)
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                
                // Partial results or error message
                when (state) {
                    VoiceState.LISTENING, VoiceState.PROCESSING -> {
                        if (partialText.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                )
                            ) {
                                Text(
                                    text = partialText,
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        } else {
                            Text(
                                text = "Dites par exemple:\n\"développeur web\" ou \"stage marketing\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    VoiceState.ERROR -> {
                        errorInfo?.let { error ->
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = error.message,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        
                                        Divider()
                                        
                                        Text(
                                            text = "💡 ${error.suggestion}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                                
                                Button(
                                    onClick = {
                                        state = VoiceState.INITIALIZING
                                        partialText = ""
                                        errorInfo = null
                                        voiceHelper.startListening()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Réessayer")
                                }
                            }
                        }
                    }
                    
                    else -> {}
                }
                
                // Cancel button (except when error or success)
                if (state != VoiceState.ERROR && state != VoiceState.SUCCESS) {
                    TextButton(
                        onClick = {
                            voiceHelper.cancel()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Annuler")
                    }
                }
            }
        }
    }
}

/**
 * Animated microphone icon that pulses while listening
 */
@Composable
private fun AnimatedMicrophoneIcon(
    state: VoiceState,
    audioLevel: Float
) {
    // Pulsing animation when listening
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    // Audio level based scale
    val audioScale = 1f + (audioLevel * 0.3f)
    
    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer circle (pulsing background)
        if (state == VoiceState.LISTENING) {
            Box(
                modifier = Modifier
                    .size(120.dp * scale)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
            )
        }
        
        // Inner circle with mic icon
        Box(
            modifier = Modifier
                .size(80.dp * if (state == VoiceState.LISTENING) audioScale else 1f)
                .background(
                    color = when (state) {
                        VoiceState.ERROR -> MaterialTheme.colorScheme.error
                        VoiceState.SUCCESS -> Color(0xFF4CAF50)
                        else -> MaterialTheme.colorScheme.primary
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Microphone",
                modifier = Modifier.size(40.dp),
                tint = Color.White
            )
        }
    }
}

/**
 * Voice recognition states
 */
private enum class VoiceState {
    INITIALIZING,
    LISTENING,
    PROCESSING,
    SUCCESS,
    ERROR
}

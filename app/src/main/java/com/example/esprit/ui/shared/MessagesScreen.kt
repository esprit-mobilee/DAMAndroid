package com.example.esprit.ui.shared



import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

// =====================================================
// UI MODEL
// =====================================================

data class ConversationUi(
    val id: String,
    val title: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int = 0,
    val type: ConversationType = ConversationType.DIRECT,
    val roleLabel: String? = null,      // Professor / Student / Parent
    val isHighlighted: Boolean = false  // pour la première ligne (comme ton design)
)

enum class ConversationType { DIRECT, ANNOUNCEMENT, GROUP, BOT }

// =====================================================
// MAIN SCREEN
// =====================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(navController: NavHostController) {

    var searchQuery by remember { mutableStateOf("") }

    // Palette proche d’Esprit + design que tu as montré
    val backgroundColor = Color(0xFFF4F5F7)
    val highlightColor = Color(0xFFFFE5E5)       // ligne sélectionnée
    val accentRed = Color(0xFFD9352A)

    // Conversations (statique pour l’instant)
    val allConversations = remember {
        listOf(
            // 🔴 Ligne en surbrillance (comme Dr Evelyn dans ton design)
            ConversationUi(
                id = "691e2126d4558f41c78b085a",   // Ahmed
                title = "Prof. Ahmed Ben Salem",
                lastMessage = "Merci, je vais corriger votre devoir.",
                time = "10:45 AM",
                unreadCount = 2,
                type = ConversationType.DIRECT,
                roleLabel = "Professor",
                isHighlighted = true
            ),
            ConversationUi(
                id = "691e24db7a1a6b2eb5bc6617",   // Manel
                title = "Manel",
                lastMessage = "Peut-on décaler la soutenance ?",
                time = "9:30 AM",
                unreadCount = 0,
                type = ConversationType.DIRECT,
                roleLabel = "Student"
            ),
            ConversationUi(
                id = "annonces",
                title = "Annonces officielles",
                lastMessage = "Nouvelle annonce de la direction.",
                time = "Yesterday",
                unreadCount = 2,
                type = ConversationType.ANNOUNCEMENT,
                roleLabel = "Administration"
            ),
            ConversationUi(
                id = "group48ima",
                title = "Groupe 4SIM4",
                lastMessage = "Révision projet Foyer ce soir ?",
                time = "Yesterday",
                unreadCount = 3,
                type = ConversationType.GROUP
            ),
            ConversationUi(
                id = "bot",
                title = "EspritBot",
                lastMessage = "Comment puis-je vous aider ?",
                time = "2d ago",
                unreadCount = 0,
                type = ConversationType.BOT
            )
        )
    }

    val conversations = allConversations.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
                it.lastMessage.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            MessagesTopBar(
                backgroundColor = backgroundColor
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* plus tard: nouveau message */ },
                containerColor = accentRed
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nouveau message", tint = Color.White)
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // Barre de recherche
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text("Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(4.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(conversations) { conv ->
                    ConversationRow(
                        conv = conv,
                        accentRed = accentRed,
                        highlightColor = highlightColor,
                        onClick = {
                            when (conv.type) {
                                ConversationType.ANNOUNCEMENT ->
                                    navController.navigate("announcements")

                                ConversationType.DIRECT -> {
                                    val encodedName = Uri.encode(conv.title)
                                    navController.navigate("chat/${conv.id}/$encodedName")
                                }

                                ConversationType.GROUP -> {
                                    // plus tard : chat de groupe
                                }

                                ConversationType.BOT -> {
                                    val encoded = Uri.encode("EspritBot")
                                    navController.navigate("chat/bot/$encoded")
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

// =====================================================
// TOP BAR
// =====================================================

@Composable
private fun MessagesTopBar(
    backgroundColor: Color
) {
    Surface(
        color = backgroundColor,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar profil en haut (cercle dégradé)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(
                                Color(0xFF6A85F1),
                                Color(0xFF9B5CF6)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "M",           // initiale de l’utilisateur connecté
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Messages",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = { /* search avancée plus tard */ }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            }
        }
    }
}

// =====================================================
// CONVERSATION ROW
// =====================================================

@Composable
fun ConversationRow(
    conv: ConversationUi,
    accentRed: Color,
    highlightColor: Color,
    onClick: () -> Unit
) {
    val cardBackground = if (conv.isHighlighted) highlightColor else Color.White

    Surface(
        color = cardBackground,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shadowElevation = if (conv.isHighlighted) 1.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E4EC)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = conv.title.first().uppercase(),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = Color(0xFF3A3F4C)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conv.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (conv.roleLabel != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        RoleChip(label = conv.roleLabel)
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = conv.lastMessage,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF8A8F9C)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = conv.time,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF9BA0AC),
                        fontSize = 11.sp
                    )
                )

                if (conv.unreadCount > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(accentRed)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = conv.unreadCount.toString(),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// =====================================================
// ROLE CHIP
// =====================================================

@Composable
private fun RoleChip(label: String) {
    val bg = when (label.lowercase()) {
        "professor", "prof", "professeur" -> Color(0xFFE8ECFF)
        "student", "étudiant", "etudiant" -> Color(0xFFE8FFF1)
        "parent" -> Color(0xFFFFF4E5)
        else -> Color(0xFFEDEFF3)
    }

    val textColor = when (label.lowercase()) {
        "professor", "prof", "professeur" -> Color(0xFF3F51B5)
        "student", "étudiant", "etudiant" -> Color(0xFF2E7D32)
        "parent" -> Color(0xFFEF6C00)
        else -> Color(0xFF5F6470)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

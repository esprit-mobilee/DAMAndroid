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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.esprit.ui.nav.Destinations
import com.example.esprit.util.DataStoreManager

// ================================
// UI MODEL
// ================================
data class ConversationUi(
    val id: String,
    val title: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int = 0,
    val roleLabel: String? = null,
    val isHighlighted: Boolean = false,
    val type: String = "DIRECT"
)

// ================================
// MAIN SCREEN
// ================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    navController: NavHostController,
    vm: ConversationsViewModel = hiltViewModel()
) {
    println("🟢 MessagesScreen COMPOSED")
    val context = LocalContext.current
    val dataStore = remember { DataStoreManager(context) }
    val currentUserId by dataStore.userIdFlow.collectAsState(initial = null)

    LaunchedEffect(currentUserId) {
        println("🟡 [MessagesScreen] currentUserId = $currentUserId")

        if (currentUserId.isNullOrBlank()) {
            println("❌ userId is NULL → load() NOT called")
            return@LaunchedEffect
        }

        println("✅ userId OK → calling vm.load($currentUserId)")
        vm.load(currentUserId!!)
    }


    var searchQuery by remember { mutableStateOf("") }

    val conversations = vm.items.map { item ->
        when (item) {
            is UnifiedConversation.Direct -> {
                ConversationUi(
                    id = item.data.userId,
                    title = item.data.fullName ?: "Utilisateur",
                    lastMessage = item.data.lastMessage ?: "",
                    time = item.data.lastMessageTime ?: "",
                    roleLabel = item.data.role,
                    type = "DIRECT"
                )
            }
            is UnifiedConversation.Club -> {
                ConversationUi(
                    id = item.data.id,
                    title = item.data.name,
                    lastMessage = "Discussion de groupe",
                    time = "", // TODO: Fetch separate time or ignore
                    roleLabel = "Club",
                    type = "CLUB"
                )
            }
        }
    }.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
                it.lastMessage.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = { MessagesTopBar() },

        // ➕ Nouvelle conversation
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Destinations.CONTACT_LIST)
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nouvelle conversation")
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // 🔍 Recherche
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text("Rechercher") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                singleLine = true
            )

            if (vm.loading.value) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (conversations.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Aucune conversation")
                }
            } else {
                // 📩 Liste des conversations
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(conversations) { conv ->
                        ConversationRow(
                            conv = conv,
                            onClick = {
                                val encodedName = Uri.encode(conv.title)
                                if (conv.type == "CLUB") {
                                    // Club Chat
                                    navController.navigate(
                                        Destinations.clubChatRoute(conv.id, encodedName)
                                    )
                                } else {
                                    // Direct Chat (AI supported)
                                    navController.navigate(
                                        Destinations.privateChatSharedRoute(
                                            peerId = conv.id,
                                            peerName = encodedName
                                        )
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// ================================
// TOP BAR
// ================================
@Composable
fun MessagesTopBar() {
    Surface(color = Color.White, shadowElevation = 4.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(Color(0xFF6A85F1), Color(0xFF9B5CF6))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("M", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.width(12.dp))

            Text(
                "Messages",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ================================
// ROW ITEM
// ================================
@Composable
fun ConversationRow(
    conv: ConversationUi,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E4EC)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    conv.title.first().uppercase(),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = Color(0xFF3A3F4C)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    conv.title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    conv.lastMessage,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                conv.time,
                style = MaterialTheme.typography.labelSmall,
                color = Color.DarkGray
            )
        }
    }
}

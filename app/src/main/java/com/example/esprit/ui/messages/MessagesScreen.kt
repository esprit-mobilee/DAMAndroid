package com.example.esprit.ui.messages

import com.example.esprit.ui.messages.MessagesViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.esprit.model.chat.ConversationDto
import com.example.esprit.ui.nav.Destinations

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    navController: NavController,
    viewModel: MessagesViewModel = hiltViewModel()
) {
    val conversations by viewModel.conversations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadConversations()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Destinations.CONTACT_LIST) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Edit, contentDescription = "New Chat")
            }
        },
        containerColor = MaterialTheme.colorScheme.background 
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 1. Header (Custom, no TopAppBar)
            MessagesHeader()

            // 2. Search Bar
            SearchBarPlaceholder()

            // 3. Content List
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp) // Space for FAB
                ) {
                    // "Active Now" Row
                    item {
                        ActiveUsersRow(conversations)
                    }

                    // Divider/Spacer
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    // Chat List
                    items(conversations) { conversation ->
                        ConversationItem(
                            conversation = conversation,
                            onClick = {
                                val isClub = conversation.partner.role == "CLUB"
                                val fname = conversation.partner.firstName ?: ""
                                val lname = conversation.partner.lastName ?: ""
                                val displayName = if (fname.isBlank() && lname.isBlank()) {
                                    conversation.partner.name ?: "Unknown"
                                } else {
                                    "$fname $lname".trim()
                                }
                                
                                if (isClub) {
                                    navController.navigate(Destinations.clubChatRoute(conversation.partnerId, displayName))
                                } else {
                                    val route = Destinations.privateChatRoute(
                                        conversation.partnerId,
                                        displayName
                                    )
                                    navController.navigate(route)
                                }
                            }
                        )
                    }
                    
                    if (conversations.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("No conversations yet", color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessagesHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
           // User Avatar (Placeholder or injected)
           Surface(
               modifier = Modifier.size(40.dp),
               shape = CircleShape,
               color = Color.LightGray
           ) {
               // Could put actual user image here if available
               Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(8.dp))
           }
           Spacer(modifier = Modifier.width(12.dp))
           Text(
               text = "Chats",
               style = MaterialTheme.typography.headlineMedium,
               fontWeight = FontWeight.Bold
           )
        }
        
        Row {
            IconButton(onClick = { /* TODO Camera */ }) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Camera")
            }
            IconButton(onClick = { /* TODO New Message */ }) {
                Icon(Icons.Default.Edit, contentDescription = "New Message")
            }
        }
    }
}

@Composable
fun SearchBarPlaceholder() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Search", color = Color.Gray)
        }
    }
}

@Composable
fun ActiveUsersRow(conversations: List<ConversationDto>) {
    // Filter for online users (mock logic or real if isOnline is accurate)
    val onlineUsers = conversations.filter { it.partner.isOnline }
    
    if (onlineUsers.isNotEmpty()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(onlineUsers) { conv ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box {
                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                             Box(contentAlignment = Alignment.Center) {
                                 Text(
                                     text = (conv.partner.firstName?.take(1) ?: conv.partner.name?.take(1) ?: "?").uppercase(),
                                     fontWeight = FontWeight.Bold,
                                     fontSize = 20.sp
                                 )
                             }
                        }
                        // Green Dot
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(Color.Green, CircleShape)
                                .align(Alignment.BottomEnd)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.background) // Border effect
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = conv.partner.firstName ?: conv.partner.name ?: "User",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversation: ConversationDto,
    onClick: () -> Unit
) {
    val fname = conversation.partner.firstName ?: ""
    val lname = conversation.partner.lastName ?: ""
    val displayName = if (fname.isBlank() && lname.isBlank()) {
        conversation.partner.name ?: "Unknown"
    } else {
        "$fname $lname".trim()
    }
    
    val isUnread = false // TODO: Add unread boolean to DTO

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box {
             Surface(
                modifier = Modifier.size(60.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                 Box(contentAlignment = Alignment.Center) {
                     Text(
                         text = displayName.take(1).uppercase(),
                         fontWeight = FontWeight.Bold,
                         fontSize = 24.sp
                     )
                 }
            }
            if (conversation.partner.isOnline) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color.Green, CircleShape)
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background) // Border effect
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Medium // Bold if unread
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = conversation.lastMessage.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUnread) MaterialTheme.colorScheme.onSurface else Color.Gray,
                    maxLines = 1,
                    fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = " · ${conversation.lastMessage.createdAt.take(10)}", // Formatting needed
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
        
        // Interaction Status / Unread Dot
        if (isUnread) {
             Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
    }
}

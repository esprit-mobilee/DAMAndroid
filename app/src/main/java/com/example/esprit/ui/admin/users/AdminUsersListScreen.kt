package com.example.esprit.ui.admin.users

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.esprit.model.User
import com.example.esprit.ui.nav.Destinations

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersListScreen(
    navController: NavController,
    viewModel: AdminUsersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // auto-refresh
    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }
    
    // Check for refresh signal from Create Screen
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val refresh by savedStateHandle?.getStateFlow("refreshUsers", false)?.collectAsState() ?: mutableStateOf(false)
    
    LaunchedEffect(refresh) {
        if (refresh) {
            viewModel.loadUsers()
            savedStateHandle?.set("refreshUsers", false)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Gestion Utilisateurs", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Destinations.ADMIN_USER_CREATE) },
                containerColor = Color(0xFFD32F2F),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter")
            }
        },
        containerColor = Color(0xFFF2F2F7) // iOS Light Gray Background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFFD32F2F))
            } else if (uiState.error != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Une erreur est survenue",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = uiState.error ?: "",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(onClick = { viewModel.loadUsers() }) {
                        Text("Réessayer", color = Color(0xFFD32F2F))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.users) { user ->
                        UserItem(
                            user = user,
                            onDelete = {
                                user.id?.let { viewModel.deleteUser(it) }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserItem(user: User, onDelete: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirmer la suppression") },
            text = { Text("Êtes-vous sûr de vouloir supprimer ${user.fullName} ? Cette action est irréversible.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) { Text("Supprimer") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Annuler", color = Color.Black) }
            },
            containerColor = Color.White
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            // Add shadow manually via modifier if needed or verify elevation visibility on Light Gray
            // Elevation 0 with a border or shadow is cleaner. Let's try slight shadow via elevation if standard.
            // Actually, CardDefaults.cardElevation(2.dp) is fine for "modern"
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            UserAvatar(name = user.fullName, role = user.role)
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                // Details Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Role Badge
                    Surface(
                        color = getRoleColor(user.role).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = (user.role ?: "N/A").uppercase(),
                            color = getRoleColor(user.role),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // ID
                    Text(
                        text = "ID: ${user.identifiant ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            
            // Delete Action
            IconButton(onClick = { showDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Delete, 
                    contentDescription = "Delete", 
                    tint = Color(0xFF8E8E93) // iOS Gray
                )
            }
        }
    }
}

@Composable
fun UserAvatar(name: String, role: String?) {
    val initials = name.split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.toString() }
        .joinToString("")
        .uppercase()
        .ifEmpty { "?" }

    val backgroundColor = getRoleColor(role)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(backgroundColor.copy(alpha = 0.2f))
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = backgroundColor
        )
    }
}

fun getRoleColor(role: String?): Color {
    return when (role?.lowercase()) {
        "student", "etudiant" -> Color(0xFFD32F2F) // Red
        "parent" -> Color(0xFF1976D2) // Blue
        "club" -> Color(0xFF388E3C) // Green
        "admin" -> Color.Black
        else -> Color.Gray
    }
}

package com.example.esprit.ui.shared

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.esprit.ui.nav.Destinations
import com.example.esprit.util.DataStoreManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectUserScreen(
    navController: NavHostController,
    vm: SelectUserViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val dataStore = remember { DataStoreManager(context) }
    val currentUserId by dataStore.userIdFlow.collectAsState(initial = null)

    LaunchedEffect(currentUserId) {
        vm.loadUsers(currentUserId)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Nouvelle conversation") })
        }
    ) { padding ->
        if (vm.isLoading.value) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(vm.users) { user ->
                    ListItem(
                        headlineContent = { Text(user.fullName) },
                        supportingContent = { Text(user.role) },
                        modifier = Modifier.clickable {

                            val userId = user.id
                            if (userId.isNullOrBlank()) {
                                // 🛑 Sécurité anti-crash
                                return@clickable
                            }

                            val safeName = user.fullName.ifBlank { "Utilisateur" }
                            val encodedName = Uri.encode(safeName)

                            navController.navigate(
                                Destinations.CHAT
                                    .replace("{peerId}", userId)
                                    .replace("{peerName}", encodedName)
                            ) {
                                popUpTo(Destinations.SELECT_USER) { inclusive = true }
                            }
                        }

                    )
                    Divider()
                }
            }
        }
    }
}

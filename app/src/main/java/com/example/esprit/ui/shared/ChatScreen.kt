package com.example.esprit.ui.shared

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavHostController,
    peerId: String,
    peerName: String
) {
    val vm: ChatViewModel = hiltViewModel()

    LaunchedEffect(peerId) {
        vm.init(peerId)
    }

    val messages by vm.filteredMessages.collectAsState()
    val searchQuery by vm.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(peerName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        },
        bottomBar = {
            MessageInput(
                onSend = { vm.sendMessage(it) },
                onStartRecord = { vm.startRecording() },
                onStopRecord = { vm.stopRecording() }        // ← MANQUAIT
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Top
        ) {

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { vm.onSearchQueryChanged(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    placeholder = { Text("Rechercher un message...") }
                )
            }

            items(messages, key = { it.id }) { message ->
                MessageBubble(message = message)
            }
        }
    }
}


package com.example.esprit.ui.shared

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.esprit.util.DataStoreManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavHostController,
    peerId: String,
    peerName: String
) {
    val vm: ChatViewModel = hiltViewModel()

    val context = LocalContext.current
    val dataStore = remember { DataStoreManager(context) }

    var currentUserId by remember { mutableStateOf("") }

    val messages by vm.messages.collectAsState()
    val summary by vm.summary.collectAsState()

    // Récupérer userId depuis DataStore
    LaunchedEffect(Unit) {
        val id = dataStore.getUserId()
        currentUserId = id
        println("🔥 UI → currentUserId LOADED = $id")
    }

    // Initialisation du ViewModel quand userId est prêt
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotBlank()) {
            println("🔥 UI → INIT ViewModel with user=$currentUserId peer=$peerId")
            vm.setCurrentUser(currentUserId)
            vm.init(peerId, currentUserId)
        }
    }

    // =============================
    // POPUP RÉSUMÉ IA
    // =============================
    if (summary != null) {
        AlertDialog(
            onDismissRequest = { vm.clearSummary() },
            title = { Text("Résumé de la conversation") },
            text = { Text(summary?.summary ?: "Aucun contenu") },
            confirmButton = {
                TextButton(onClick = { vm.clearSummary() }) {
                    Text("Fermer")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(peerName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        println("⚡ UI: Summarize clicked")
                        vm.summarizeAll()
                    }) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI Summary")
                    }
                }
            )
        },
        bottomBar = {
            MessageInput(
                onSend = { vm.sendMessage(it) },
                onStartRecord = { vm.startRecording() },
                onStopRecord = { vm.stopRecording() }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            items(messages) { msgItem ->
                MessageBubble(
                    message = msgItem,
                    onReact = { emoji ->
                        println("😀 Reaction $emoji on message ${msgItem.id}")
                        // (on branchera l’API juste après)
                    }
                )

            }
        }
    }
}

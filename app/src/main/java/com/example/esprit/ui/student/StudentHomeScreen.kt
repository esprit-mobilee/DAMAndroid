package com.example.esprit.ui.student

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.esprit.model.Role
import com.example.esprit.ui.components.*
import com.example.esprit.ui.shared.ProfileViewModel
import com.example.esprit.ui.student.clubs.StudentClubsScreen
import com.example.esprit.ui.student.feed.StudentFeedScreen
import com.example.esprit.ui.student.home.StudentDashboard
import com.example.esprit.ui.theme.BgGray
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentHomeScreen(
    onNavigateTimetable: () -> Unit,
    onNavigateAbsences: () -> Unit,
    onNavigateAnnouncements: () -> Unit,
    onNavigateProfile: () -> Unit,
    onNavigateStages: () -> Unit,
    onNavigateClubs: () -> Unit,
    onNavigateMessages: () -> Unit,
    onLogout: () -> Unit,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) { profileViewModel.loadMe() }
    val ui by profileViewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Normalize roles
    val userRoles = ui.user?.roles?.map { it.uppercase() } ?: listOf(ui.user?.role?.uppercase() ?: "STUDENT")
    val primaryRole = try { Role.valueOf(userRoles.firstOrNull() ?: "STUDENT") } catch (_: Exception) { Role.STUDENT }

    val destinations = buildList {
        add(
            DrawerDestination(
                label = "Profil",
                icon = { Icon(Icons.Default.Person, null) },
                onClick = { scope.launch { drawerState.close() }; onNavigateProfile() }
            )
        )
        add(
            DrawerDestination(
                label = "Messages",
                icon = { Icon(Icons.Default.Chat, contentDescription = "Messages") },
                onClick = { 
                     scope.launch { drawerState.close() }
                     onNavigateMessages()
                }
            )
        )
    }
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            EspritDrawer(
                userName = ui.user?.name ?: "Étudiant ESPRIT",
                subtitle = ui.user?.classGroup ?: "",
                role = primaryRole,
                destinations = destinations,
                onLogout = { scope.launch { drawerState.close() }; onLogout() }
            )
        }
    ) {
        Scaffold(
            topBar = {
                EspritTopBar(
                    title = "ESPRIT",
                    subtitle = "Espace Étudiant",
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            },
            containerColor = BgGray
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                StudentDashboard(
                    onNavigateTimetable = onNavigateTimetable,
                    onNavigateAbsences = onNavigateAbsences,
                    onNavigateAnnouncements = onNavigateAnnouncements,
                    onNavigateStages = onNavigateStages,
                    onNavigateClubs = onNavigateClubs
                )
            }
        }
    }
}

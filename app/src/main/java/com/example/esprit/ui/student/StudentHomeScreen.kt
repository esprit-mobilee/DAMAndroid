package com.example.esprit.ui.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.esprit.model.Role
import com.example.esprit.ui.components.ActionGridItem
import com.example.esprit.ui.components.DrawerDestination
import com.example.esprit.ui.components.EspritDrawer
import com.example.esprit.ui.components.EspritTopBar
import com.example.esprit.ui.components.HeroCard
import com.example.esprit.ui.shared.ProfileViewModel
import com.example.esprit.ui.theme.BgGray
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentHomeScreen(
    onNavigateTimetable: () -> Unit,
    onNavigateAbsences: () -> Unit,
    onNavigateAnnouncements: () -> Unit,
    onNavigateProfile: () -> Unit,
    onLogout: () -> Unit,
    // 👇 we inject the VM here
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    // call /me once
    LaunchedEffect(Unit) {
        profileViewModel.loadMe()
    }
    val ui = profileViewModel.uiState.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            EspritDrawer(
                // 👇 real name if backend sent it, fallback to old text
                userName = ui.value.user?.fullName ?: "Étudiant ESPRIT",
                // 👇 real class/group if present, else empty
                subtitle = ui.value.user?.classGroup ?: "",
                role = when (ui.value.user?.role?.uppercase()) {
                    "TEACHER" -> Role.TEACHER
                    "PARENT" -> Role.PARENT
                    "ADMIN" -> Role.ADMIN
                    else -> Role.STUDENT
                },
                destinations = listOf(
                    DrawerDestination(
                        label = "Profil",
                        icon = { Icon(Icons.Default.Person, null) },
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateProfile()
                        }
                    ),
                    DrawerDestination(
                        label = "Messages",
                        icon = { Icon(Icons.Default.Email, null) },
                        onClick = {
                            // TODO messages
                            scope.launch { drawerState.close() }
                        }
                    ),
                    DrawerDestination(
                        label = "Emploi du temps",
                        icon = { Icon(Icons.Default.CalendarToday, null) },
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateTimetable()
                        }
                    )
                ),
                onLogout = {
                    scope.launch { drawerState.close() }
                    onLogout()
                }
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
        ) { inner ->
            Column(
                modifier = Modifier
                    .padding(inner)
                    .padding(16.dp)
            ) {
                HeroCard("Bienvenue dans votre Espace")

                Spacer(Modifier.height(16.dp))

                FlowRow(
                    maxItemsInEachRow = 3,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionGridItem("Emploi du temps", onClick = onNavigateTimetable)
                    ActionGridItem("Absences", onClick = onNavigateAbsences)
                    ActionGridItem("Examens") { /* TODO */ }
                    ActionGridItem("Résultats") { /* TODO */ }
                    ActionGridItem("Stages") { /* TODO */ }
                    ActionGridItem("Annonces", onClick = onNavigateAnnouncements)
                }
            }
        }
    }
}

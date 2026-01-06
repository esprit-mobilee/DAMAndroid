package com.example.esprit.ui.teacher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Announcement
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.esprit.model.Role
import com.example.esprit.ui.components.ActionGridItem
import com.example.esprit.ui.components.DrawerDestination
import com.example.esprit.ui.components.EspritDrawer
import com.example.esprit.ui.components.EspritTopBar
import com.example.esprit.ui.components.HeroCard
import com.example.esprit.ui.theme.BgGray
import kotlinx.coroutines.launch

import androidx.compose.foundation.layout.ExperimentalLayoutApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TeacherHomeScreen(
    onLogout: () -> Unit,
    onNavigateProfile: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            EspritDrawer(
                userName = "Enseignant ESPRIT",
                subtitle = "2025/2026",
                role = Role.TEACHER,
                destinations = listOf(
                    DrawerDestination(
                        label = "Profil",
                        icon = { androidx.compose.material3.Icon(Icons.Default.Person, null) },
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateProfile()
                        }
                    ),
                    DrawerDestination(
                        label = "Mes cours",
                        icon = { androidx.compose.material3.Icon(Icons.Default.Schedule, null) },
                        onClick = { scope.launch { drawerState.close() } }
                    ),
                    DrawerDestination(
                        label = "Annonces",
                        icon = { androidx.compose.material3.Icon(Icons.Default.Announcement, null) },
                        onClick = { scope.launch { drawerState.close() } }
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
                    subtitle = "Espace Enseignant",
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
                    ActionGridItem("Mes cours") { /* TODO */ }
                    ActionGridItem("Examens") { /* TODO */ }
                    ActionGridItem("Présence") { /* TODO */ }
                    ActionGridItem("Annonces") { /* TODO */ }
                }
            }
        }
    }
}

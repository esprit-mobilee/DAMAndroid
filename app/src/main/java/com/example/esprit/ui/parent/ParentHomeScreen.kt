package com.example.esprit.ui.parent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Announcement
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentHomeScreen(
    onLogout: () -> Unit,
    onNavigateProfile: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            EspritDrawer(
                userName = "Parent ESPRIT",
                subtitle = "Responsable étudiant",
                role = Role.PARENT,
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
                        label = "Absences enfant",
                        icon = { androidx.compose.material3.Icon(Icons.Default.School, null) },
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
                    subtitle = "Espace Parents",
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
                    ActionGridItem("Suivi absences") { /* TODO */ }
                    ActionGridItem("Résultats") { /* TODO */ }
                    ActionGridItem("Annonces") { /* TODO */ }
                }
            }
        }
    }
}

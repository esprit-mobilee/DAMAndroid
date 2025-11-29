package com.example.esprit.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications   // Annonces
import androidx.compose.material.icons.filled.Group          // Utilisateurs
import androidx.compose.material.icons.filled.Person         // Profil
import androidx.compose.material.icons.filled.Work           // Stages
import androidx.compose.material.icons.filled.Event          // Événements
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdminHomeScreen(
    onLogout: () -> Unit,
    onNavigateProfile: () -> Unit = {},
    onNavigateUsers: () -> Unit = {},
    onNavigateAnnouncements: () -> Unit = {},
    onNavigateStages: () -> Unit = {},
    onNavigateEvents: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            EspritDrawer(
                userName = "Administrateur",
                subtitle = "ESPRIT",
                role = Role.ADMIN,
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
                        label = "Utilisateurs",
                        icon = { Icon(Icons.Default.Group, null) },
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateUsers()
                        }
                    ),
                    DrawerDestination(
                        label = "Annonces",
                        icon = { Icon(Icons.Default.Notifications, null) },
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateAnnouncements()
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
                    subtitle = "Espace Administration",
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

                    ActionGridItem(
                        label = "Utilisateurs",
                        icon = { Icon(Icons.Default.Group, contentDescription = null) },
                        onClick = { onNavigateUsers() }
                    )

                    ActionGridItem(
                        label = "Annonces",
                        icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                        onClick = { onNavigateAnnouncements() }
                    )

                    ActionGridItem(
                        label = "Stages",
                        icon = { Icon(Icons.Default.Work, contentDescription = null) },
                        onClick = { onNavigateStages() }
                    )

                    ActionGridItem(
                        label = "Événements",
                        icon = { Icon(Icons.Default.Event, contentDescription = null) },
                        onClick = { onNavigateEvents() }
                    )
                }
            }
        }
    }
}

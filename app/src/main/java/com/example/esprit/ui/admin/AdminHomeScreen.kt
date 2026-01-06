package com.example.esprit.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Announcement
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.esprit.model.Role
import com.example.esprit.ui.components.ActionGridItem
import com.example.esprit.ui.components.DrawerDestination
import com.example.esprit.ui.components.EspritDrawer
import com.example.esprit.ui.components.EspritTopBar
import com.example.esprit.ui.components.HeroCard
import com.example.esprit.ui.components.StatCard
import com.example.esprit.ui.notifications.NotificationBellIcon
import com.example.esprit.ui.notifications.NotificationsViewModel
import com.example.esprit.ui.shared.ProfileViewModel
import com.example.esprit.ui.theme.BgGray
import kotlinx.coroutines.launch

import androidx.compose.foundation.layout.ExperimentalLayoutApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdminHomeScreen(
    onLogout: () -> Unit,
    onNavigateProfile: () -> Unit = {},
    onNavigateVieEtudiante: () -> Unit = {},
    onNavigateStages: () -> Unit = {},
    onNavigateApplications: () -> Unit = {},
    onNavigateToRequests: () -> Unit = {},
    onNavigateUsers: () -> Unit = {},
    onNavigateMessages: () -> Unit = {},
    onNavigateNotifications: () -> Unit = {},
    profileViewModel: ProfileViewModel = hiltViewModel(),
    notificationsViewModel: NotificationsViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        profileViewModel.loadMe()
        notificationsViewModel.fetchNotifications()
    }
    val ui by profileViewModel.uiState.collectAsState()
    val unreadCount by notificationsViewModel.unreadCount.collectAsState()

    // Connect socket when user is loaded
    LaunchedEffect(ui.user) {
        ui.user?.id?.let { userId ->
            notificationsViewModel.connectSocket(userId)
        }
    }

    val drawerState = rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            EspritDrawer(
                userName = ui.user?.fullName ?: "Administrateur",
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
                        icon = { Icon(Icons.Default.Groups, null) },
                        onClick = { scope.launch { drawerState.close() } }
                    ),
                    DrawerDestination(
                        label = "Annonces",
                        icon = { Icon(Icons.Default.Announcement, null) },
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
                    subtitle = "Espace Administration",
                    onMenuClick = { scope.launch { drawerState.open() } },
                    actions = {
                        Spacer(Modifier.width(8.dp))
                        NotificationBellIcon(
                            unreadCount = unreadCount,
                            onClick = onNavigateNotifications
                        )
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Home,
                                contentDescription = "Accueil",
                            )
                        },
                        label = {
                            Text(
                                "Accueil",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        selected = true,
                        onClick = { /* Already on home */ },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFD32F2F),
                            selectedTextColor = Color(0xFFD32F2F),
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = Color.LightGray,
                            unselectedTextColor = Color.LightGray
                        )
                    )
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.ChatBubble,
                                contentDescription = "Msgs",
                            )
                        },
                        label = {
                            Text(
                                "Msgs",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        selected = false,
                        onClick = onNavigateMessages,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFD32F2F),
                            selectedTextColor = Color(0xFFD32F2F),
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = Color.LightGray,
                            unselectedTextColor = Color.LightGray
                        )
                    )

                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                            )
                        },
                        label = {
                            Text(
                                "Profile",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        selected = false,
                        onClick = onNavigateProfile,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFD32F2F),
                            selectedTextColor = Color(0xFFD32F2F),
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = Color.LightGray,
                            unselectedTextColor = Color.LightGray
                        )
                    )
                }
            },
            containerColor = BgGray
        ) { inner ->
            Column(
                modifier = Modifier
                    .padding(inner)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                HeroCard("Bienvenue dans votre Espace")
                Spacer(Modifier.height(20.dp))
                
                // Stats Section
                Text(
                    text = "Vue d'ensemble",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF212121)
                )
                Spacer(Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(value = "24", label = "Utilisateurs", color = Color(0xFFD32F2F))
                    StatCard(value = "12", label = "Stages", color = Color(0xFFD32F2F))
                    StatCard(value = "8", label = "Candidatures", color = Color(0xFFD32F2F))
                }
                
                Spacer(Modifier.height(24.dp))
                
                Text(
                    text = "Actions rapides",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF212121)
                )
                Spacer(Modifier.height(12.dp))

                FlowRow(
                    maxItemsInEachRow = 3,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionGridItem(
                        label = "Utilisateurs",
                        icon = {
                            Icon(
                                Icons.Default.Groups,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    ) { onNavigateUsers() }
                    
                    ActionGridItem(
                        label = "Annonces",
                        icon = {
                            Icon(
                                Icons.Default.Announcement,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    ) { /* TODO */ }

                    ActionGridItem(
                        label = "Stages",
                        icon = {
                            Icon(
                                Icons.Default.Work,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    ) {
                        onNavigateStages()
                    }

                    ActionGridItem(
                        label = "Événements",
                        icon = {
                            Icon(
                                Icons.Default.Event,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    ) { /* TODO */ }

                    ActionGridItem(
                        label = "Clubs & vie étudiante",
                        icon = {
                            Icon(
                                Icons.Default.Group,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    ) {
                        onNavigateVieEtudiante()
                    }

                    ActionGridItem(
                        label = "Candidatures",
                        icon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    ) {
                        onNavigateApplications()
                    }

                    ActionGridItem(
                        label = "Demandes",
                        icon = {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    ) {
                        onNavigateToRequests()
                    }
                }
            }
        }
    }
}

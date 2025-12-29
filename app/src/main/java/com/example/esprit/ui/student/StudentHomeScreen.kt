package com.example.esprit.ui.student

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.esprit.ui.shared.ProfileViewModel
import com.example.esprit.ui.theme.BgGray
import kotlinx.coroutines.launch

import androidx.compose.foundation.layout.ExperimentalLayoutApi

import com.example.esprit.ui.notifications.NotificationBellIcon
import com.example.esprit.ui.notifications.NotificationsViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StudentHomeScreen(
    onNavigateTimetable: () -> Unit,
    onNavigateAbsences: () -> Unit,
    onNavigateAnnouncements: () -> Unit,
    onNavigateProfile: () -> Unit,
    onNavigateStages: () -> Unit,
    onNavigateAIChat: () -> Unit,
    onNavigateClubs: () -> Unit,
    onNavigateMessages: () -> Unit,
    onNavigateDocumentRequests: () -> Unit,
    onNavigateClubChat: (String) -> Unit, // New callback
    onLogout: () -> Unit,
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

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val userRoles = ui.user?.roles ?: emptyList()
    val primaryRole = userRoles.firstOrNull() ?: Role.STUDENT


    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            val destinations = buildList {
                add(
                    DrawerDestination(
                        label = "Profil",
                        icon = { Icon(Icons.Default.Person, null) },
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateProfile()
                        }
                    )
                )
                add(
                    DrawerDestination(
                        label = "Messages",
                        icon = { Icon(Icons.Default.Email, null) },
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateMessages()
                        }
                    )
                )
                // CLUB CHAT
                val clubId = ui.user?.presidentOf ?: ui.user?.club
                if (!clubId.isNullOrBlank()) {
                    add(
                        DrawerDestination(
                            label = "Discussion de groupe",
                            icon = { Icon(Icons.Default.Email, null) },
                            onClick = {
                                scope.launch { drawerState.close() }
                                onNavigateClubChat(clubId)
                            }
                        )
                    )
                }
                add(
                    DrawerDestination(
                        label = "Emploi du temps",
                        icon = { Icon(Icons.Default.CalendarToday, null) },
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateTimetable()
                        }
                    )
                )

            }

            EspritDrawer(
                userName = ui.user?.fullName ?: "Étudiant ESPRIT",
                subtitle = ui.user?.classGroup ?: "",
                role = primaryRole,
                destinations = destinations,
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
                    onMenuClick = { scope.launch { drawerState.open() } },
                    actions = {
                        NotificationBellIcon(
                            unreadCount = unreadCount,
                            onClick = onNavigateNotifications
                        )
                    }
                )
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
                    text = "Mon activité",
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
                    StatCard(value = "5", label = "Absences", color = Color(0xFFD32F2F))
                    StatCard(value = "3", label = "Stages", color = Color(0xFFD32F2F))
                    StatCard(value = "2", label = "Candidatures", color = Color(0xFFD32F2F))
                }
                
                Spacer(Modifier.height(24.dp))
                
                Text(
                    text = "Accès rapide",
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
                        label = "Emploi du temps",
                        icon = {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(28.dp)
                            )
                        },
                        onClick = onNavigateTimetable
                    )
                    
                    ActionGridItem(
                        label = "Absences",
                        icon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(28.dp)
                            )
                        },
                        onClick = onNavigateAbsences
                    )
                    
                    ActionGridItem(
                        label = "Examens",
                        icon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    ) { /* TODO */ }
                    
                    ActionGridItem(
                        label = "Résultats",
                        icon = {
                            Icon(
                                Icons.Default.Person,
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
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(28.dp)
                            )
                        },
                        onClick = onNavigateStages
                    )

                    ActionGridItem(
                        label = "Annonces",
                        icon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(28.dp)
                            )
                        },
                        onClick = onNavigateAnnouncements
                    )
                    ActionGridItem(
                        label = "CLUBS",
                        icon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(28.dp)
                            )
                        },
                        onClick = onNavigateClubs
                    )


                    ActionGridItem(
                        label = "Assistant Stage (IA)",
                        icon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(28.dp)
                            )
                        },
                        onClick = onNavigateAIChat
                    )

                    ActionGridItem(
                        label = "Demandes Documents",
                        icon = {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(28.dp)
                            )
                        },
                        onClick = onNavigateDocumentRequests
                    )
                }
            }
        }
    }
}

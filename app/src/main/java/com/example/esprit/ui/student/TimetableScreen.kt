package com.example.esprit.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.esprit.model.TimetableItem
import com.example.esprit.model.Role
import com.example.esprit.ui.components.EspritTopBar
import com.example.esprit.ui.components.EspritDrawer
import com.example.esprit.ui.components.DrawerDestination
import com.example.esprit.ui.theme.BgGray
import com.example.esprit.ui.theme.TextGray
import com.example.esprit.util.Resource
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    viewModel: StudentViewModel = hiltViewModel(),
    onNavigateHome: () -> Unit = {},
    onNavigateProfile: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val state by viewModel.timetable.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            EspritDrawer(
                userName = "Étudiant ESPRIT",
                subtitle = "SIM / 4",
                role = Role.STUDENT,
                destinations = listOf(
                    DrawerDestination(
                        label = "Accueil",
                        icon = { Icon(Icons.Default.Home, null) },
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateHome()
                        }
                    ),
                    DrawerDestination(
                        label = "Profil",
                        icon = { Icon(Icons.Default.Person, null) },
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateProfile()
                        }
                    ),
                    DrawerDestination(
                        label = "Emploi du temps",
                        icon = { Icon(Icons.Default.CalendarToday, null) },
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
                    subtitle = "Emploi du temps",
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            },
            containerColor = BgGray
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(BgGray)
            ) {
                when (state) {
                    is Resource.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is Resource.Error -> {
                        Text(
                            text = (state as Resource.Error).message,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            color = Color.Red
                        )
                    }

                    is Resource.Success -> {
                        val data = (state as Resource.Success<List<TimetableItem>>).data
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(data) { item ->
                                TimetableCard(item)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimetableCard(item: TimetableItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = item.course,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${item.start} - ${item.end}  •  Salle ${item.room}",
                color = TextGray,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Ens. ${item.teacher}",
                color = TextGray,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

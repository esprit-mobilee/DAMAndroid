package com.example.esprit.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.esprit.ui.theme.RedPrimary
import com.example.esprit.ui.theme.RedDark

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onChangePassword: (String, String) -> Unit,
    onBack: () -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            snackbarHostState.showSnackbar("Mot de passe mis à jour avec succès ✅")
            oldPassword = ""
            newPassword = ""
            confirmPassword = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mon Profil", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF8F9FA) // Light gray background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            state = androidx.compose.foundation.lazy.rememberLazyListState()
        ) {
            // 1. Header Card
            item {
                ProfileHeader(uiState.user)
            }

            // 2. AI Profile Section
            if (uiState.aiProfile != null) {
                item {
                    AiProfileSection(uiState.aiProfile)
                }
            }

            // 3. Security Section REMOVED as per user request
            
            item {
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ProfileHeader(user: com.example.esprit.model.User?) {
    val gradient = Brush.linearGradient(
        colors = listOf(RedPrimary, RedDark)
    )

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            // Colored Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(gradient)
            )

            // Content
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .offset(y = (-50).dp), // Pull up avatar
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Determine Display Name
                val displayedName = if (!user?.firstName.isNullOrBlank() || !user?.lastName.isNullOrBlank()) {
                    "${user?.firstName.orEmpty()} ${user?.lastName.orEmpty()}".trim()
                } else if (!user?.name.isNullOrBlank()) {
                    user?.name
                } else {
                    // Fallback but NOT email if possible, or keep duplicate if really no name
                    "Utilisateur ESPRIT" 
                }

                // Avatar
                Surface(
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(4.dp, Color.White),
                    modifier = Modifier.size(100.dp),
                    color = Color(0xFFE0E0E0)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = displayedName?.take(1)?.uppercase() ?: "U",
                            style = MaterialTheme.typography.headlineLarge,
                            color = RedDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Name (Bold)
                Text(
                    text = displayedName ?: "Nom inconnu",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                // Role Chip
                val role = user?.roles?.firstOrNull()?.name?.replace("ROLE_", "") ?: "Étudiant"
                Surface(
                    color = RedPrimary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = role,
                        color = RedPrimary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Info Rows
                ProfileInfoRow(Icons.Rounded.Email, user?.email ?: "email@esprit.tn")
                if (!user?.classGroup.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    ProfileInfoRow(Icons.Rounded.School, user!!.classGroup!!)
                }
            }
        }
    }
}

@Composable
fun ProfileInfoRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiProfileSection(profile: com.example.esprit.model.AiProfile) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        
        // Section Label
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = RedPrimary)
            Spacer(Modifier.width(8.dp))
            Text(
                "Profil IA",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                
                // Summary Quote
                if (!profile.summary.isNullOrBlank()) {
                    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                         Box(
                             modifier = Modifier
                                 .width(4.dp)
                                 .fillMaxHeight()
                                 .background(RedPrimary.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                         )
                         Spacer(Modifier.width(12.dp))
                         Text(
                             text = profile.summary,
                             style = MaterialTheme.typography.bodyMedium,
                             fontStyle = FontStyle.Italic,
                             color = Color(0xFF424242),
                             lineHeight = 22.sp
                         )
                    }
                }

                // Skills Chips
                if (profile.skills.isNotEmpty()) {
                    Column {
                        SectionHeader("Compétences Clés", Icons.Rounded.Verified)
                        Spacer(Modifier.height(12.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            profile.skills.forEach { skill ->
                                AssistChip(
                                    onClick = {},
                                    label = { Text(skill) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = Color(0xFFF5F5F5),
                                        labelColor = Color.Black
                                    ),
                                    border = null
                                )
                            }
                        }
                    }
                }

                // Experience List
                if (profile.experience.isNotEmpty()) {
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                    Column {
                        SectionHeader("Expérience", Icons.Rounded.Work)
                        Spacer(Modifier.height(12.dp))
                        profile.experience.forEach { exp ->
                            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                Icon(
                                    Icons.Rounded.Business, 
                                    contentDescription = null, 
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp).offset(y = 2.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = exp,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = RedPrimary)
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.labelLarge, color = Color.Gray, fontWeight = FontWeight.SemiBold)
    }
}

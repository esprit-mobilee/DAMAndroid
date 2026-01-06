package com.example.esprit.ui.admin.users

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.esprit.model.Role

@Composable
fun AdminUserCreateScreen(
    navController: NavController,
    viewModel: AdminUsersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Form state
    var selectedRole by remember { mutableStateOf(Role.STUDENT) }
    var name by remember { mutableStateOf("") }
    var identifiant by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var classGroup by remember { mutableStateOf("") }
    
    // Helper to generate password
    fun generatePassword(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..8).map { chars.random() }.joinToString("")
    }

    // Helper to generate ID
    fun generateId(role: Role): String {
        val r1 = (100..999).random()
        val r2 = (100..999).random()
        return when (role) {
            Role.STUDENT -> "${r1}ST${r2}"
            Role.CLUB -> "${r1}CLB${r2}"
            Role.PARENT -> "${r1}PR${r2}"
            else -> ""
        }
    }

    // Update ID when role changes
    LaunchedEffect(selectedRole) {
        identifiant = generateId(selectedRole)
        if (password.isBlank()) { 
             password = generatePassword() 
        }
    }

    // Initial check
    LaunchedEffect(Unit) {
        if (identifiant.isBlank()) identifiant = generateId(selectedRole)
        if (password.isBlank()) password = generatePassword()
    }
    
    // Handle Success
    LaunchedEffect(uiState.createSuccess) {
        if (uiState.createSuccess) {
            viewModel.resetCreateState()
            navController.previousBackStackEntry?.savedStateHandle?.set("refreshUsers", true)
            navController.popBackStack()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF2F2F7) // iOS Light Gray Background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            // Back Button (Custom or just Text)
            // The mockup shows "Gestion Utilisateurs" as a large title, likely standard navigation, 
            // but we'll add a back arrow for usability if not in a native nav stack
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.offset(x = (-12).dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }

            // Title
            Text(
                text = "Gestion Utilisateurs",
                style = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Ajoutez de nouveaux étudiants, parents ou clubs.",
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Type de compte Section
            Text(
                text = "Type de compte",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Role Selection Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RoleCard(
                    title = "Étudiant",
                    icon = Icons.Default.School,
                    selected = selectedRole == Role.STUDENT,
                    onClick = { selectedRole = Role.STUDENT },
                    modifier = Modifier.weight(1f)
                )
                RoleCard(
                    title = "Parent",
                    icon = Icons.Default.Person, // Or SupervisorAccount
                    selected = selectedRole == Role.PARENT,
                    onClick = { selectedRole = Role.PARENT },
                    modifier = Modifier.weight(1f)
                )
                RoleCard(
                    title = "Club",
                    icon = Icons.Default.Groups,
                    selected = selectedRole == Role.CLUB,
                    onClick = { selectedRole = Role.CLUB },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Inputs
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                
                // Identifiant
                CustomTextField(
                    value = identifiant,
                    onValueChange = { identifiant = it },
                    placeholder = "Identifiant",
                    icon = Icons.Default.AccountBox // Safer than Badge
                )

                // Nom complet
                CustomTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Nom complet",
                    icon = Icons.Default.Person
                )

                // Email
                CustomTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "Email (optionnel)",
                    icon = Icons.Default.Email
                )

                // Password with Refresh
                Row(
                   modifier = Modifier.fillMaxWidth(),
                   horizontalArrangement = Arrangement.spacedBy(8.dp),
                   verticalAlignment = Alignment.CenterVertically
                ) {
                    CustomTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "Mot de passe",
                        icon = Icons.Default.Lock,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Refresh Button
                    FilledIconButton(
                        onClick = { password = generatePassword() },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color(0xFFD32F2F) // Red accent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(50.dp) // Match height of text field approx
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Password",
                            tint = Color.White
                        )
                    }
                }

                // Student specific fields (Classes / Student ID)
                if (selectedRole == Role.STUDENT) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CustomTextField(
                            value = classGroup,
                            onValueChange = { classGroup = it },
                            placeholder = "Classe (opt)",
                            icon = Icons.Default.Home, // School might be missing, safe fallback if needed, but let's try School first? No, let's use Home -> specific instruction says use Info for Tag. 
                            // Actually, I'll stick with School for now, and check if it fails. But for Tag I'll use Info.
                            // Wait, previous file had School? No.
                            // I'll use Home for Classe as a safe bet for "Classroom/Home".
                            modifier = Modifier.weight(1f)
                        )
                        
                        CustomTextField(
                            value = "", 
                            onValueChange = {}, 
                            placeholder = "ID Etudiant...",
                            icon = Icons.Default.Info, // Tag might be missing
                            modifier = Modifier.weight(1f),
                            enabled = false 
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Error Message
             if (uiState.createError != null) {
                Text(
                    text = uiState.createError ?: "",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Submit Button
            Button(
                onClick = {
                    val payload = mutableMapOf<String, Any>(
                        "name" to name,
                        "identifiant" to identifiant,
                        "password" to password,
                        "role" to selectedRole.name.lowercase()
                    )
                    if (email.isNotBlank()) payload["email"] = email
                    if (classGroup.isNotBlank() && selectedRole == Role.STUDENT) {
                        payload["classGroup"] = classGroup
                    }
                    
                    viewModel.createUser(payload)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8E8E93) // iOS Gray Button
                ),
                enabled = !uiState.isCreating && name.isNotBlank() && identifiant.isNotBlank() && password.length >= 6
            ) {
                 if (uiState.isCreating) {
                     CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                 } else {
                     Text("Créer le compte", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                 }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun RoleCard(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (selected) Color(0xFFD32F2F) else Color.White
    val contentColor = if (selected) Color.White else Color.Black
    
    Card(
        onClick = onClick,
        modifier = modifier
            .aspectRatio(1f) // Square
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 4.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
            )
        }
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = true,
        textStyle = TextStyle(
            fontSize = 16.sp,
            color = Color.Black
        ),
        decorationBox = { innerTextField ->
            Row(
                modifier = modifier
                    .height(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = TextStyle(
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        )
                    }
                    innerTextField()
                }
            }
        }
    )
}

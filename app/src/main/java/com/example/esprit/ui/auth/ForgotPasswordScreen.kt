package com.example.esprit.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.esprit.R
import com.example.esprit.ui.theme.RedPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBackComp: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // States for inputs
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var pass1 by remember { mutableStateOf("") }
    var pass2 by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mot de passe oublié") },
                navigationIcon = {
                    IconButton(onClick = onBackComp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                
                // Header Image/Icon depending on step
                val icon = when(uiState.step) {
                    ResetStep.EMAIL -> Icons.Default.Email
                    ResetStep.CODE -> Icons.Default.Pin
                    ResetStep.NEW_PASSWORD -> Icons.Default.Lock
                    ResetStep.SUCCESS -> Icons.Default.CheckCircle
                }
                
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = RedPrimary
                )

                if (uiState.message != null) {
                    Text(
                        text = uiState.message!!,
                        color = Color(0xFF2E7D32), // Green
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                when (uiState.step) {
                    ResetStep.EMAIL -> {
                        Text(
                            "Entrez votre adresse email pour recevoir un code de vérification.",
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                        
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Email, null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                        
                        Button(
                            onClick = { viewModel.sendCode(email) },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RedPrimary),
                            enabled = !uiState.isLoading
                        ) {
                            if (uiState.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            else Text("Envoyer le code")
                        }
                    }
                    
                    ResetStep.CODE -> {
                         Text(
                            "Entrez le code à 6 chiffres reçu par email.",
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                        
                        OutlinedTextField(
                            value = code,
                            onValueChange = { if (it.length <= 6) code = it },
                            label = { Text("Code de vérification") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Pin, null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        
                        Button(
                            onClick = { viewModel.verifyCode(code) },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RedPrimary),
                            enabled = !uiState.isLoading
                        ) {
                            if (uiState.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            else Text("Vérifier")
                        }
                    }
                    
                    ResetStep.NEW_PASSWORD -> {
                        Text(
                            "Créez votre nouveau mot de passe.",
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                        
                        OutlinedTextField(
                            value = pass1,
                            onValueChange = { pass1 = it },
                            label = { Text("Nouveau mot de passe") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            leadingIcon = { Icon(Icons.Default.Lock, null) }
                        )

                        OutlinedTextField(
                            value = pass2,
                            onValueChange = { pass2 = it },
                            label = { Text("Confirmer le mot de passe") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            leadingIcon = { Icon(Icons.Default.Lock, null) }
                        )
                        
                        Button(
                            onClick = { viewModel.resetPassword(pass1, pass2) },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RedPrimary),
                            enabled = !uiState.isLoading
                        ) {
                            if (uiState.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            else Text("Réinitialiser")
                        }
                    }
                    
                    ResetStep.SUCCESS -> {
                        Text(
                            "Vous pouvez maintenant vous connecter avec votre nouveau mot de passe.",
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                        
                        Button(
                            onClick = onBackComp,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RedPrimary)
                        ) {
                            Text("Retour à la connexion")
                        }
                    }
                }
            }
        }
    }
}

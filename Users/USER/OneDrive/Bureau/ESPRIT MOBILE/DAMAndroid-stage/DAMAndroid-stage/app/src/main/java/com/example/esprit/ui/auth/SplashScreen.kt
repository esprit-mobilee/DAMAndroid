package com.example.esprit.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.esprit.R
import com.example.esprit.model.Role

@Composable
fun SplashScreen(
    viewModel: SplashViewModel,
    onUnauthenticated: () -> Unit,
    onAuthenticated: (Role) -> Unit
) {
    // run the auth check as soon as we arrive
    LaunchedEffect(Unit) {
        viewModel.checkAuth { ok, role ->
            if (ok && role != null) {
                onAuthenticated(role)
            } else {
                onUnauthenticated()
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.esprit_logo),
                contentDescription = "ESPRIT",
            )
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
}

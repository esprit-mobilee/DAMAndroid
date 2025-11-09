package com.example.esprit.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
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

            // 1) logo
            Image(
                painter = painterResource(id = R.drawable.esprit_logo),
                contentDescription = "ESPRIT",
            )

            Spacer(modifier = Modifier.height(16.dp))


        }
    }
}

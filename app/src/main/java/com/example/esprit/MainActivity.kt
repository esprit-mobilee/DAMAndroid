package com.example.esprit

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.esprit.ui.theme.EspritTheme
import com.example.esprit.ui.nav.AppNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setContent {
                    EspritTheme {
                        val navController = rememberNavController()
                        AppNavGraph(navController = navController)
                    }
                }
            } else {
                // Fallback pour versions Android < 26
                setContent {
                    EspritTheme {
                        Text("Cette application nécessite Android 8.0 (API 26) ou supérieur")
                    }
                }
            }
        } catch (e: Exception) {
            // En cas d'erreur, afficher un message au lieu de crasher
            setContent {
                EspritTheme {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Erreur lors du démarrage de l'application",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = e.message ?: "Erreur inconnue",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

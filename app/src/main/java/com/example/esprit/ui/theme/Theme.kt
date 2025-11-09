package com.esprit.connect.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.esprit.ui.theme.BgGray
import com.example.esprit.ui.theme.EspritTypography
import com.example.esprit.ui.theme.HeaderBlack
import com.example.esprit.ui.theme.RedPrimary

private val LightColors = lightColorScheme(
    primary = RedPrimary,
    onPrimary = Color.White,
    secondary = HeaderBlack,
    background = BgGray,
    surface = Color.White,
    onSurface = Color.Black
)

@Composable
fun EspritTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = EspritTypography,
        content = content
    )
}

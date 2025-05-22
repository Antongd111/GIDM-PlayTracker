package com.example.playtracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AzulElectrico,
    secondary = VioletaAcento,
    background = FondoOscuro,
    surface = Input,
    onPrimary = TextoClaro,
    onSecondary = TextoClaro,
    onBackground = TextoClaro,
    onSurface = TextoClaro,
    error = Error,
    onError = TextoClaro
)

private val LightColorScheme = lightColorScheme(
    primary = AzulElectrico,
    secondary = VioletaAcento,
    background = FondoOscuro,
    surface = Input,
    onPrimary = TextoClaro,
    onSecondary = TextoClaro,
    onBackground = TextoClaro,
    onSurface = TextoClaro,
    error = Error,
    onError = TextoClaro
)

@Composable
fun PlayTrackerTheme(
    darkTheme: Boolean = true, // o isSystemInDarkTheme()
    dynamicColor: Boolean = false, // ¡desactivado!
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
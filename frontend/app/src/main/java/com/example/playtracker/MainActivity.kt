package com.example.playtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.example.playtracker.ui.theme.PlayTrackerTheme
import com.example.playtracker.navigation.AppNavigation
import com.example.playtracker.data.storage.TokenManager
import kotlinx.coroutines.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.navigationBarColor = 0xFF1E1E2F.toInt()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            PlayTrackerTheme {
                val navController = rememberNavController()
                val context = applicationContext
                var startDestination by remember { mutableStateOf<String?>(null) }

                // Cargar el token desde DataStore en un hilo de fondo
                LaunchedEffect(Unit) {
                    val token = withContext(Dispatchers.IO) {
                        TokenManager.getToken(context)
                    }
                    startDestination = if (!token.isNullOrBlank()) "main" else "login"
                }

                // Mostrar navegación solo si se ha decidido la ruta inicial
                if (startDestination != null) {
                    AppNavigation(navController, startDestination!!)
                }
            }
        }
    }
}
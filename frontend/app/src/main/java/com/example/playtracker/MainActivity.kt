package com.example.playtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.example.playtracker.data.UserPreferences
import com.example.playtracker.navigation.AppNavigation
import com.example.playtracker.ui.theme.PlayTrackerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.navigationBarColor = 0xFF1E1E2F.toInt()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            PlayTrackerTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                val prefs = remember { UserPreferences(context) }

                var startDestination by remember { mutableStateOf<String?>(null) }

                // Lee el token del DataStore y decide la pantalla inicial
                LaunchedEffect(Unit) {
                    val token = withContext(Dispatchers.IO) {
                        prefs.tokenFlow.firstOrNull()
                    }
                    startDestination = if (!token.isNullOrBlank()) "main" else "login"
                }

                if (startDestination != null) {
                    AppNavigation(navController, startDestination!!)
                }
            }
        }
    }
}

package com.example.playtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.playtracker.ui.LoginScreen
import com.example.playtracker.ui.theme.PlayTrackerTheme
import androidx.navigation.compose.rememberNavController
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.navigationBarColor = 0xFF1E1E2F.toInt()

        // Hace que el contenido dibuje detrás de la status bar
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            PlayTrackerTheme {
                val navController = rememberNavController()
                LoginScreen(navController = navController)
            }
        }
    }
}
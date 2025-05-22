package com.example.playtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.example.playtracker.navigation.AppNavigation
import com.example.playtracker.ui.theme.PlayTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.navigationBarColor = 0xFF1E1E2F.toInt()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            PlayTrackerTheme {
                val navController = rememberNavController()
                AppNavigation(navController)
            }
        }
    }
}
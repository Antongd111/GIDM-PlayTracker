package com.example.playtracker.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.playtracker.ui.screen.GameDetailScreen
import com.example.playtracker.ui.screen.MainScreen
import com.example.playtracker.ui.screen.LoginScreen
import com.example.playtracker.ui.screen.RegisterScreen

@Composable
fun AppNavigation(navController: NavHostController, startDestination: String) {
    // Grafo de navegación principal (root)
    NavHost(navController = navController, startDestination = startDestination) {

        // Pantalla de login (sin bottom bar)
        composable("login") {
            LoginScreen(navController)
        }

        // Pantalla de registro (sin bottom bar)
        composable("register") {
            RegisterScreen(navController)
        }

        // Shell principal con bottom bar y NavHost interno (tabs)
        composable("main") {
            MainScreen(navController)
        }

        // Detalle de juego (fuera de la shell con bottom bar)
        composable(route = "gameDetail/{gameId}") { backStackEntry ->
            val gameId = backStackEntry.arguments
                ?.getString("gameId")
                ?.toLongOrNull()
                ?: return@composable

            GameDetailScreen(
                gameId = gameId,
            )
        }
    }
}
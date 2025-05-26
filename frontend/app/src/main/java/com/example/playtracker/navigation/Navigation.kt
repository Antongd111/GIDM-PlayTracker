package com.example.playtracker.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.playtracker.ui.screen.GameDetailScreen
import com.example.playtracker.ui.screen.HomeScreen
import com.example.playtracker.ui.screen.LoginScreen
import com.example.playtracker.ui.viewmodel.GameDetailViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playtracker.ui.screen.MainScreen
import com.example.playtracker.ui.screen.SocialScreen

@Composable
fun AppNavigation(navController: NavHostController, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(navController)
        }
        composable("home") {
            HomeScreen(navController)
        }
        composable("social") {
            SocialScreen(navController)
        }
        composable("main") {
            MainScreen(navController)
        }
        composable("gameDetail/{gameId}") { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId")?.toIntOrNull() ?: return@composable

            // Crea el ViewModel manualmente si no usas Hilt
            val viewModel: GameDetailViewModel = viewModel(factory = object : ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    return GameDetailViewModel() as T
                }
            })

            GameDetailScreen(
                viewModel = viewModel,
                gameId = gameId
            )
        }
    }
}
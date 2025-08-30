package com.example.playtracker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.playtracker.data.remote.service.RetrofitInstance
import com.example.playtracker.data.repository.FriendsRepository
import com.example.playtracker.data.repository.ReviewsRepository
import com.example.playtracker.data.repository.UserGameRepository
import com.example.playtracker.data.repository.UserRepository
import com.example.playtracker.data.repository.impl.FriendsRepositoryImpl
import com.example.playtracker.data.repository.impl.ReviewsRepositoryImpl
import com.example.playtracker.data.repository.impl.UserGameRepositoryImpl
import com.example.playtracker.data.repository.impl.UserRepositoryImpl
import com.example.playtracker.ui.screen.GameDetailScreen
import com.example.playtracker.ui.screen.GamesScreen
import com.example.playtracker.ui.screen.MainScreen
import com.example.playtracker.ui.screen.SocialScreen
import com.example.playtracker.ui.screen.LoginScreen
import com.example.playtracker.ui.screen.UserScreen
import com.example.playtracker.ui.viewmodel.GameDetailViewModel

@Composable
fun AppNavigation(navController: NavHostController, startDestination: String) {
    // Grafo de navegación principal (root)
    NavHost(navController = navController, startDestination = startDestination) {

        // Pantalla de login (sin bottom bar)
        composable("login") {
            LoginScreen(navController)
        }

        // Shell principal con bottom bar y NavHost interno (tabs)
        composable("main") {
            MainScreen(navController) // pasa el parent para poder ir a detalles
        }

        // Detalle de juego (fuera de la shell con bottom bar)
        composable(route = "gameDetail/{gameId}") { backStackEntry ->
            val gameId = backStackEntry.arguments
                ?.getString("gameId")
                ?.toLongOrNull()
                ?: return@composable

            // ViewModel sin factory (crea sus dependencias internamente)
            val viewModel: GameDetailViewModel = viewModel(
                key = "GameDetailVM_$gameId" // clave estable por juego
            )

            GameDetailScreen(
                gameId = gameId,
            )
        }
    }
}
package com.example.playtracker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.playtracker.data.remote.service.RetrofitInstance
import com.example.playtracker.data.repository.FriendsRepository
import com.example.playtracker.data.repository.UserGameRepository
import com.example.playtracker.data.repository.UserRepository
import com.example.playtracker.data.repository.impl.FriendsRepositoryImpl
import com.example.playtracker.data.repository.impl.UserGameRepositoryImpl
import com.example.playtracker.data.repository.impl.UserRepositoryImpl
import com.example.playtracker.ui.screen.GameDetailScreen
import com.example.playtracker.ui.screen.GamesScreen
import com.example.playtracker.ui.screen.MainScreen
import com.example.playtracker.ui.screen.SocialScreen
import com.example.playtracker.ui.screen.LoginScreen
import com.example.playtracker.ui.viewmodel.GameDetailViewModel
import com.example.playtracker.ui.viewmodel.SocialViewModelFactory

@Composable
fun AppNavigation(navController: NavHostController, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(navController)
        }

        composable("home") {
            GamesScreen(navController)
        }

        // --- SOCIAL ---
        composable("social") {
            SocialScreen(navController = navController)
        }

        composable("main") {
            MainScreen(navController)
        }

        // --- GAME DETAIL ---
        composable(route = "gameDetail/{gameId}") { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId")?.toLongOrNull()
                ?: return@composable

            val viewModel: GameDetailViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        val gameApi = RetrofitInstance.gameApi
                        val userGameApi = RetrofitInstance.userGameApi
                        val userGameRepo: UserGameRepository = UserGameRepositoryImpl(
                            gameApi = gameApi,
                            userGameApi = userGameApi
                        )
                        return GameDetailViewModel(gameApi, userGameRepo) as T
                    }
                },
                key = "GameDetailVM_$gameId"
            )

            GameDetailScreen(
                viewModel = viewModel,
                gameId = gameId
            )
        }
    }
}

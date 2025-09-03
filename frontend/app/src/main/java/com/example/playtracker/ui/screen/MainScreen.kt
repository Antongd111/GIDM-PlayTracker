package com.example.playtracker.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.playtracker.data.local.datastore.UserPreferences

@Composable
fun MainScreen(parentNavController: NavHostController) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current

    val prefs = remember { UserPreferences(context) }
    val storedUserId by prefs.userIdFlow.collectAsState(initial = null)

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .navigationBarsPadding()
            ) {
                // Pantalla de videojuegos
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            if (currentRoute == "home")
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surface
                        )
                        .clickable {
                            if (currentRoute != "home") {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.VideogameAsset,
                        contentDescription = "Juegos",
                        modifier = Modifier.size(40.dp),
                        tint = if (currentRoute == "home")
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }

                // Pantalla de Social
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            if (currentRoute == "social")
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surface
                        )
                        .clickable {
                            if (currentRoute != "social") {
                                navController.navigate("social") {
                                    popUpTo("home") { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Group,
                        contentDescription = "Social",
                        modifier = Modifier.size(40.dp),
                        tint = if (currentRoute == "social")
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }

                // Mi perfil (solo resalta en "me", NO en "user/{userId}")
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            if (currentRoute == "me")
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surface
                        )
                        .clickable(enabled = storedUserId != null) {
                            // Navega a "me" (no a user/{id})
                            if (currentRoute != "me") {
                                navController.navigate("me") {
                                    popUpTo("home") { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Usuario",
                        modifier = Modifier.size(40.dp),
                        tint = if (currentRoute == "me")
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .navigationBarsPadding()
        ) {
            NavHost(
                navController = navController,
                startDestination = "home"
            ) {
                // Home puede seguir usando el root si lo necesitas para otras pantallas
                composable("home") { GamesScreen(parentNavController) }

                // Social debe usar el nav interno, así al ir a user/{id} mantiene Scaffold e insets
                composable("social") { SocialScreen(navController /* interno */) }

                // Mi perfil (sin arg): resalta la pestaña Usuario
                composable("me") {
                    // Usa tu storedUserId para mostrar tu propio perfil
                    storedUserId?.let { uid ->
                        UserScreen(parentNavController, navController, uid)
                    }
                }

                // Perfil de otros: NO resalta la pestaña Usuario
                composable(
                    route = "user/{userId}",
                    arguments = listOf(navArgument("userId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val userId = backStackEntry.arguments?.getInt("userId") ?: return@composable
                    UserScreen(parentNavController, navController, userId)
                }
            }
        }
    }
}

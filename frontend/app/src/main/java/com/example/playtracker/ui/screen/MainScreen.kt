package com.example.playtracker.ui.screen

import android.util.Log
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.example.playtracker.data.api.RetrofitInstance
import com.example.playtracker.data.storage.TokenManager

@Composable
fun MainScreen(parentNavController: NavHostController) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current

    var userId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        try {
            val token = TokenManager.getToken(context)
            Log.d("MainScreen", "Token actual: $token")
            val user = RetrofitInstance.userApi.getCurrentUser("Bearer $token")
            userId = user.id
        } catch (e: Exception) {
            Log.e("MainScreen", "Error al obtener el usuario: ${e.message}")
        }
    }

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .navigationBarsPadding()
            ) {
                // Juegos
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(if (currentRoute == "home") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                        .clickable {
                            if (currentRoute != "home") {
                                val options = navOptions {
                                    popUpTo("home") { inclusive = false }
                                    launchSingleTop = true
                                }
                                navController.navigate("home", options)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.VideogameAsset,
                        contentDescription = "Juegos",
                        modifier = Modifier.size(40.dp),
                        tint = if (currentRoute == "home") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }

                // Social
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(if (currentRoute == "social") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                        .clickable {
                            if (currentRoute != "social") {
                                val options = navOptions {
                                    popUpTo("home") { inclusive = false }
                                    launchSingleTop = true
                                }
                                navController.navigate("social", options)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Group,
                        contentDescription = "Social",
                        modifier = Modifier.size(40.dp),
                        tint = if (currentRoute == "social") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }

                // Usuario actual (navega a user/{userId})
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(if (currentRoute?.startsWith("user/") == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                        .clickable {
                            Log.d("MainScreen", "Botón de usuario pulsado, userId=$userId")
                            userId?.let {
                                navController.navigate("user/$it")
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Usuario",
                        modifier = Modifier.size(40.dp),
                        tint = if (currentRoute?.startsWith("user/") == true) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
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
                composable("home") { HomeScreen(parentNavController) }
                composable("social") { SocialScreen(parentNavController) }

                // Nueva ruta con parámetro userId
                composable("user/{userId}") { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull()
                    if (userId != null) {
                        UserScreen(parentNavController, userId)
                    }
                }
            }
        }
    }
}

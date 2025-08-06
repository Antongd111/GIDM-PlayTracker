package com.example.playtracker.ui.screen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.playtracker.R
import com.example.playtracker.data.api.RetrofitInstance
import com.example.playtracker.data.model.User

@Composable
fun UserScreen(navController: NavController, userId: Int) {
    val context = LocalContext.current
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        try {
            user = RetrofitInstance.userApi.getUserById(userId)
        } catch (e: Exception) {
            Log.e("UserScreen", "Error cargando usuario: ${e.message}")
            errorMessage = "No se pudo cargar el usuario"
        } finally {
            isLoading = false
        }
    }

    when {
        isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        errorMessage != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(errorMessage ?: "Error")
            }
        }

        user != null -> {
            val favoriteGame = GameSample("The Witcher 3: Wild Hunt", "https://image.api.playtracker/fav.jpg")
            val completedGames = List(5) {
                GameSample("The Witcher 3: Wild Hunt", "https://image.api.playtracker/fav.jpg")
            }
            val topGenres = listOf("Estrategia", "Acción", "Simulación")

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(60.dp)
//                            .padding(horizontal = 10.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        IconButton(onClick = { navController.popBackStack() }) {
//                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
//                        }
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text("Perfil de ${user!!.username}", style = MaterialTheme.typography.headlineSmall)
//                    }

                    // Avatar + nombre
                    user?.let { user ->
                        val avatarPainter = rememberAsyncImagePainter(
                            model = if (!user.avatarUrl.isNullOrBlank()) user.avatarUrl else R.drawable.default_avatar
                        )
                        Log.d("UserScreen", "Avatar URL: ${user?.avatarUrl}")
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(top = 16.dp, start = 10.dp, end = 10.dp)
                        ) {
                            Image(
                                painter = avatarPainter,
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                            )

                            Spacer(modifier = Modifier.width(20.dp))
                            Column {
                                Text(user.username, style = MaterialTheme.typography.titleLarge)
                                Spacer(modifier = Modifier.height(5.dp))
                                Text(user.status, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(bottom = 10.dp, end = 10.dp, start = 10.dp)
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        Button(onClick = { }) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "Añadir amigo",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 15.dp, bottom = 16.dp, start = 10.dp, end = 10.dp)
                    ) {
                        Text("Juego favorito", style = MaterialTheme.typography.titleMedium)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp)) {
                                Image(
                                    painter = rememberAsyncImagePainter(favoriteGame.imageUrl),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(favoriteGame.title, style = MaterialTheme.typography.bodyLarge)
                                    Row {
                                        repeat(5) {
                                            Icon(
                                                Icons.Default.Star,
                                                contentDescription = "Estrella",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Text("Ver opinión", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.primary)

                        Text("Juegos completados (${completedGames.size})", style = MaterialTheme.typography.titleMedium)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                            items(completedGames) { game ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Image(
                                        painter = rememberAsyncImagePainter(game.imageUrl),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .height(120.dp)
                                            .width(100.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Text(
                                        game.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.width(100.dp),
                                        maxLines = 1,
                                        textAlign = TextAlign.Center
                                    )
                                    Row {
                                        repeat(5) {
                                            Icon(
                                                Icons.Default.Star,
                                                contentDescription = "Estrella",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.primary)

                        Text("Géneros más jugados", style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                            topGenres.forEach { genre ->
                                AssistChip(
                                    onClick = {},
                                    label = { Text(genre) },
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }
    }
}

data class GameSample(val title: String, val imageUrl: String)

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
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.playtracker.R
import com.example.playtracker.data.api.RetrofitInstance
import com.example.playtracker.data.model.User
import com.example.playtracker.data.model.UserGame
import com.example.playtracker.data.model.GameDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

@Composable
fun UserScreen(
    navController: NavController,
    userId: Int                 // ← volvemos a exigirlo por parámetro
) {
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Estado para juegos completados reales
    var completedGames by remember { mutableStateOf<List<GameDetail>>(emptyList()) }
    var isLoadingCompleted by remember { mutableStateOf(true) }
    var completedError by remember { mutableStateOf<String?>(null) }

    // Cargar el usuario + user_games cuando cambie el parámetro userId
    LaunchedEffect(userId) {
        isLoading = true
        isLoadingCompleted = true
        errorMessage = null
        completedError = null

        try {
            // 1) Cargar el usuario
            user = withContext(Dispatchers.IO) {
                RetrofitInstance.userApi.getUserById(userId)
            }
            Log.d("UserScreen", "Cargado userId=$userId -> ${user?.username}")

            // 2) Cargar user_games y filtrar por COMPLETED (ajusta el literal si en tu BD usas otro)
            val userGames: List<UserGame> = withContext(Dispatchers.IO) {
                RetrofitInstance.userGameApi.getUserGames(userId)
            }
            val completed = userGames.filter { it.status?.equals("Completado", ignoreCase = true) == true }

            // 3) Obtener detalles RAWG en paralelo (limitamos a 20 por rendimiento)
            val rawgIds = completed.map { it.game_rawg_id }.distinct().take(20)
            val details = withContext(Dispatchers.IO) {
                rawgIds.map { id ->
                    async {
                        try {
                            RetrofitInstance.gameApi.getGameDetails(id)
                        } catch (e: Exception) {
                            Log.e("UserScreen", "Error RAWG id=$id -> ${e.message}")
                            null
                        }
                    }
                }.awaitAll().filterNotNull()
            }
            completedGames = details
        } catch (e: Exception) {
            Log.e("UserScreen", "Error cargando datos: ${e.message}", e)
            if (user == null) errorMessage = "No se pudo cargar el usuario"
            completedError = "No se pudieron cargar los juegos completados"
        } finally {
            isLoading = false
            isLoadingCompleted = false
        }
    }

    when {
        isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(errorMessage ?: "Error")
        }

        user != null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header avatar + nombre
                    user?.let { u ->
                        val avatarPainter = rememberAsyncImagePainter(
                            model = if (!u.avatarUrl.isNullOrBlank()) u.avatarUrl else R.drawable.default_avatar
                        )
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
                            Spacer(Modifier.width(20.dp))
                            Column {
                                Text(u.username, style = MaterialTheme.typography.titleLarge)
                                Spacer(Modifier.height(5.dp))
                                Text(u.status, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(bottom = 10.dp, end = 10.dp, start = 10.dp)
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        Button(onClick = { /* follow/add friend */ }) {
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
                        // ---- Juego favorito (placeholder, lo dejamos para el final) ----
                        Text("Juego favorito", style = MaterialTheme.typography.titleMedium)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp)) {
                                Image(
                                    painter = rememberAsyncImagePainter(R.drawable.default_avatar),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("— pendiente —", style = MaterialTheme.typography.bodyLarge)
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
                                    Text(
                                        "Ver opinión",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // ---- Juegos completados (datos reales) ----
                        val completedCount = completedGames.size
                        Text("Juegos completados ($completedCount)", style = MaterialTheme.typography.titleMedium)

                        when {
                            isLoadingCompleted -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                            completedError != null -> {
                                Text(
                                    completedError ?: "Error cargando completados",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            completedGames.isEmpty() -> {
                                Text(
                                    "Aún no hay juegos completados.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            else -> {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    items(completedGames, key = { it.id }) { game ->
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            val imageUrl = game.imageUrl
                                            Image(
                                                painter = rememberAsyncImagePainter(
                                                    model = if (!imageUrl.isNullOrBlank()) imageUrl else R.drawable.default_avatar
                                                ),
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
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // ---- Géneros más jugados (de momento estático) ----
                        Text("Géneros más jugados", style = MaterialTheme.typography.titleMedium)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            listOf("Estrategia", "Acción", "Simulación").forEach { genre ->
                                AssistChip(onClick = {}, label = { Text(genre) }, shape = RoundedCornerShape(8.dp))
                            }
                        }

                        Spacer(Modifier.height(30.dp))
                    }
                }
            }
        }
    }
}

package com.example.playtracker.ui.screen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.playtracker.R
import com.example.playtracker.data.UserPreferences
import com.example.playtracker.data.api.RetrofitInstance
import com.example.playtracker.data.model.GameDetail
import com.example.playtracker.data.model.User
import com.example.playtracker.data.model.UserGame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalContext

@Composable
fun UserScreen(
    navController: NavController,
    userId: Int
) {
    // Token (para pedir /friends del usuario autenticado)
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    val token by prefs.tokenFlow.collectAsState(initial = null)
    val bearer = token?.let { "Bearer $it" }

    // Estado para usuario
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Estado para juegos completados
    var completedGames by remember { mutableStateOf<List<GameDetail>>(emptyList()) }
    var isLoadingCompleted by remember { mutableStateOf(true) }
    var completedError by remember { mutableStateOf<String?>(null) }

    // Estado para favorito
    var favoriteGame by remember { mutableStateOf<GameDetail?>(null) }
    var isLoadingFavorite by remember { mutableStateOf(true) }
    var favoriteError by remember { mutableStateOf<String?>(null) }

    // Estado para amigos (del usuario autenticado)
    var friends by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoadingFriends by remember { mutableStateOf(true) }
    var friendsError by remember { mutableStateOf<String?>(null) }

    // Cargar datos cuando cambie userId o el token
    LaunchedEffect(userId, bearer) {
        isLoading = true
        isLoadingCompleted = true
        isLoadingFavorite = true
        isLoadingFriends = true
        errorMessage = null
        completedError = null
        favoriteError = null
        friendsError = null

        try {
            // 1) Usuario
            user = withContext(Dispatchers.IO) {
                RetrofitInstance.userApi.getUserById(userId)
            }

            // 2) UserGames -> Completados
            val userGames: List<UserGame> = withContext(Dispatchers.IO) {
                RetrofitInstance.userGameApi.getUserGames(userId)
            }
            val completed = userGames.filter { it.status?.equals("Completado", ignoreCase = true) == true }

            // 3) Detalles RAWG en paralelo (máx. 20)
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

            // 4) Favorito (si hay)
            favoriteGame = null
            val favId = user?.favorite_rawg_game_id
            if (favId != null) {
                favoriteGame = withContext(Dispatchers.IO) {
                    RetrofitInstance.gameApi.getGameDetails(favId)
                }
            }

            // 5) Amigos del usuario autenticado (GET /friends)
            friends = withContext(Dispatchers.IO) {
                val b = bearer ?: return@withContext emptyList<User>()
                val res = RetrofitInstance.friendsApi.listFriends(b)
                if (!res.isSuccessful) emptyList() else
                    res.body().orEmpty().map { f ->
                        // FriendLite -> User (rellenamos campos que tu User exige)
                        User(
                            id = f.id,
                            username = f.username ?: "Usuario ${f.id}",
                            email = "",                          // <- requerido por tu User
                            avatarUrl = f.avatar_url ?: "",
                            status = "",                         // <- requerido por tu User (no null)
                            favorite_rawg_game_id = null         // <- si es Int? pásalo a null
                        )
                    }
            }

        } catch (e: Exception) {
            Log.e("UserScreen", "Error cargando datos: ${e.message}", e)
            if (user == null) errorMessage = "No se pudo cargar el usuario"
            completedError = "No se pudieron cargar los juegos completados"
            favoriteError = "No se pudo cargar el juego favorito"
            friendsError = "No se pudieron cargar los amigos"
        } finally {
            isLoading = false
            isLoadingCompleted = false
            isLoadingFavorite = false
            isLoadingFriends = false
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
                        // ---- Juego favorito ----
                        Text("Juego favorito", style = MaterialTheme.typography.titleMedium)

                        when {
                            isLoadingFavorite -> {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) { Box(Modifier.height(110.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                            }
                            favoriteError != null -> {
                                Text(
                                    favoriteError ?: "Error cargando favorito",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            favoriteGame == null -> {
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
                                            Text("— sin favorito —", style = MaterialTheme.typography.bodyLarge)
                                            Text("Elige un juego y márcalo con la ⭐", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                            else -> {
                                val game = favoriteGame!!
                                Card(
                                    onClick = { navController.navigate("gameDetail/${game.id}") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(modifier = Modifier.padding(12.dp)) {
                                        val imageUrl = game.imageUrl
                                        Image(
                                            painter = rememberAsyncImagePainter(
                                                model = if (!imageUrl.isNullOrBlank()) imageUrl else R.drawable.default_avatar
                                            ),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(100.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(game.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                                            Spacer(Modifier.height(4.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                repeat(5) {
                                                    Icon(Icons.Default.Star, contentDescription = "Estrella",
                                                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                            Spacer(Modifier.height(6.dp))
                                            Text("Ver detalles", style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary)
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

                        // ---- Juegos completados ----
                        val completedCount = completedGames.size
                        Text("Juegos completados ($completedCount)", style = MaterialTheme.typography.titleMedium)

                        when {
                            isLoadingCompleted -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp),
                                    contentAlignment = Alignment.Center
                                ) { CircularProgressIndicator() }
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
                                Text("Aún no hay juegos completados.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 8.dp))
                            }
                            else -> {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    items(completedGames, key = { it.id }) { game ->
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .clickable { navController.navigate("gameDetail/${game.id}") }
                                                .padding(4.dp)
                                        ) {
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
                                                    Icon(Icons.Default.Star, contentDescription = "Estrella",
                                                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
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

                        // ---- Amigos (del usuario autenticado) ----
                        Text("Amigos", style = MaterialTheme.typography.titleMedium)

                        when {
                            isLoadingFriends -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp),
                                    contentAlignment = Alignment.Center
                                ) { CircularProgressIndicator() }
                            }
                            friendsError != null -> {
                                Text(
                                    friendsError ?: "Error cargando amigos",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            friends.isEmpty() -> {
                                Text(
                                    "Aún no tiene amigos.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            else -> {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.padding(top = 12.dp)
                                ) {
                                    items(friends, key = { it.id }) { friend ->
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .width(88.dp)
                                                .clickable {
                                                    navController.navigate("user/${friend.id}")
                                                }
                                        ) {
                                            val avatar = rememberAsyncImagePainter(
                                                model = if (!friend.avatarUrl.isNullOrBlank())
                                                    friend.avatarUrl else R.drawable.default_avatar
                                            )
                                            Image(
                                                painter = avatar,
                                                contentDescription = "Avatar de ${friend.username}",
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .clip(CircleShape)
                                            )
                                            Spacer(Modifier.height(6.dp))
                                            Text(
                                                friend.username,
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 1,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(30.dp))
                    }
                }
            }
        }
    }
}

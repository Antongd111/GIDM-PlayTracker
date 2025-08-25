package com.example.playtracker.ui.screen

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.playtracker.R
import com.example.playtracker.data.local.datastore.UserPreferences
import com.example.playtracker.data.remote.service.RetrofitInstance
import com.example.playtracker.data.repository.impl.UserGameRepositoryImpl
import com.example.playtracker.data.repository.impl.UserRepositoryImpl
import com.example.playtracker.domain.model.Game
import com.example.playtracker.domain.model.Friend
import com.example.playtracker.ui.viewmodel.UserViewModel
import com.example.playtracker.ui.viewmodel.UserViewModelFactory

@Composable
fun UserScreen(
    navController: NavController,
    userId: Int,
    viewModel: UserViewModel = viewModel(factory = UserViewModelFactory(
        userRepo = UserRepositoryImpl(RetrofitInstance.userApi, RetrofitInstance.friendsApi),
        userGameRepo = UserGameRepositoryImpl(RetrofitInstance.userGameApi, RetrofitInstance.gameApi),
        gameApi = RetrofitInstance.gameApi
    )
    )
) {
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    val token by prefs.tokenFlow.collectAsState(initial = null)

    val ui by viewModel.ui.collectAsState()

    LaunchedEffect(userId, token) {
        viewModel.load(userId, token)
    }

    when {
        ui.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        ui.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(ui.error ?: "Error")
        }
        ui.user != null -> {
            val u = ui.user!!
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {

                    // --- Header avatar + nombre ---
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(top = 16.dp, start = 10.dp, end = 10.dp)
                    ) {
                        val avatarPainter = rememberAsyncImagePainter(
                            model = if (!u.avatarUrl.isNullOrBlank()) u.avatarUrl else R.drawable.default_avatar
                        )
                        Image(
                            painter = avatarPainter,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                        )
                        Spacer(Modifier.width(20.dp))
                        Column {
                            Text(u.name, style = MaterialTheme.typography.titleLarge)
                            Spacer(Modifier.height(5.dp))
                            u.status?.let {
                                Text(it, style = MaterialTheme.typography.bodyMedium)
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
                            ui.favorite == null -> {
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
                                val game = ui.favorite!!
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
                        val completedCount = ui.completed.size
                        Text("Juegos completados ($completedCount)", style = MaterialTheme.typography.titleMedium)

                        if (ui.completed.isEmpty()) {
                            Text("Aún no hay juegos completados.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 8.dp))
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                items(ui.completed, key = { it.id }) { game: Game ->
                                    Card(
                                        onClick = { navController.navigate("gameDetail/${game.id}") },
                                        modifier = Modifier
                                            .width(140.dp) // ancho fijo cómodo
                                            .wrapContentHeight(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(8.dp)
                                        ) {
                                            val imageUrl = game.imageUrl
                                            Image(
                                                painter = rememberAsyncImagePainter(
                                                    model = if (!imageUrl.isNullOrBlank()) imageUrl else R.drawable.default_avatar
                                                ),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .height(120.dp)
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                            Spacer(Modifier.height(8.dp))
                                            Text(
                                                game.title,
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.fillMaxWidth(),
                                                maxLines = 1,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Row(
                                                horizontalArrangement = Arrangement.Center,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
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

                        // ---- Amigos ----
                        Text("Amigos", style = MaterialTheme.typography.titleMedium)

                        if (ui.friends.isEmpty()) {
                            Text(
                                "Aún no tiene amigos.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(top = 12.dp)
                            ) {
                                items(ui.friends, key = { it.id }) { friend: Friend ->
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
                                            contentDescription = "Avatar de ${friend.name}",
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(CircleShape)
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            friend.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 1,
                                            textAlign = TextAlign.Center
                                        )
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

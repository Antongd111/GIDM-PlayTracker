package com.example.playtracker.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.playtracker.data.local.datastore.UserPreferences
import com.example.playtracker.data.remote.service.RetrofitInstance
import com.example.playtracker.data.repository.impl.UserGameRepositoryImpl
import com.example.playtracker.ui.viewmodel.GameDetailViewModel
import kotlinx.coroutines.flow.firstOrNull

@Composable
fun GameDetailScreen(
    gameId: Long,
    viewModel: GameDetailViewModel = viewModel(
        factory = GameDetailViewModel.Factory(
            gameApi = RetrofitInstance.gameApi,
            userGameRepo = UserGameRepositoryImpl(RetrofitInstance.userGameApi, RetrofitInstance.gameApi)
        )
    )
) {
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    var userId by remember { mutableStateOf<Int?>(null) }

    // Carga del detalle del juego (no depende de userId)
    LaunchedEffect(gameId) {
        viewModel.loadGameDetail(gameId)
    }

    // Lee userId del DataStore una vez
    LaunchedEffect(Unit) {
        userId = prefs.userIdFlow.firstOrNull()
        userId?.let { uid ->
            viewModel.getUserGame(uid, gameId)
        }
    }

    // Si más tarde cambia userId (p.ej. tras login), vuelve a cargar el userGame
    LaunchedEffect(userId) {
        userId?.let { uid ->
            viewModel.getUserGame(uid, gameId)
        }
    }

    val gameDetail = viewModel.gameDetail
    val userGame = viewModel.userGame
    val isLoading = viewModel.isLoading
    val error = viewModel.error

    when {
        isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        error != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: $error")
            }
        }

        gameDetail != null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(WindowInsets.systemBars.asPaddingValues())
            ) {
                // Imagen principal
                Image(
                    painter = rememberAsyncImagePainter(gameDetail.imageUrl),
                    contentDescription = gameDetail.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = gameDetail.title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = 16.dp)
                )
                Text(
                    text = gameDetail.releaseDate ?: "Sin fecha",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Botones de estado
                val statusOptions = listOf("No seguido", "Por jugar", "Jugando", "Completado")
                val currentStatus = userGame?.status ?: "No seguido"
                val canUpdate = userId != null

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    statusOptions.forEach { status ->
                        val isSelected = currentStatus == status

                        Button(
                            onClick = {
                                userId?.let { uid ->
                                    viewModel.updateGameStatus(uid, gameId, status)
                                }
                            },
                            enabled = canUpdate,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 2.dp),
                            contentPadding = PaddingValues(4.dp),
                            shape = RoundedCornerShape(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected)
                                    Color.White
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(status, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Descripción con "ver más"
                var expanded by remember { mutableStateOf(false) }

                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = gameDetail.description ?: "Sin descripción.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = if (expanded) Int.MAX_VALUE else 10,
                        overflow = TextOverflow.Clip
                    )

                    if (!expanded) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            MaterialTheme.colorScheme.background
                                        ),
                                        startY = 60f,
                                        endY = Float.POSITIVE_INFINITY
                                    )
                                )
                        )
                    }
                }

                if (!gameDetail.description.isNullOrEmpty() && gameDetail.description.length > 200) {
                    TextButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .alpha(0.6f)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            if (expanded) "Ver menos" else "Ver más",
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.primary,
                )

                // Galería de imágenes
                Text(
                    text = "Galería",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    items(gameDetail.screenshots) { imageUrl ->
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = null,
                            contentScale = ContentScale.FillHeight,
                            modifier = Modifier
                                .height(180.dp)
                                .widthIn(max = 400.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.primary,
                )

//                // Opiniones simuladas
//                Text(
//                    text = "Opiniones",
//                    style = MaterialTheme.typography.titleMedium,
//                    modifier = Modifier
//                        .align(Alignment.CenterHorizontally)
//                        .padding(horizontal = 16.dp, vertical = 10.dp),
//                )
//                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
//                    gameDetail.similarGames.forEach { similar ->
//                        Card(
//                            modifier = Modifier.fillMaxWidth(),
//                            shape = RoundedCornerShape(12.dp)
//                        ) {
//                            Row(
//                                modifier = Modifier.padding(12.dp),
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Image(
//                                    painter = rememberAsyncImagePainter(similar.imageUrl),
//                                    contentDescription = similar.title,
//                                    modifier = Modifier
//                                        .size(40.dp)
//                                        .clip(CircleShape)
//                                )
//                                Spacer(modifier = Modifier.width(8.dp))
//                                Column {
//                                    Text(similar.title, style = MaterialTheme.typography.bodyMedium)
//                                    Row {
//                                        repeat(5) {
//                                            Icon(
//                                                imageVector = Icons.Default.Star,
//                                                contentDescription = "Star",
//                                                tint = MaterialTheme.colorScheme.primary,
//                                                modifier = Modifier.size(16.dp)
//                                            )
//                                        }
//                                    }
//                                    Text(
//                                        "Juego similar recomendado",
//                                        style = MaterialTheme.typography.bodySmall,
//                                        modifier = Modifier.padding(top = 4.dp)
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
            }
        }
    }
}

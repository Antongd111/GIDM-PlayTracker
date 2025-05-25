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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.playtracker.ui.viewmodel.GameDetailViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration

@Composable
fun GameDetailScreen(viewModel: GameDetailViewModel, gameId: Int) {
    LaunchedEffect(Unit) {
        viewModel.loadGameDetail(gameId)
    }

    val gameDetail = viewModel.gameDetail
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
                // Cover + title
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
                        .padding(start = 16.dp, end = 16.dp)
                )
                Text(
                    text = gameDetail.releaseDate ?: "Sin fecha",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Status buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("No seguido", "Por jugar", "Jugando", "Completado").forEach { status ->
                        Button(
                            onClick = { /* actualizar estado */ },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(4.dp),
                            shape = RoundedCornerShape(0.dp),
                        ) {
                            Text(status, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SECCIÓN: DESCRIPCIÓN
                // Si el texto es demasiado grande, se queda en un máximo de 10 líneas, a no ser que
                // se pulse el botón "Ver más"
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

                // Galería
                Text(
                    text = "Galería",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
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

                // Reviews (simuladas o futuras)
                Text(
                    text = "Opiniones",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
                )
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    gameDetail.similarGames.forEach { similar ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(similar.imageUrl),
                                    contentDescription = similar.title,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(similar.title, style = MaterialTheme.typography.bodyMedium)
                                    Row {
                                        repeat(5) {  // Aquí solo ejemplo de estrellas fijas
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = "Star",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        "Juego similar recomendado",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
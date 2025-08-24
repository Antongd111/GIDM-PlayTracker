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
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import com.example.playtracker.data.repository.impl.ReviewsRepositoryImpl
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.playtracker.domain.model.Review

@Composable
fun GameDetailScreen(
    gameId: Long,
    viewModel: GameDetailViewModel = viewModel(
        factory = GameDetailViewModel.Factory(
            gameApi = RetrofitInstance.gameApi,
            userGameRepo = UserGameRepositoryImpl(RetrofitInstance.userGameApi, RetrofitInstance.gameApi),
            reviewsRepo = ReviewsRepositoryImpl(RetrofitInstance.reviewsApi)
        )
    )
) {
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    var userId by remember { mutableStateOf<Int?>(null) }

    // Token
    val token by prefs.tokenFlow.collectAsState(initial = null)
    val bearer: String? = token?.let { "Bearer $it" }

    // Repo de reviews (usa el mismo Retrofit con tu interceptor de auth)
    val reviewsRepo = remember { ReviewsRepositoryImpl(RetrofitInstance.reviewsApi) }
    val scope = rememberCoroutineScope()

    // Carga del detalle del juego (no depende de userId)
    LaunchedEffect(gameId, bearer) {
        viewModel.loadGameDetail(gameId)

        // DEBUG: comprueba si llega token
        android.util.Log.d("GameDetail", "bearer is null? ${bearer == null}")

        // LLAMA solo si hay token
        if (bearer != null) {
            viewModel.loadReviews(gameId, bearer)
        }
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

                // =======================
                // Opiniones (carrusel)
                // =======================
                ReviewsSection(
                    title = "Opiniones",
                    reviews = viewModel.reviews,
                    isLoading = viewModel.isLoadingReviews,
                    error = viewModel.reviewsError
                )

                // =======================
                // Opiniones (UI real)
                // =======================

                // Preview muy simple de tu reseña actual (si existe en userGame)
                if ((userGame?.score ?: 0) > 0 || !userGame?.notes.isNullOrBlank()) {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        Text("Tu reseña", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        val stars = (userGame?.score ?: 0) / 10 // 0..10
                        Row {
                            repeat(10) { i ->
                                val filled = (i + 1) <= stars
                                Icon(
                                    imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.Star,
                                    contentDescription = null,
                                    tint = if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        if (!userGame?.notes.isNullOrBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(userGame?.notes ?: "", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                }

                // Botón para escribir/editar reseña
                var showReviewSheet by remember { mutableStateOf(false) }
                Button(
                    onClick = {
                        if (userId == null) {
                            Toast.makeText(context, "Inicia sesión para reseñar", Toast.LENGTH_SHORT).show()
                        } else {
                            showReviewSheet = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) { Text(if ((userGame?.score ?: 0) > 0 || !userGame?.notes.isNullOrBlank()) "Editar reseña" else "Escribir reseña") }

                Spacer(Modifier.height(24.dp))

                if (showReviewSheet) {
                    // Prefill con lo que tengas guardado
                    var tempScore by rememberSaveable { mutableStateOf((userGame?.score ?: 0) / 10) } // 0..10
                    var tempNotes by rememberSaveable { mutableStateOf(userGame?.notes ?: "") }
                    var isSaving by remember { mutableStateOf(false) }
                    var errorMsg by remember { mutableStateOf<String?>(null) }

                    @OptIn(ExperimentalMaterial3Api::class)
                    ModalBottomSheet(
                        onDismissRequest = { if (!isSaving) showReviewSheet = false },
                        dragHandle = null
                    ) {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Tu reseña", style = MaterialTheme.typography.titleLarge)

                            Text("Puntuación (0–10)", style = MaterialTheme.typography.labelLarge)
                            StarRating(
                                value0to10 = tempScore,
                                onChange = { tempScore = it }
                            )

                            OutlinedTextField(
                                value = tempNotes,
                                onValueChange = { tempNotes = it },
                                label = { Text("Escribe tu opinión") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 120.dp),
                                maxLines = 6
                            )

                            errorMsg?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }

                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { if (!isSaving) showReviewSheet = false },
                                    enabled = !isSaving
                                ) { Text("Cancelar") }

                                Spacer(Modifier.width(8.dp))

                                Button(
                                    onClick = {
                                        isSaving = true
                                        errorMsg = null
                                        scope.launch {
                                            try {
                                                val b = bearer ?: error("Debes iniciar sesión")
                                                // Llamada real al backend (Result -> excepción si falla)
                                                reviewsRepo.upsert(
                                                    gameId = gameId,
                                                    score0to10 = tempScore,
                                                    notes = tempNotes,
                                                    bearer = b
                                                ).getOrThrow()

                                                Toast.makeText(context, "Reseña guardada", Toast.LENGTH_SHORT).show()
                                                showReviewSheet = false

                                                // refrescar userGame para ver el preview actualizado
                                                userId?.let { uid -> viewModel.getUserGame(uid, gameId) }
                                            } catch (e: Exception) {
                                                errorMsg = e.message ?: "Error desconocido"
                                            } finally {
                                                isSaving = false
                                            }
                                        }
                                    },
                                    enabled = !isSaving
                                ) {
                                    if (isSaving) {
                                        CircularProgressIndicator(
                                            Modifier.size(18.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(Modifier.width(8.dp))
                                    }
                                    Text("Guardar")
                                }
                            }

                            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                        }
                    }
                }

                // (Aquí podrías más adelante listar reseñas de otros usuarios + likes)
            }
        }
    }
}

/** Rating con 10 estrellas (0..10). */
@Composable
private fun StarRating(
    value0to10: Int,
    onChange: (Int) -> Unit
) {
    Row {
        for (i in 1..10) {
            val filled = i <= value0to10
            IconButton(onClick = { onChange(i) }) {
                Icon(
                    imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = null,
                    tint = if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReviewsSection(
    title: String,
    reviews: List<Review>,
    isLoading: Boolean,
    error: String?
) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .align(Alignment.CenterHorizontally)
        )

        when {
            isLoading -> {
                Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Text(
                    "Error: $error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            reviews.isEmpty() -> {
                Text(
                    "Aún no hay reseñas. ¡Sé el primero!",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            else -> {
                ReviewsCarousel(reviews)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ReviewsCarousel(reviews: List<Review>) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { reviews.size })

    LaunchedEffect(reviews) {
        if (reviews.isEmpty()) return@LaunchedEffect
        while (true) {
            kotlinx.coroutines.delay(3500)
            if (!pagerState.isScrollInProgress) {
                pagerState.animateScrollToPage((pagerState.currentPage + 1) % reviews.size)
            }
        }
    }

    Box(
        Modifier
            .fillMaxWidth()
            .height(180.dp) // fija una altura razonable para la tarjeta
    ) {
        HorizontalPager(
            state = pagerState,
            pageSpacing = 12.dp,
            modifier = Modifier.matchParentSize()
        ) { page ->
            ReviewCard(
                review = reviews[page],
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .wrapContentHeight()  // deja que la tarjeta crezca dentro de los 180dp
            )
        }
    }

    // Indicadores (sin intrinsics)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(reviews.size) { i ->
            val selected = i == pagerState.currentPage
            Box(
                Modifier
                    .padding(horizontal = 3.dp)
                    .size(if (selected) 9.dp else 7.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    )
            )
        }
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun ReviewCard(
    review: Review,
    modifier: Modifier = Modifier
) {
    // colores suaves tipo tarjeta
    val surface = MaterialTheme.colorScheme.surface
    val overlay = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
    ) {
        Box(
            Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(surface, overlay, surface)
                    )
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                // Cabecera: avatar + nombre + estrellas
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // avatar (si no tienes url, usa un círculo plano)
                        Box(
                            Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            review.username ?: "Usuario",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    // estrellas 0..10 -> 5 estrellas (medias si quieres; aquí llenas 0..5)
                    val stars5 = ((review.score0to10 ?: 0) / 2f).coerceIn(0f, 5f)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) { i ->
                            val filled = i + 1 <= stars5
                            Icon(
                                imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = null,
                                tint = if (filled) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Texto reseña
                if (!review.notes.isNullOrBlank()) {
                    Text(
                        review.notes!!,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 18.sp
                    )
                } else {
                    Text(
                        "Sin comentario.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

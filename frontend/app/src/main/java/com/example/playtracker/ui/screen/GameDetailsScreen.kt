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
import androidx.compose.material.icons.filled.StarHalf
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
import androidx.compose.ui.unit.Dp
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
import kotlin.math.roundToInt

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

    // Repo de reviews
    val reviewsRepo = remember { ReviewsRepositoryImpl(RetrofitInstance.reviewsApi) }
    val scope = rememberCoroutineScope()

    // Carga del detalle del juego + reseñas
    LaunchedEffect(gameId, bearer) {
        viewModel.loadGameDetail(gameId)
        if (bearer != null) viewModel.loadReviews(gameId, bearer)
    }

    // userId -> userGame
    LaunchedEffect(Unit) {
        userId = prefs.userIdFlow.firstOrNull()
        userId?.let { uid -> viewModel.getUserGame(uid, gameId) }
    }
    LaunchedEffect(userId) {
        userId?.let { uid -> viewModel.getUserGame(uid, gameId) }
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
                            onClick = { userId?.let { uid -> viewModel.updateGameStatus(uid, gameId, status) } },
                            enabled = canUpdate,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 2.dp),
                            contentPadding = PaddingValues(4.dp),
                            shape = RoundedCornerShape(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected) Color.White
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) { Text(status, fontSize = 12.sp) }
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
                    ) { Text(if (expanded) "Ver menos" else "Ver más", textDecoration = TextDecoration.Underline) }
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

                // Opiniones (carrusel)
                ReviewsSection(
                    title = "Opiniones",
                    reviews = viewModel.reviews,
                    isLoading = viewModel.isLoadingReviews,
                    error = viewModel.reviewsError
                )

                // Tu reseña (tarjeta bonita coherente con 5 estrellas)
                if ((userGame?.score ?: 0) > 0 || !userGame?.notes.isNullOrBlank()) {
                    YourReviewCard(
                        score0to100 = userGame?.score ?: 0,
                        notes = userGame?.notes.orEmpty()
                    )
                    Spacer(Modifier.height(12.dp))
                }

                // Botón escribir/editar reseña
                var showReviewSheet by remember { mutableStateOf(false) }
                Button(
                    onClick = {
                        if (userId == null) {
                            Toast.makeText(context, "Inicia sesión para reseñar", Toast.LENGTH_SHORT).show()
                        } else showReviewSheet = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(if ((userGame?.score ?: 0) > 0 || !userGame?.notes.isNullOrBlank()) "Editar reseña" else "Escribir reseña")
                }

                Spacer(Modifier.height(24.dp))

                if (showReviewSheet) {
                    // Slider 0..100
                    var tempScore100 by rememberSaveable { mutableStateOf(userGame?.score ?: 0) } // 0..100
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

                            // --- Slider + preview ---
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Puntuación", style = MaterialTheme.typography.labelLarge)
                                Text(String.format("%.1f / 10", tempScore100 / 10f))
                            }

                            Slider(
                                value = tempScore100.toFloat(),
                                onValueChange = { tempScore100 = it.roundToInt().coerceIn(0, 100) },
                                valueRange = 0f..100f,
                                steps = 99
                            )

                            // Preview 5⭐ con medias
                            FiveStarRating(
                                score0to10 = tempScore100 / 10f,
                                starSize = 22.dp
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

                            errorMsg?.let { Text(it, color = MaterialTheme.colorScheme.error) }

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

                                                // Envío en 0..10 ENTERO para encajar con tu repo actual:
                                                val score0to10Float = tempScore100 / 10f
                                                reviewsRepo.upsert(
                                                    gameId = gameId,
                                                    score0to10 = score0to10Float,   // <- Float
                                                    notes = tempNotes,
                                                    bearer = b
                                                ).getOrThrow()

                                                Toast.makeText(context, "Reseña guardada", Toast.LENGTH_SHORT).show()
                                                showReviewSheet = false
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
            }
        }
    }
}

/** Tarjeta compacta para "Tu reseña" usando 5 estrellas con medias. */
@Composable
private fun YourReviewCard(
    score0to100: Int,
    notes: String,
    modifier: Modifier = Modifier
) {
    val surface = MaterialTheme.colorScheme.surface
    val overlay = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)

    Surface(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 3.dp,
        shadowElevation = 6.dp
    ) {
        Column(
            Modifier
                .background(Brush.verticalGradient(listOf(surface, overlay, surface)))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tu reseña", style = MaterialTheme.typography.titleMedium)
                FiveStarRating(score0to10 = (score0to100 / 10f), starSize = 16.dp)
            }
            if (notes.isNotBlank()) {
                Text(notes, style = MaterialTheme.typography.bodyMedium, lineHeight = 18.sp)
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

@Composable
fun FiveStarRating(
    score0to10: Float,
    modifier: Modifier = Modifier,
    starSize: Dp = 20.dp,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    // Paso 1: convertir 0..10 a 0..5
    val raw5 = (score0to10 / 2f).coerceIn(0f, 5f)
    // Paso 2: cuantizar a saltos de 0.5 (redondeo estándar)
    val q5 = (raw5 * 2f).roundToInt() / 2f  // 2.76 -> 3.0, 2.24 -> 2.0, 2.5 -> 2.5

    Row(modifier = modifier) {
        repeat(5) { i ->
            val idx = i + 1
            val full = q5 >= idx
            val half = !full && q5 >= idx - 0.5f

            val icon = when {
                full -> Icons.Filled.Star
                half -> Icons.Filled.StarHalf
                else -> Icons.Outlined.Star
            }
            val starTint = if (full || half) tint else MaterialTheme.colorScheme.onSurfaceVariant

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = starTint,
                modifier = Modifier.size(starSize)
            )
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
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
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
            .height(180.dp)
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
                    .wrapContentHeight()
            )
        }
    }

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
                    Brush.verticalGradient(listOf(surface, overlay, surface))
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(review.username ?: "Usuario", style = MaterialTheme.typography.titleMedium)
                    }

                    FiveStarRating(
                        score0to10 = (review.score0to10 ?: 0).toFloat(),
                        starSize = 18.dp,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

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

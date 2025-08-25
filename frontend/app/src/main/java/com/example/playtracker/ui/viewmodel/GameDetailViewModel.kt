package com.example.playtracker.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.playtracker.data.remote.service.GameApi
import com.example.playtracker.data.repository.UserGameRepository
import com.example.playtracker.data.remote.dto.game.GameDetailDto
import com.example.playtracker.data.repository.ReviewsRepository
import com.example.playtracker.domain.model.GameReviews
import com.example.playtracker.domain.model.Review
import com.example.playtracker.domain.model.UserGame
import kotlinx.coroutines.launch

class GameDetailViewModel(
    private val gameApi: GameApi,
    private val userGameRepo: UserGameRepository,
    private val reviewsRepo: ReviewsRepository
) : ViewModel() {

    var gameDetail by mutableStateOf<GameDetailDto?>(null)
        private set

    var userGame by mutableStateOf<UserGame?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    // --- estado para reseñas ---
    var isPostingReview by mutableStateOf(false)
        private set
    var postReviewError by mutableStateOf<String?>(null)
        private set
    var myReview by mutableStateOf<Review?>(null)
        private set

    // --- listado de reseñas públicas del juego ---
    var gameReviews by mutableStateOf<GameReviews?>(null)
        private set
    var reviews by mutableStateOf<List<Review>>(emptyList())
        private set
    var isLoadingReviews by mutableStateOf(false)
        private set
    var reviewsError by mutableStateOf<String?>(null)
        private set

    fun loadReviews(gameId: Long, bearer: String) {
        viewModelScope.launch {
            isLoadingReviews = true
            reviewsError = null
            reviewsRepo.getReviews(gameId = gameId, limit = 20, bearer = bearer)
                .onSuccess { gr ->
                    gameReviews = gr
                    reviews = gr.reviews
                }
                .onFailure { e ->
                    reviewsError = e.message ?: "No se pudieron cargar las reseñas"
                }
            isLoadingReviews = false
        }
    }

    fun submitReview(
        gameId: Long,
        score0to10: Float?,
        notes: String?,
        bearer: String,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            isPostingReview = true
            postReviewError = null

            reviewsRepo.upsert(
                gameId = gameId,
                score0to10 = score0to10,
                notes = notes,
                bearer = bearer
            )
                .onSuccess { review ->
                    myReview = review
                    onDone()
                }
                .onFailure { e ->
                    postReviewError = e.message ?: "No se pudo guardar la reseña"
                }

            isPostingReview = false
        }
    }

    fun loadGameDetail(gameId: Long) {
        viewModelScope.launch {
            isLoading = true
            error = null
            runCatching { gameApi.getGameDetails(gameId) }
                .onSuccess { dto ->
                    gameDetail = dto
                    isLoading = false
                }
                .onFailure { e ->
                    error = e.message ?: "Error al cargar detalles"
                    isLoading = false
                }
        }
    }

    fun getUserGame(userId: Int, gameId: Long) {
        viewModelScope.launch {
            runCatching { userGameRepo.getUserGame(userId, gameId) }
                .onSuccess { userGame = it }
        }
    }

    fun updateGameStatus(userId: Int, gameRawgId: Long, newStatus: String) {
        viewModelScope.launch {
            runCatching { userGameRepo.upsertUserGame(userId, gameRawgId, newStatus) }
                .onSuccess { userGame = it }
                .onFailure { error = "Error al actualizar estado del juego" }
        }
    }

    class Factory(
        private val gameApi: GameApi,
        private val userGameRepo: UserGameRepository,
        private val reviewsRepo: ReviewsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GameDetailViewModel(gameApi, userGameRepo, reviewsRepo) as T
        }
    }
}

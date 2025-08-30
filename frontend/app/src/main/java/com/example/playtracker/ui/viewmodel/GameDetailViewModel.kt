package com.example.playtracker.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playtracker.data.remote.dto.game.GameDetailDto
import com.example.playtracker.data.remote.service.RetrofitInstance
import com.example.playtracker.data.repository.ReviewsRepository
import com.example.playtracker.data.repository.UserGameRepository
import com.example.playtracker.data.repository.impl.ReviewsRepositoryImpl
import com.example.playtracker.data.repository.impl.UserGameRepositoryImpl
import com.example.playtracker.domain.model.GameReviews
import com.example.playtracker.domain.model.Review
import com.example.playtracker.domain.model.UserGame
import kotlinx.coroutines.launch

class GameDetailViewModel : ViewModel() {

    // --- Dependencias creadas aquí (simplificación sin DI) ---
    private val gameApi = RetrofitInstance.gameApi
    private val userGameRepo: UserGameRepository =
        UserGameRepositoryImpl(
            userGameApi = RetrofitInstance.userGameApi,
            gameApi = gameApi
        )
    private val reviewsRepo: ReviewsRepository =
        ReviewsRepositoryImpl(RetrofitInstance.reviewsApi)

    // --- Estado ---
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

    // --- Lógica ---
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
            if (newStatus.equals("No seguido", ignoreCase = true)) {
                runCatching { userGameRepo.deleteUserGame(userId, gameRawgId) }
                    .onSuccess {
                        userGame = null
                        myReview = null
                        error = null
                    }
                    .onFailure {
                        error = "Error al eliminar el juego de tu biblioteca"
                    }
            } else {
                runCatching { userGameRepo.upsertUserGame(userId, gameRawgId, newStatus) }
                    .onSuccess {
                        userGame = it
                        error = null
                    }
                    .onFailure {
                        error = "Error al actualizar estado del juego"
                    }
            }
        }
    }
}

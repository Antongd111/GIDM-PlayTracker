package com.example.playtracker.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.playtracker.data.remote.service.GameApi
import com.example.playtracker.data.repository.UserGameRepository
import com.example.playtracker.data.remote.dto.game.GameDetailDto // <-- dejamos DTO por ahora
import com.example.playtracker.domain.model.UserGame
import kotlinx.coroutines.launch

class GameDetailViewModel(
    private val gameApi: GameApi,
    private val userGameRepo: UserGameRepository
) : ViewModel() {

    var gameDetail by mutableStateOf<GameDetailDto?>(null)
        private set

    var userGame by mutableStateOf<UserGame?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

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
        private val userGameRepo: UserGameRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GameDetailViewModel(gameApi, userGameRepo) as T
        }
    }
}

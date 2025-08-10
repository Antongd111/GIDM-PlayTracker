package com.example.playtracker.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playtracker.data.api.RetrofitInstance
import com.example.playtracker.data.model.GameDetail
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.playtracker.data.model.UserGame
import com.example.playtracker.data.model.UserGameRequest
import com.example.playtracker.data.model.UserGameUpdate

class GameDetailViewModel : ViewModel() {

    var gameDetail by mutableStateOf<GameDetail?>(null)
        private set

    var userGame by mutableStateOf<UserGame?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun loadGameDetail(gameId: Int) {
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val result = RetrofitInstance.gameApi.getGameDetails(gameId)
                gameDetail = result
            } catch (e: Exception) {
                error = e.message ?: "Error al cargar detalles"
            } finally {
                isLoading = false
            }
        }
    }

    fun getUserGame(userId: Int, gameId: Int) {
        viewModelScope.launch {
            try {
                val result = RetrofitInstance.userGameApi.getUserGame(userId, gameId)
                userGame = result
            } catch (e: Exception) {
                // Puedes manejar el error si quieres mostrar algo
            }
        }
    }

    fun updateGameStatus(userId: Int, gameRawgId: Int, newStatus: String) {
        viewModelScope.launch {
            try {
                if (userGame != null) {
                    // Ya existe: actualizar estado
                    val updated = RetrofitInstance.userGameApi.updateUserGame(
                        userId = userId,
                        gameRawgId = gameRawgId,
                        body = UserGameUpdate(status = newStatus)
                    )
                    userGame = updated
                } else {
                    // No existe: crear nueva entrada
                    val created = RetrofitInstance.userGameApi.createUserGame(
                        userId = userId,
                        body = UserGameRequest(
                            game_rawg_id = gameRawgId,
                            status = newStatus
                        )
                    )
                    userGame = created
                }
            } catch (e: Exception) {
                error = "Error al actualizar estado del juego"
            }
        }
    }
}
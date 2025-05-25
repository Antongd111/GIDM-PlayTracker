package com.example.playtracker.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playtracker.data.api.RetrofitInstance
import com.example.playtracker.data.model.GameDetail
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class GameDetailViewModel : ViewModel() {

    var gameDetail by mutableStateOf<GameDetail?>(null)
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
}
package com.example.playtracker.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.playtracker.domain.model.Game
import com.example.playtracker.domain.model.GamePreview
import com.example.playtracker.data.repository.GameRepository
import com.example.playtracker.data.repository.RecommendationsRepository
import com.example.playtracker.data.repository.impl.GameRepositoryImpl
import com.example.playtracker.data.repository.impl.RecommendationsRepositoryImpl
import com.example.playtracker.data.remote.service.RetrofitInstance
import com.example.playtracker.data.remote.service.RecommendationsApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GamesViewModel(
    private val repo: GameRepository,
    private val recRepo: RecommendationsRepository
) : ViewModel() {
    private val _recError = MutableStateFlow<String?>(null)
    val recError = _recError.asStateFlow()

    // Populares
    private val _popular = MutableStateFlow<List<Game>>(emptyList())
    val popular: StateFlow<List<Game>> = _popular.asStateFlow()

    // Búsqueda
    private val _searchResults = MutableStateFlow<List<Game>>(emptyList())
    val searchResults: StateFlow<List<Game>> = _searchResults.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // Recomendaciones
    private val _recommendations = MutableStateFlow<List<GamePreview>>(emptyList())
    val recommendations: StateFlow<List<GamePreview>> = _recommendations.asStateFlow()

    fun loadPopular() = viewModelScope.launch {
        _loading.value = true
        _error.value = null
        _isSearching.value = false
        runCatching { repo.getPopular() }
            .onSuccess { _popular.value = it }
            .onFailure { _error.value = it.message ?: "Error cargando populares" }
        _loading.value = false
    }

    fun loadRecommendations(userId: Int, topK: Int = 20) = viewModelScope.launch {
        if (userId <= 0) {
            Log.e("REC", "userId inválido: $userId (no se llama al backend)")
            return@launch
        }
        _recError.value = null
        try {
            Log.d("REC", "Llamando a recomendaciones con userId=$userId topK=$topK")
            val items = recRepo.getRecommendations(userId, topK)
            _recommendations.value = items
            Log.d("REC", "OK -> ${items.size} recomendaciones")
        } catch (e: Exception) {
            _recError.value = e.message
            Log.e("REC", "FALLO recomendaciones", e)  // imprime stacktrace y mensaje
        }
    }

    fun search(q: String) = viewModelScope.launch {
        if (q.isBlank()) {
            _isSearching.value = false
            _searchResults.value = emptyList()
            return@launch
        }
        _loading.value = true
        _error.value = null
        _isSearching.value = true
        runCatching { repo.search(q) }
            .onSuccess { _searchResults.value = it }
            .onFailure { _error.value = it.message ?: "Error en la búsqueda" }
        _loading.value = false
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val gameRepo = GameRepositoryImpl(RetrofitInstance.gameApi)
                val recRepo = RecommendationsRepositoryImpl(RetrofitInstance.recommendationsApi)
                return GamesViewModel(gameRepo, recRepo) as T
            }
        }
    }

}

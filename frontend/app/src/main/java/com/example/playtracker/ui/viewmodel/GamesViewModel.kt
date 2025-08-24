package com.example.playtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.playtracker.domain.model.Game
import com.example.playtracker.data.repository.GameRepository
import com.example.playtracker.data.remote.service.RetrofitInstance
import com.example.playtracker.data.repository.impl.GameRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GamesViewModel(
    private val repo: GameRepository
) : ViewModel() {

    private val _popular = MutableStateFlow<List<Game>>(emptyList())
    val popular = _popular.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Game>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    fun loadPopular() = viewModelScope.launch {
        _loading.value = true
        _error.value = null
        _isSearching.value = false
        runCatching { repo.getPopular() }
            .onSuccess { _popular.value = it }
            .onFailure { _error.value = it.message ?: "Error cargando populares" }
        _loading.value = false
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
                // Implementación mínima sin DI: repo que usa RetrofitInstance por dentro
                val repo = GameRepositoryImpl(RetrofitInstance.gameApi)
                return GamesViewModel(repo) as T
            }
        }
    }
}

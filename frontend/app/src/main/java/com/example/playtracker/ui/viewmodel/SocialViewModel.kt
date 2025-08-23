package com.example.playtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.playtracker.data.api.RetrofitInstance
import com.example.playtracker.data.repository.FriendsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estados posibles para el botón
enum class FriendState { NONE, PENDING_SENT, FRIENDS }

data class FriendsUiState(
    val workingFor: Set<Int> = emptySet(),            // ids en loading
    val states: Map<Int, FriendState> = emptyMap(),   // estado por userId
    val error: String? = null
)

class FriendsViewModel(
    private val repo: FriendsRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(FriendsUiState())
    val ui: StateFlow<FriendsUiState> = _ui

    /** Asegura que los usuarios del resultado estén registrados como NONE si no sabemos más. */
    fun ensureUsers(ids: List<Int>) {
        if (ids.isEmpty()) return
        _ui.update { curr ->
            val next = curr.states.toMutableMap()
            ids.forEach { if (it !in next) next[it] = FriendState.NONE }
            curr.copy(states = next)
        }
    }

    /** Hidrata el estado real consultando backend: amigos y solicitudes que yo envié. */
    fun hydrateStatesForResults(resultIds: List<Int>, bearer: String) {
        if (resultIds.isEmpty()) return
        viewModelScope.launch {
            ensureUsers(resultIds)

            val friends = repo.listFriends(bearer).getOrElse { emptyList() }
            val outgoing = repo.listOutgoing(bearer).getOrElse { emptyList() }

            _ui.update { curr ->
                val next = curr.states.toMutableMap()
                // Amigos
                friends.forEach { if (it.id in resultIds) next[it.id] = FriendState.FRIENDS }
                // Solicitudes enviadas por mí
                outgoing.forEach { req ->
                    val otherId = req.other_user.id
                    if (otherId in resultIds) next[otherId] = FriendState.PENDING_SENT
                }
                curr.copy(states = next)
            }
        }
    }

    /** Acción única: según estado actual envía/cancela/unfriend. */
    fun toggleFriendAction(userId: Int, bearer: String, onSnack: (String) -> Unit) {
        val current = _ui.value.states[userId] ?: FriendState.NONE
        if (_ui.value.workingFor.contains(userId)) return

        viewModelScope.launch {
            _ui.update { it.copy(workingFor = it.workingFor + userId, error = null) }

            when (current) {
                FriendState.NONE -> {
                    repo.sendRequest(userId, bearer)
                        .onSuccess {
                            _ui.update {
                                it.copy(
                                    workingFor = it.workingFor - userId,
                                    states = it.states + (userId to FriendState.PENDING_SENT)
                                )
                            }
                            onSnack("Solicitud enviada")
                        }
                        .onFailure { e ->
                            // Rehidrata por si el backend dijo “ya existía”
                            hydrateStatesForResults(listOf(userId), bearer)
                            _ui.update { it.copy(workingFor = it.workingFor - userId, error = e.message) }
                            onSnack("No se pudo enviar: ${e.message ?: ""}")
                        }
                }
                FriendState.PENDING_SENT -> {
                    repo.cancelRequest(userId, bearer)
                        .onSuccess {
                            _ui.update {
                                it.copy(
                                    workingFor = it.workingFor - userId,
                                    states = it.states + (userId to FriendState.NONE)
                                )
                            }
                            onSnack("Solicitud cancelada")
                        }
                        .onFailure { e ->
                            hydrateStatesForResults(listOf(userId), bearer)
                            _ui.update { it.copy(workingFor = it.workingFor - userId, error = e.message) }
                            onSnack("No se pudo cancelar: ${e.message ?: ""}")
                        }
                }
                FriendState.FRIENDS -> {
                    repo.unfriend(userId, bearer)
                        .onSuccess {
                            _ui.update {
                                it.copy(
                                    workingFor = it.workingFor - userId,
                                    states = it.states + (userId to FriendState.NONE)
                                )
                            }
                            onSnack("Amistad eliminada")
                        }
                        .onFailure { e ->
                            hydrateStatesForResults(listOf(userId), bearer)
                            _ui.update { it.copy(workingFor = it.workingFor - userId, error = e.message) }
                            onSnack("No se pudo eliminar: ${e.message ?: ""}")
                        }
                }
            }
        }
    }
}

class FriendsViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repo = FriendsRepository(RetrofitInstance.friendsApi)
        return FriendsViewModel(repo) as T
    }
}

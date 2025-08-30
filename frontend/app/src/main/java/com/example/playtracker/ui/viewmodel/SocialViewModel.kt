package com.example.playtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playtracker.domain.model.FriendRequest
import com.example.playtracker.domain.model.User
import com.example.playtracker.data.repository.FriendsRepository
import com.example.playtracker.data.repository.UserRepository
import com.example.playtracker.data.repository.impl.FriendsRepositoryImpl
import com.example.playtracker.data.repository.impl.UserRepositoryImpl
import com.example.playtracker.data.remote.service.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FriendState { NONE, PENDING_SENT, FRIENDS }

data class SocialUiState(
    val workingFor: Set<Int> = emptySet(),
    val states: Map<Int, FriendState> = emptyMap(),
    val error: String? = null,

    val incoming: List<FriendRequest> = emptyList(),
    val workingIncoming: Set<Int> = emptySet(),

    val results: List<User> = emptyList(),
    val isSearching: Boolean = false
)

class SocialViewModel : ViewModel() {

    // --- Dependencias internas ---
    private val users: UserRepository =
        UserRepositoryImpl(
            users = RetrofitInstance.userApi,
            friends = RetrofitInstance.friendsApi
        )

    private val friends: FriendsRepository =
        FriendsRepositoryImpl(RetrofitInstance.friendsApi)

    private val _ui = MutableStateFlow(SocialUiState())
    val ui: StateFlow<SocialUiState> = _ui

    /** Buscar usuarios */
    fun search(query: String) = viewModelScope.launch {
        if (query.isBlank()) {
            _ui.update { it.copy(results = emptyList(), isSearching = false, error = null) }
            return@launch
        }
        _ui.update { it.copy(isSearching = true, error = null) }
        runCatching { users.searchUsers(query) }
            .onSuccess { list ->
                _ui.update { curr ->
                    val next = curr.states.toMutableMap()
                    list.forEach { if (it.id !in next) next[it.id] = FriendState.NONE }
                    curr.copy(results = list, states = next, isSearching = true)
                }
            }
            .onFailure { e ->
                _ui.update { it.copy(error = e.message, results = emptyList(), isSearching = true) }
            }
    }

    /** Hidrata estados (amigos y OUTGOING) */
    fun hydrateStatesForResults(bearer: String) = viewModelScope.launch {
        val resultIds = _ui.value.results.map { it.id }
        if (resultIds.isEmpty()) return@launch

        val friendsList = friends.listFriends(bearer).getOrElse { emptyList() }
        val outgoing = friends.listOutgoing(bearer).getOrElse { emptyList() }

        _ui.update { curr ->
            val next = curr.states.toMutableMap()
            friendsList.forEach { f -> if (f.id in resultIds) next[f.id] = FriendState.FRIENDS }
            outgoing.forEach { req ->
                val otherId = req.otherUser.id
                if (otherId in resultIds) next[otherId] = FriendState.PENDING_SENT
            }
            curr.copy(states = next)
        }
    }

    /** Carga solicitudes ENTRANTES */
    fun loadIncoming(bearer: String) = viewModelScope.launch {
        val inc = friends.listIncoming(bearer).getOrElse { emptyList() }
        _ui.update { it.copy(incoming = inc) }
    }

    /** Acción por usuario: send/cancel/unfriend */
    fun toggleFriendAction(userId: Int, bearer: String, onSnack: (String) -> Unit) {
        val current = _ui.value.states[userId] ?: FriendState.NONE
        if (_ui.value.workingFor.contains(userId)) return

        viewModelScope.launch {
            _ui.update { it.copy(workingFor = it.workingFor + userId, error = null) }

            when (current) {
                FriendState.NONE -> {
                    friends.sendRequest(userId, bearer)
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
                            hydrateStatesForResults(bearer)
                            _ui.update { it.copy(workingFor = it.workingFor - userId, error = e.message) }
                            onSnack("No se pudo enviar: ${e.message ?: ""}")
                        }
                }
                FriendState.PENDING_SENT -> {
                    friends.cancelRequest(userId, bearer)
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
                            hydrateStatesForResults(bearer)
                            _ui.update { it.copy(workingFor = it.workingFor - userId, error = e.message) }
                            onSnack("No se pudo cancelar: ${e.message ?: ""}")
                        }
                }
                FriendState.FRIENDS -> {
                    friends.unfriend(userId, bearer)
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
                            hydrateStatesForResults(bearer)
                            _ui.update { it.copy(workingFor = it.workingFor - userId, error = e.message) }
                            onSnack("No se pudo eliminar: ${e.message ?: ""}")
                        }
                }
            }
        }
    }

    fun acceptIncoming(fromUserId: Int, bearer: String, onSnack: (String) -> Unit) {
        viewModelScope.launch {
            _ui.update { it.copy(workingIncoming = it.workingIncoming + fromUserId) }
            friends.accept(fromUserId, bearer)
                .onSuccess {
                    _ui.update { curr ->
                        val newList = curr.incoming.filter { it.otherUser.id != fromUserId }
                        val newStates = curr.states + (fromUserId to FriendState.FRIENDS)
                        curr.copy(
                            workingIncoming = curr.workingIncoming - fromUserId,
                            incoming = newList,
                            states = newStates
                        )
                    }
                    onSnack("Solicitud aceptada")
                }
                .onFailure { e ->
                    _ui.update { it.copy(workingIncoming = it.workingIncoming - fromUserId, error = e.message) }
                    onSnack("No se pudo aceptar: ${e.message ?: ""}")
                }
        }
    }

    fun declineIncoming(fromUserId: Int, bearer: String, onSnack: (String) -> Unit) {
        viewModelScope.launch {
            _ui.update { it.copy(workingIncoming = it.workingIncoming + fromUserId) }
            friends.decline(fromUserId, bearer)
                .onSuccess {
                    _ui.update { curr ->
                        val newList = curr.incoming.filter { it.otherUser.id != fromUserId }
                        curr.copy(
                            workingIncoming = curr.workingIncoming - fromUserId,
                            incoming = newList,
                            states = curr.states + (fromUserId to FriendState.NONE)
                        )
                    }
                    onSnack("Solicitud rechazada")
                }
                .onFailure { e ->
                    _ui.update { it.copy(workingIncoming = it.workingIncoming - fromUserId, error = e.message) }
                    onSnack("No se pudo rechazar: ${e.message ?: ""}")
                }
        }
    }
}

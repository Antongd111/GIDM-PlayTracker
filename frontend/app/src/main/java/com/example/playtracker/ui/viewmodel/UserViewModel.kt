package com.example.playtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playtracker.domain.model.User
import com.example.playtracker.domain.model.Friend
import com.example.playtracker.domain.model.UserGame
import com.example.playtracker.data.repository.UserRepository
import com.example.playtracker.data.repository.UserGameRepository
import com.example.playtracker.data.repository.FriendsRepository
import com.example.playtracker.data.repository.impl.UserRepositoryImpl
import com.example.playtracker.data.repository.impl.UserGameRepositoryImpl
import com.example.playtracker.data.repository.impl.FriendsRepositoryImpl
import com.example.playtracker.data.remote.service.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class UserUiState(
    val loading: Boolean = true,
    val user: User? = null,
    val favorite: UserGame? = null,
    val completed: List<UserGame> = emptyList(),
    val friends: List<Friend> = emptyList(),
    val isOwn: Boolean = false,
    val friendState: FriendState = FriendState.NONE,
    val userGames: List<UserGame> = emptyList(),
    val reviews: List<UserGame> = emptyList(),
    val workingFriend: Boolean = false,
    val error: String? = null
)

class UserViewModel : ViewModel() {

    // --- Dependencias internas ---
    private val users: UserRepository =
        UserRepositoryImpl(RetrofitInstance.userApi, RetrofitInstance.friendsApi)
    private val userGamesRepo: UserGameRepository =
        UserGameRepositoryImpl(RetrofitInstance.userGameApi, RetrofitInstance.gameApi)
    private val friendsRepo: FriendsRepository =
        FriendsRepositoryImpl(RetrofitInstance.friendsApi)

    private val _ui = MutableStateFlow(UserUiState())
    val ui: StateFlow<UserUiState> = _ui

    /** Carga todo para la pantalla de perfil SOLO desde userGames (sin GameApi). */
    fun load(userId: Int, token: String?) {
        viewModelScope.launch {
            _ui.value = UserUiState(loading = true)
            val bearer = token?.let { "Bearer $it" }

            runCatching {
                // 1) Usuario y (si hay sesión) mi id
                val meId = if (bearer != null) users.me(bearer).id else null
                val user = users.getUser(userId)

                // 2) Todos los UserGame del usuario
                val allUG: List<UserGame> = userGamesRepo.listByUser(userId)

                // 3) Completados (solo filtramos por estado)
                val completedUG = allUG.filter { it.status?.equals("Completado", ignoreCase = true) == true }

                // 4) Favorito: intentamos localizarlo en la propia lista
                val favoriteUG: UserGame? = user.favoriteRawgId?.let { favId ->
                    allUG.firstOrNull { it.gameRawgId == favId }
                }

                // 5) Reseñas: los que tengan `notes` no vacías
                val reviewsUG = allUG.filter { !it.notes.isNullOrBlank() }

                // 6) Amigos y estado de amistad (si hay sesión)
                val friends = if (bearer != null) users.getFriendsOf(userId, bearer) else emptyList()
                val isOwn = meId != null && meId == user.id
                val friendState = if (!isOwn && bearer != null) {
                    val currentFriends = friendsRepo.listFriends(bearer).getOrElse { emptyList() }
                    val outgoing = friendsRepo.listOutgoing(bearer).getOrElse { emptyList() }
                    when {
                        currentFriends.any { it.id == user.id } -> FriendState.FRIENDS
                        outgoing.any { it.otherUser.id == user.id } -> FriendState.PENDING_SENT
                        else -> FriendState.NONE
                    }
                } else FriendState.NONE

                LoadedBundle(
                    user = user,
                    completed = completedUG,
                    favorite = favoriteUG,
                    friends = friends,
                    isOwn = isOwn,
                    friendState = friendState,
                    allUG = allUG,
                    reviews = reviewsUG
                )
            }.onSuccess { b ->
                _ui.value = UserUiState(
                    loading = false,
                    user = b.user,
                    completed = b.completed,
                    favorite = b.favorite,
                    friends = b.friends,
                    isOwn = b.isOwn,
                    friendState = b.friendState,
                    userGames = b.allUG,
                    reviews = b.reviews
                )
            }.onFailure { e ->
                _ui.value = UserUiState(loading = false, error = e.message)
            }
        }
    }

    /** Alterna acción de amistad según friendState actual. */
    fun toggleFriendAction(bearer: String) {
        val u = _ui.value.user ?: return
        if (_ui.value.isOwn) return
        if (_ui.value.workingFriend) return

        viewModelScope.launch {
            _ui.value = _ui.value.copy(workingFriend = true, error = null)
            when (_ui.value.friendState) {
                FriendState.NONE -> {
                    friendsRepo.sendRequest(u.id, bearer)
                        .onSuccess {
                            _ui.value = _ui.value.copy(
                                workingFriend = false,
                                friendState = FriendState.PENDING_SENT
                            )
                        }
                        .onFailure { e ->
                            _ui.value = _ui.value.copy(workingFriend = false, error = e.message)
                        }
                }
                FriendState.PENDING_SENT -> {
                    friendsRepo.cancelRequest(u.id, bearer)
                        .onSuccess {
                            _ui.value = _ui.value.copy(
                                workingFriend = false,
                                friendState = FriendState.NONE
                            )
                        }
                        .onFailure { e ->
                            _ui.value = _ui.value.copy(workingFriend = false, error = e.message)
                        }
                }
                FriendState.FRIENDS -> {
                    friendsRepo.unfriend(u.id, bearer)
                        .onSuccess {
                            _ui.value = _ui.value.copy(
                                workingFriend = false,
                                friendState = FriendState.NONE
                            )
                        }
                        .onFailure { e ->
                            _ui.value = _ui.value.copy(workingFriend = false, error = e.message)
                        }
                }
            }
        }
    }

    /** Actualiza nombre y estado del propio usuario. */
    fun updateProfile(newName: String, newStatus: String?, bearer: String) {
        val u = _ui.value.user ?: return
        if (!_ui.value.isOwn) return

        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = null)
            runCatching {
                users.updateUserProfile(
                    name = newName,
                    status = newStatus,
                    bearer = bearer
                )
            }.onSuccess { updated ->
                _ui.value = _ui.value.copy(
                    loading = false,
                    user = updated
                )
            }.onFailure { e ->
                _ui.value = _ui.value.copy(loading = false, error = e.message)
            }
        }
    }

    private data class LoadedBundle(
        val user: User,
        val completed: List<UserGame>,
        val favorite: UserGame?,
        val friends: List<Friend>,
        val isOwn: Boolean,
        val friendState: FriendState,
        val allUG: List<UserGame>,
        val reviews: List<UserGame>
    )
}

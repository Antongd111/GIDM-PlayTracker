package com.example.playtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playtracker.domain.model.User
import com.example.playtracker.domain.model.Game
import com.example.playtracker.domain.model.Friend
import com.example.playtracker.domain.model.UserGame
import com.example.playtracker.data.repository.UserRepository
import com.example.playtracker.data.repository.UserGameRepository
import com.example.playtracker.data.repository.FriendsRepository
import com.example.playtracker.data.remote.service.GameApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class UserReviewItem(
    val rawgId: Long,
    val title: String,
    val imageUrl: String?,
    val score: Int?,
    val text: String,
    val addedAt: String?
)

data class UserUiState(
    val loading: Boolean = true,
    val user: User? = null,
    val favorite: Game? = null,

    // Construidos a partir de UserGame COMPLETADO
    val completed: List<Game> = emptyList(),

    val friends: List<Friend> = emptyList(),
    val isOwn: Boolean = false,

    // Amistad con el usuario mostrado (solo si no es el propio)
    val friendState: FriendState = FriendState.NONE,
    val workingFriend: Boolean = false,

    // ⬇️ NUEVO: fuente de verdad para puntuaciones/notas/estado
    val userGames: List<UserGame> = emptyList(),

    // ⬇️ NUEVO: reseñas (UserGame con notes) + datos del juego
    val reviews: List<UserReviewItem> = emptyList(),

    val error: String? = null
)

class UserViewModel(
    private val users: UserRepository,
    private val userGames: UserGameRepository,
    private val gameApi: GameApi,
    private val friendsRepo: FriendsRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(UserUiState())
    val ui: StateFlow<UserUiState> = _ui

    /** Carga todo para la pantalla de perfil. */
    fun load(userId: Int, token: String?) {
        viewModelScope.launch {
            _ui.value = UserUiState(loading = true)
            val bearer = token?.let { "Bearer $it" }

            runCatching {
                // Quién soy yo (para saber si es mi propio perfil)
                val meId = if (bearer != null) users.me(bearer).id else null

                // Usuario mostrado
                val user = users.getUser(userId)

                // ⬇️ Trae TODOS los userGames del usuario (fuente de puntuaciones y reseñas)
                val allUG = userGames.listByUser(userId)

                // Filtra COMPLETADOS (para construir la parrilla) y RESEÑADOS (notes no vacías)
                val completedUG = allUG.filter { it.status?.equals("COMPLETADO", ignoreCase = true) == true }
                val reviewedUG = allUG.filter { !it.notes.isNullOrBlank() }

                // RAWG ids que necesitamos para pedir detalles: completados + reseñados + favorito
                val neededRawgIds = buildSet {
                    addAll(completedUG.map { it.gameRawgId })
                    addAll(reviewedUG.map { it.gameRawgId })
                    user.favoriteRawgId?.let { add(it) }
                }.toList()

                // Pide detalles (secuencial por simplicidad, puedes paralelizar si quieres)
                val details = neededRawgIds.associateWith { id -> gameApi.getGameDetails(id) }

                // Construye lista de Game para COMPLETADOS
                val completedGames: List<Game> = completedUG.mapNotNull { ug ->
                    val dto = details[ug.gameRawgId] ?: return@mapNotNull null
                    Game(
                        id = dto.id,
                        title = dto.title,
                        imageUrl = dto.imageUrl,
                        year = dto.releaseDate?.take(4)?.toIntOrNull(),
                        rating = dto.rating
                    )
                }

                // Favorito
                val favorite = user.favoriteRawgId?.let { favId ->
                    val dto = details[favId] ?: gameApi.getGameDetails(favId)
                    Game(
                        id = dto.id,
                        title = dto.title,
                        imageUrl = dto.imageUrl,
                        year = dto.releaseDate?.take(4)?.toIntOrNull(),
                        rating = dto.rating
                    )
                }

                // Reseñas (UserGame con notes + datos del juego)
                val reviews: List<UserReviewItem> = reviewedUG.mapNotNull { ug ->
                    val dto = details[ug.gameRawgId] ?: return@mapNotNull null
                    UserReviewItem(
                        rawgId = ug.gameRawgId,
                        title = dto.title,
                        imageUrl = dto.imageUrl,
                        score = ug.score,
                        text = ug.notes!!,
                        addedAt = ug.addedAt
                    )
                }

                // Lista de amigos del usuario mostrado (si tenemos token)
                val friends = if (bearer != null) users.getFriendsOf(userId, bearer) else emptyList()

                // Estado de relación si NO es mi perfil
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
                    completed = completedGames,
                    favorite = favorite,
                    friends = friends,
                    isOwn = isOwn,
                    friendState = friendState,
                    allUG = allUG,
                    reviews = reviews
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
        val completed: List<Game>,
        val favorite: Game?,
        val friends: List<Friend>,
        val isOwn: Boolean,
        val friendState: FriendState,
        val allUG: List<UserGame>,
        val reviews: List<UserReviewItem>
    )
}

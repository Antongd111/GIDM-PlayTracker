package com.example.playtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playtracker.domain.model.User
import com.example.playtracker.domain.model.Game
import com.example.playtracker.domain.model.Friend
import com.example.playtracker.data.repository.UserRepository
import com.example.playtracker.data.repository.UserGameRepository
import com.example.playtracker.data.remote.service.GameApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class UserUiState(
    val loading: Boolean = true,
    val user: User? = null,
    val favorite: Game? = null,
    val completed: List<Game> = emptyList(),
    val friends: List<Friend> = emptyList(),
    val error: String? = null
)

class UserViewModel(
    private val users: UserRepository,
    private val userGames: UserGameRepository,
    private val gameApi: GameApi
) : ViewModel() {

    private val _ui = MutableStateFlow(UserUiState())
    val ui: StateFlow<UserUiState> = _ui

    fun load(userId: Int, token: String?) {
        viewModelScope.launch {
            _ui.value = UserUiState(loading = true)

            runCatching {
                val bearer = token?.let { "Bearer $it" }

                val user = users.getUser(userId)

                val completed = if (bearer != null) userGames.getCompletedGames(userId, bearer) else emptyList()

                val favorite = user.favoriteRawgId?.let { favId ->
                    val dto = gameApi.getGameDetails(favId)
                    Game(
                        id = dto.id,
                        title = dto.title,
                        imageUrl = dto.imageUrl,
                        year = dto.releaseDate?.take(4)?.toIntOrNull(),
                        rating = dto.rating
                    )
                }

                val friends = if (bearer != null) users.getFriendsOf(userId, bearer) else emptyList()

                Triple(user, completed, Pair(favorite, friends))
            }.onSuccess { (user, completed, pair) ->
                val (favorite, friends) = pair
                _ui.value = UserUiState(
                    loading = false,
                    user = user,
                    completed = completed,
                    favorite = favorite,
                    friends = friends
                )
            }.onFailure { e ->
                _ui.value = UserUiState(loading = false, error = e.message)
            }
        }
    }
}

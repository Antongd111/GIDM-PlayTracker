package com.example.playtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playtracker.data.remote.service.GameApi
import com.example.playtracker.data.repository.FriendsRepository
import com.example.playtracker.data.repository.UserGameRepository
import com.example.playtracker.data.repository.UserRepository

class UserViewModelFactory(
    private val userRepo: UserRepository,
    private val userGameRepo: UserGameRepository,
    private val gameApi: GameApi,
    private val friendsRepo: FriendsRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UserViewModel(
            users = userRepo,
            userGames = userGameRepo,
            gameApi = gameApi,
            friendsRepo = friendsRepo
        ) as T
    }
}

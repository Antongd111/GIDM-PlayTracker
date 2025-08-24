package com.example.playtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playtracker.data.repository.UserGameRepository
import com.example.playtracker.data.repository.UserRepository
import com.example.playtracker.data.remote.service.GameApi

class UserViewModelFactory(
    private val userRepo: UserRepository,
    private val userGameRepo: UserGameRepository,
    private val gameApi: GameApi
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UserViewModel(userRepo, userGameRepo, gameApi) as T
    }
}

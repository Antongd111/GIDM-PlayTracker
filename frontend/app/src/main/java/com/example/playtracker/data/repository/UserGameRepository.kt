package com.example.playtracker.data.repository

import com.example.playtracker.domain.model.Game
import com.example.playtracker.domain.model.UserGame

interface UserGameRepository {
    suspend fun getCompletedGames(userId: Int, bearer: String): List<Game>
    suspend fun getUserGame(userId: Int, rawgId: Long): UserGame?
    suspend fun upsertUserGame(userId: Int, rawgId: Long, newStatus: String): UserGame
}

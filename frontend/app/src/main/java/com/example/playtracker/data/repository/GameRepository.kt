package com.example.playtracker.data.repository

import com.example.playtracker.domain.model.Game

interface GameRepository {
    suspend fun getPopular(): List<Game>
    suspend fun search(query: String): List<Game>
}
package com.example.playtracker.data.repository

import com.example.playtracker.domain.model.Game
import com.example.playtracker.domain.model.GamePreview

interface GameRepository {
    suspend fun getPopular(): List<GamePreview>
    suspend fun search(query: String): List<GamePreview>
}
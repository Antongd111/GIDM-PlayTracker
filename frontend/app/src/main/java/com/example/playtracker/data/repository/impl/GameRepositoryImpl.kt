package com.example.playtracker.data.repository.impl


import com.example.playtracker.data.remote.service.GameApi
import com.example.playtracker.data.repository.GameRepository
import com.example.playtracker.domain.model.Game
import com.example.playtracker.domain.model.GamePreview

class GameRepositoryImpl(
    private val api: GameApi
) : GameRepository {

    override suspend fun getPopular(): List<GamePreview> =
        api.getPopularGames().map { dto ->
            GamePreview(
                id = dto.id,
                title = dto.title,
                imageUrl = dto.imageUrl,
                year = dto.year ?: "0000",
            )
        }

    override suspend fun search(query: String): List<GamePreview> =
        api.searchGames(query).map { dto ->
            GamePreview(
                id = dto.id,
                title = dto.title,
                imageUrl = dto.imageUrl,
                year = dto.year ?: "0000",
            )
        }
}
package com.example.playtracker.data.repository.impl


import com.example.playtracker.data.remote.service.GameApi // ajusta el nombre real de tu interfaz
import com.example.playtracker.data.repository.GameRepository
import com.example.playtracker.domain.model.Game

class GameRepositoryImpl(
    private val api: GameApi
) : GameRepository {

    override suspend fun getPopular(): List<Game> =
        api.getPopularGames().map { dto ->
            Game(
                id = dto.id,
                title = dto.title,
                imageUrl = dto.imageUrl,
                year = dto.year.takeIf { it != 0 },
                rating = dto.rating
            )
        }

    override suspend fun search(query: String): List<Game> =
        api.searchGames(query).map { dto ->
            Game(
                id = dto.id,
                title = dto.title,
                imageUrl = dto.imageUrl,
                year = dto.year.takeIf { it != 0 },
                rating = dto.rating
            )
        }
}
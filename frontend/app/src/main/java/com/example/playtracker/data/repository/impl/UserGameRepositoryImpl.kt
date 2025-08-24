package com.example.playtracker.data.repository.impl

import com.example.playtracker.data.remote.dto.usergame.UserGameRequestDto
import com.example.playtracker.data.remote.dto.usergame.UserGameUpdateDto
import com.example.playtracker.data.remote.mapper.toDomain
import com.example.playtracker.data.remote.service.GameApi
import com.example.playtracker.data.remote.service.UserGameApi
import com.example.playtracker.domain.model.Game
import com.example.playtracker.data.repository.UserGameRepository
import com.example.playtracker.domain.model.UserGame

class UserGameRepositoryImpl(
    private val userGameApi: UserGameApi,
    private val gameApi: GameApi
) : UserGameRepository {

    override suspend fun getCompletedGames(userId: Int, bearer: String): List<Game> {
        // 1) obtener user_games del backend
        val userGames = userGameApi.getUserGames(userId)

        // 2) filtrar completados
        val completed = userGames.filter { it.status?.equals("COMPLETADO", ignoreCase = true) == true }

        // 3) pedir detalles de cada juego (máx. 20 como en tu UserScreen)
        val rawgIds = completed.map { it.game_rawg_id }.distinct().take(20)

        return rawgIds.mapNotNull { id ->
            try {
                val dto = gameApi.getGameDetails(id)
                // mapear DTO → domain.Game (lo simplificamos a un modelo "lite")
                Game(
                    id = dto.id,
                    title = dto.title,
                    imageUrl = dto.imageUrl,
                    year = dto.releaseDate?.take(4)?.toIntOrNull(),
                    rating = dto.rating
                )
            } catch (e: Exception) {
                null // si falla un id, lo ignoramos
            }
        }
    }

    override suspend fun getUserGame(userId: Int, rawgId: Long): UserGame? =
        runCatching { userGameApi.getUserGame(userId, rawgId) }
            .getOrNull()
            ?.toDomain()

    override suspend fun upsertUserGame(userId: Int, rawgId: Long, newStatus: String): UserGame {
        // Intentamos actualizar; si no existe, creamos
        return runCatching {
            userGameApi.updateUserGame(
                userId = userId,
                gameRawgId = rawgId,
                body = UserGameUpdateDto(status = newStatus)
            ).toDomain()
        }.getOrElse {
            userGameApi.createUserGame(
                userId = userId,
                body = UserGameRequestDto(
                    game_rawg_id = rawgId,
                    status = newStatus
                )
            ).toDomain()
        }
    }
}

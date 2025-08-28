package com.example.playtracker.data.repository.impl

import com.example.playtracker.data.remote.dto.usergame.UserGameRequestDto
import com.example.playtracker.data.remote.dto.usergame.UserGameUpdateDto
import com.example.playtracker.data.remote.mapper.toDomain
import com.example.playtracker.data.remote.service.GameApi
import com.example.playtracker.data.remote.service.UserGameApi
import com.example.playtracker.domain.model.Game
import com.example.playtracker.data.repository.UserGameRepository
import com.example.playtracker.domain.model.UserGame
import retrofit2.HttpException

class UserGameRepositoryImpl(
    private val userGameApi: UserGameApi,
    private val gameApi: GameApi
) : UserGameRepository {

    override suspend fun listByUser(userId: Int): List<UserGame> =
        userGameApi.getUserGames(userId).map { it.toDomain() }

    override suspend fun getCompletedGames(userId: Int, bearer: String): List<Game> {
        // ✅ Evita pedir dos veces: reutiliza listByUser()
        val userGames = listByUser(userId)

        val completed = userGames.filter { it.status?.equals("COMPLETADO", ignoreCase = true) == true }
        val rawgIds = completed.map { it.gameRawgId }.distinct().take(20)

        return rawgIds.mapNotNull { id ->
            runCatching {
                val dto = gameApi.getGameDetails(id)
                Game(
                    id = dto.id,
                    title = dto.title,
                    imageUrl = dto.imageUrl,
                    year = dto.releaseDate?.take(4)?.toIntOrNull(),
                    rating = dto.rating
                )
            }.getOrNull()
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

    override suspend fun deleteUserGame(userId: Int, rawgId: Long) {
        val resp = userGameApi.deleteUserGame(userId, rawgId)
        if (resp.isSuccessful || resp.code() == 404) {
            return
        } else {
            throw HttpException(resp)
        }
    }
}

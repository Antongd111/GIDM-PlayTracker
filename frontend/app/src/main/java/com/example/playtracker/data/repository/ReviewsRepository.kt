package com.example.playtracker.data.repository

import com.example.playtracker.domain.model.GameReviews
import com.example.playtracker.domain.model.Review

interface ReviewsRepository {
    suspend fun upsert(
        gameId: Long,
        score0to10: Float?,
        notes: String?,
        containsSpoilers: Boolean = false,
        bearer: String
    ): Result<Review>

    suspend fun getReviews(
        gameId: Long,
        limit: Int = 20,
        bearer: String
    ): Result<GameReviews>

    suspend fun like(gameId: Long, authorUserId: Int, bearer: String): Result<Unit>
    suspend fun unlike(gameId: Long, authorUserId: Int, bearer: String): Result<Unit>
}

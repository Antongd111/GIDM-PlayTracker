package com.example.playtracker.data.repository.impl

import com.example.playtracker.data.remote.dto.review.ReviewUpsertInDto
import com.example.playtracker.data.remote.service.ReviewsApi
import com.example.playtracker.data.repository.ReviewsRepository
import com.example.playtracker.domain.model.GameReviews
import com.example.playtracker.domain.model.Review
import com.example.playtracker.data.remote.mapper.toDomain

class ReviewsRepositoryImpl(
    private val api: ReviewsApi
) : ReviewsRepository {

    override suspend fun upsert(
        gameId: Long,
        score0to10: Int?,
        notes: String?,
        containsSpoilers: Boolean,
        bearer: String
    ): Result<Review> = runCatching {
        val body = ReviewUpsertInDto(
            score = score0to10?.coerceIn(0, 10)?.times(10),
            notes = notes?.ifBlank { null },
            contains_spoilers = containsSpoilers
        )
        val res = api.upsertReview(gameId, body, bearer)
        if (!res.isSuccessful) error("HTTP ${res.code()}")
        res.body()!!.toDomain()
    }

    override suspend fun getReviews(
        gameId: Long,
        limit: Int,
        bearer: String
    ): Result<GameReviews> = runCatching {
        val res = api.getGameReviews(gameId, limit, bearer)
        if (!res.isSuccessful) error("HTTP ${res.code()}")
        res.body()!!.toDomain()
    }

    override suspend fun like(gameId: Long, authorUserId: Int, bearer: String): Result<Unit> =
        runCatching {
            val res = api.likeReview(gameId, authorUserId, bearer)
            if (!res.isSuccessful) error("HTTP ${res.code()}")
        }

    override suspend fun unlike(gameId: Long, authorUserId: Int, bearer: String): Result<Unit> =
        runCatching {
            val res = api.unlikeReview(gameId, authorUserId, bearer)
            if (!res.isSuccessful) error("HTTP ${res.code()}")
        }
}

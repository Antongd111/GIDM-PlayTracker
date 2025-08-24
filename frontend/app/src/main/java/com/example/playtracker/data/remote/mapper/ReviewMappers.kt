package com.example.playtracker.data.remote.mapper

import com.example.playtracker.data.remote.dto.review.GameReviewsResponseDto
import com.example.playtracker.data.remote.dto.review.ReviewOutDto
import com.example.playtracker.domain.model.GameReviews
import com.example.playtracker.domain.model.Review
import kotlin.math.roundToInt

fun ReviewOutDto.toDomain(): Review =
    Review(
        userId = user_id,
        gameRawgId = game_rawg_id.toLong(),
        score0to10 = score?.let { (it / 10.0).roundToInt() }, // 0..100 -> 0..10
        notes = notes,
        containsSpoilers = contains_spoilers,
        reviewUpdatedAt = review_updated_at,
        username = username,
        avatarUrl = avatar_url,
        likesCount = likes_count,
        likedByMe = liked_by_me
    )

fun GameReviewsResponseDto.toDomain(): GameReviews =
    GameReviews(
        gameRawgId = game_rawg_id.toLong(),
        avgScoreGlobal0to10 = avg_score_global?.div(10.0),    // 0..100 -> 0..10
        countReviews = count_reviews,
        reviews = reviews.map { it.toDomain() }               // usa el mapper de arriba
    )
package com.example.playtracker.data.remote.dto.review

data class GameReviewsResponseDto(
    val game_rawg_id: Int,
    val avg_score_global: Double?,
    val count_reviews: Int,
    val reviews: List<ReviewOutDto>
)
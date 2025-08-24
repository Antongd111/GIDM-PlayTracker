package com.example.playtracker.domain.model

data class GameReviews(
    val gameRawgId: Long,
    val avgScoreGlobal0to10: Double?,  // normalizado a 0..10
    val countReviews: Int,
    val reviews: List<Review>
)
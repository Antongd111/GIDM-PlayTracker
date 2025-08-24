package com.example.playtracker.data.remote.dto.review

data class ReviewOutDto(
    val user_id: Int,
    val game_rawg_id: Int,
    val score: Int?,              // 0..100
    val notes: String?,
    val contains_spoilers: Boolean,
    val review_updated_at: String?,
    val username: String?,
    val avatar_url: String?,
    val likes_count: Int,
    val liked_by_me: Boolean
)
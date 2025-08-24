package com.example.playtracker.data.remote.dto.review

data class ReviewUpsertInDto(
    val score: Int?,
    val notes: String?,
    val contains_spoilers: Boolean = false
)
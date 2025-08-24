package com.example.playtracker.data.remote.dto.usergame

data class UserGameRequestDto(
    val game_rawg_id: Long,
    val status: String? = null,
    val score: Int? = null,
    val notes: String? = null
)
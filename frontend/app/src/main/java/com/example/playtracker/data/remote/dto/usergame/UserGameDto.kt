package com.example.playtracker.data.remote.dto.usergame

data class UserGameDto(
    val id: Int,
    val user_id: Int,
    val game_rawg_id: Long,
    val status: String?,
    val score: Int?,
    val notes: String?,
    val added_at: String
)
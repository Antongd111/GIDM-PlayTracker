package com.example.playtracker.data.model

data class UserGame(
    val id: Int,
    val user_id: Int,
    val game_rawg_id: Long,
    val status: String?,
    val score: Int?,
    val notes: String?,
    val added_at: String
)

data class UserGameRequest(
    val game_rawg_id: Long,
    val status: String? = null,
    val score: Int? = null,
    val notes: String? = null
)

data class UserGameUpdate(
    val status: String? = null,
    val score: Int? = null,
    val notes: String? = null
)
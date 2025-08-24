package com.example.playtracker.domain.model

enum class GameStatus { NO_SEGUIDO, POR_JUGAR, JUGANDO, COMPLETADO }

data class UserGame(
    val id: Int,
    val userId: Int,
    val gameRawgId: Long,
    val status: String? = null,
    val score: Int? = null,
    val notes: String? = null,
    val addedAt: String? = null
)
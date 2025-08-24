package com.example.playtracker.domain.model

data class GameDetail(
    val id: Long,
    val title: String,
    val description: String?,
    val imageUrl: String?,
    val releaseDate: String?,
    val rating: Float?,
    val screenshots: List<String>,
    val similarGames: List<Game>
)

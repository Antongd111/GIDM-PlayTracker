package com.example.playtracker.data.model

data class GameDetail(
    val id: Long,
    val title: String,
    val description: String,
    val releaseDate: String?,
    val imageUrl: String,
    val rating: Float,
    val platforms: List<String>,
    val genres: List<String>,
    val developers: List<String>,
    val publishers: List<String>,
    val tags: List<String>,
    val esrbRating: String?,
    val metacriticScore: Int?,
    val metacriticUrl: String?,
    val website: String?,
    val screenshots: List<String>,
    val videos: List<String>,
    val similarGames: List<SimilarGame>
)

data class SimilarGame(
    val id: Long,
    val title: String,
    val imageUrl: String
)
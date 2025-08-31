package com.example.playtracker.domain.model

data class UserGame(
    val id: Int,
    val userId: Int,
    val gameRawgId: Long,
    val status: String? = null,
    val score: Int? = null,
    val notes: String? = null,
    val addedAt: String? = null,
    val reviewUpdatedAt: String? = null,
    val containsSpoilers: Boolean? = false,
    val gameTitle: String? = null,
    val imageUrl: String? = null,
    val releaseYear: String? = null
)
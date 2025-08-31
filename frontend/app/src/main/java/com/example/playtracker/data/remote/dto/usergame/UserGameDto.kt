package com.example.playtracker.data.remote.dto.usergame

data class UserGameDto(
    val id: Int,
    val userId: Int,
    val gameRawgId: Long,
    val status: String?,
    val score: Int?,
    val notes: String?,
    val addedAt: String,
    val reviewUpdatedAt: String?,
    val containsSpoilers: Boolean?,
    val gameTitle: String?,
    val imageUrl: String?,
    val releaseYear: Int?
)
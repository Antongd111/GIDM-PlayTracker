package com.example.playtracker.data.remote.dto.game

data class GameDto(
    val id: Long,
    val title: String,
    val year: Int,
    val imageUrl: String,
    val rating: Float
)
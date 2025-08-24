package com.example.playtracker.domain.model

data class Game(
    val id: Long,
    val title: String,
    val imageUrl: String?,
    val year: Int? = null,
    val rating: Float? = null
)


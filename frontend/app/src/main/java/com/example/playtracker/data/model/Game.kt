package com.example.playtracker.data.model

data class Game(
    val id: Int,
    val title: String,
    val year: Int,
    val imageUrl: String,
    val rating: Float
)
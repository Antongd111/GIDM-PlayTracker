package com.example.playtracker.data.remote.dto.usergame

data class UserGameUpdateDto(
    val status: String? = null,
    val score: Int? = null,
    val notes: String? = null
)
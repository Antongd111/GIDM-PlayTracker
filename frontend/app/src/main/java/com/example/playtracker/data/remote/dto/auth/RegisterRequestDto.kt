package com.example.playtracker.data.remote.dto.auth

data class RegisterRequestDto(
    val email: String,
    val username: String,
    val password: String
)

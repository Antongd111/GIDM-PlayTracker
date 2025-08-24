package com.example.playtracker.data.remote.dto.user

import com.google.gson.annotations.SerializedName

data class UserDto(
    val id: Int,
    val email: String,
    val username: String,
    val status: String,
    val favorite_rawg_game_id: Long?,
    @SerializedName("avatar_url") val avatarUrl: String
)
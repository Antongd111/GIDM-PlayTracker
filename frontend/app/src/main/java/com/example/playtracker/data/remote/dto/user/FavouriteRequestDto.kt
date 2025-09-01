package com.example.playtracker.data.remote.dto.user

import com.google.gson.annotations.SerializedName

data class FavouriteRequestDto(
    @SerializedName("favorite_rawg_game_id")
    val favoriteRawgGameId: Long?
)

package com.example.playtracker.data.remote.dto.user

import com.google.gson.annotations.SerializedName

data class UpdateUserDto(
    @SerializedName("username") val name: String,
    val status: String?
)
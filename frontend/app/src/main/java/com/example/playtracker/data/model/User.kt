package com.example.playtracker.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int,
    val email: String,
    val username: String,
    val status: String,
    @SerializedName("avatar_url") val avatarUrl: String
)
package com.example.playtracker.domain.model

data class Review(
    val userId: Int,
    val gameRawgId: Long,
    val score0to10: Int?,           // normalizado a 0..10 para la UI
    val notes: String?,
    val containsSpoilers: Boolean,
    val reviewUpdatedAt: String?,
    val username: String?,
    val avatarUrl: String?,
    val likesCount: Int,
    val likedByMe: Boolean
)
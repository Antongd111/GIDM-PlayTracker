package com.example.playtracker.data.remote.mapper

import com.example.playtracker.data.remote.dto.usergame.UserGameDto
import com.example.playtracker.domain.model.UserGame

fun UserGameDto.toDomain() = UserGame(
    id = id,
    userId = userId,
    gameRawgId = gameRawgId,
    status = status,
    score = score,
    notes = notes,
    addedAt = addedAt,
    reviewUpdatedAt = reviewUpdatedAt,
    containsSpoilers = containsSpoilers,
    gameTitle = gameTitle,
    imageUrl = imageUrl,
    releaseYear = releaseYear?.toString()
)
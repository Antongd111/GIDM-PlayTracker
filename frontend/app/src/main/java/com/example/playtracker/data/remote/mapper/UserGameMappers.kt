package com.example.playtracker.data.remote.mapper

import com.example.playtracker.data.remote.dto.usergame.UserGameDto
import com.example.playtracker.domain.model.UserGame

fun UserGameDto.toDomain() = UserGame(
    id = id,
    userId = user_id,
    gameRawgId = game_rawg_id,
    status = status,
    score = score,
    notes = notes,
    addedAt = added_at
)
// data/remote/mapper/UserMappers.kt
package com.example.playtracker.data.remote.mapper

import com.example.playtracker.data.remote.dto.user.UserDto
import com.example.playtracker.domain.model.User


fun UserDto.toDomain() = User(
    id = id,
    name = username ?: "",
    avatarUrl = avatarUrl,
    status = status,
    favoriteRawgId = favorite_rawg_game_id
)

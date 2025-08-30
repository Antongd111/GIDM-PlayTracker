// data/repository/impl/RecommendationsRepositoryImpl.kt
package com.example.playtracker.data.repository.impl

import com.example.playtracker.data.remote.dto.game.GamePreviewDto
import com.example.playtracker.data.remote.service.RecommendationsApi
import com.example.playtracker.data.repository.RecommendationsRepository
import com.example.playtracker.domain.model.GamePreview

class RecommendationsRepositoryImpl(
    private val api: RecommendationsApi
) : RecommendationsRepository {

    override suspend fun getRecommendations(userId: Int, topK: Int): List<GamePreview> {
        val dtos = api.getRecommendations(userId = userId, topK = topK)
        return dtos.map { it.toDomain() }
    }
}

// --- mapper ---
private fun GamePreviewDto.toDomain() = GamePreview(
    id = id,
    title = title.ifBlank { "Game #$id" },
    imageUrl = imageUrl,
    releaseDate = releaseDate
)

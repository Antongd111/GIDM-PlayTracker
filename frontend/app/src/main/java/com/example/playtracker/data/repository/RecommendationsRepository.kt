package com.example.playtracker.data.repository

import com.example.playtracker.domain.model.GamePreview

interface RecommendationsRepository {
    suspend fun getRecommendations(userId: Int, topK: Int = 20): List<GamePreview>
}
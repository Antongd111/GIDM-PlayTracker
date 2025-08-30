package com.example.playtracker.data.remote.service

import com.example.playtracker.data.remote.dto.game.GamePreviewDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RecommendationsApi {
    @GET("recommendations/{userId}")
    suspend fun getRecommendations(
        @Path("userId") userId: Int,
        @Query("top_k") topK: Int = 20
    ): List<GamePreviewDto>
}
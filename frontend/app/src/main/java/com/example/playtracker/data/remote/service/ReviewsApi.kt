package com.example.playtracker.data.remote.service

import com.example.playtracker.data.remote.dto.review.GameReviewsResponseDto
import com.example.playtracker.data.remote.dto.review.ReviewOutDto
import com.example.playtracker.data.remote.dto.review.ReviewUpsertInDto
import retrofit2.Response
import retrofit2.http.*

interface ReviewsApi {
    @PUT("/reviews/{game_rawg_id}")
    suspend fun upsertReview(
        @Path("game_rawg_id") gameId: Long,
        @Body body: ReviewUpsertInDto,
        @Header("Authorization") bearer: String
    ): Response<ReviewOutDto>

    @GET("/reviews/game/{game_rawg_id}")
    suspend fun getGameReviews(
        @Path("game_rawg_id") gameId: Long,
        @Query("limit") limit: Int = 20,
        @Header("Authorization") bearer: String
    ): Response<GameReviewsResponseDto>

    @POST("/reviews/{game_rawg_id}/{author_user_id}/like")
    suspend fun likeReview(
        @Path("game_rawg_id") gameId: Long,
        @Path("author_user_id") authorUserId: Int,
        @Header("Authorization") bearer: String
    ): Response<Unit>

    @DELETE("/reviews/{game_rawg_id}/{author_user_id}/like")
    suspend fun unlikeReview(
        @Path("game_rawg_id") gameId: Long,
        @Path("author_user_id") authorUserId: Int,
        @Header("Authorization") bearer: String
    ): Response<Unit>
}

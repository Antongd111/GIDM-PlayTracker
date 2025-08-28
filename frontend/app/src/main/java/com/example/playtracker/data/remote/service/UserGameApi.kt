package com.example.playtracker.data.remote.service

import com.example.playtracker.data.remote.dto.usergame.UserGameDto
import com.example.playtracker.data.remote.dto.usergame.UserGameRequestDto
import com.example.playtracker.data.remote.dto.usergame.UserGameUpdateDto
import retrofit2.http.*

interface UserGameApi {

    @GET("users/{user_id}/games/")
    suspend fun getUserGames(
        @Path("user_id") userId: Int
    ): List<UserGameDto>

    @GET("users/{user_id}/games/{game_rawg_id}")
    suspend fun getUserGame(
        @Path("user_id") userId: Int,
        @Path("game_rawg_id") gameRawgId: Long
    ): UserGameDto

    @POST("users/{user_id}/games/")
    suspend fun createUserGame(
        @Path("user_id") userId: Int,
        @Body body: UserGameRequestDto
    ): UserGameDto

    @PUT("users/{user_id}/games/{game_rawg_id}")
    suspend fun updateUserGame(
        @Path("user_id") userId: Int,
        @Path("game_rawg_id") gameRawgId: Long,
        @Body body: UserGameUpdateDto
    ): UserGameDto

    @DELETE("users/{user_id}/games/{game_rawg_id}")
    suspend fun deleteUserGame(
        @Path("user_id") userId: Int,
        @Path("game_rawg_id") gameRawgId: Long
    ): retrofit2.Response<Unit>
}
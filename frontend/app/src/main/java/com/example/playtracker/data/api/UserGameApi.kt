package com.example.playtracker.data.api

import com.example.playtracker.data.model.UserGame
import com.example.playtracker.data.model.UserGameRequest
import com.example.playtracker.data.model.UserGameUpdate
import retrofit2.Response
import retrofit2.http.*

interface UserGameApi {

    @GET("users/{user_id}/games/")
    suspend fun getUserGames(
        @Path("user_id") userId: Int
    ): List<UserGame>

    @GET("users/{user_id}/games/{game_rawg_id}")
    suspend fun getUserGame(
        @Path("user_id") userId: Int,
        @Path("game_rawg_id") gameRawgId: Long
    ): UserGame

    @POST("users/{user_id}/games/")
    suspend fun createUserGame(
        @Path("user_id") userId: Int,
        @Body body: UserGameRequest
    ): UserGame

    @PUT("users/{user_id}/games/{game_rawg_id}")
    suspend fun updateUserGame(
        @Path("user_id") userId: Int,
        @Path("game_rawg_id") gameRawgId: Long,
        @Body body: UserGameUpdate
    ): UserGame

    @DELETE("users/{user_id}/games/{game_rawg_id}")
    suspend fun deleteUserGame(
        @Path("user_id") userId: Int,
        @Path("game_rawg_id") gameRawgId: Long
    )
}
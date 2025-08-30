package com.example.playtracker.data.remote.service

import com.example.playtracker.data.remote.dto.game.GameDto
import com.example.playtracker.data.remote.dto.game.GameDetailDto
import com.example.playtracker.data.remote.dto.game.GamePreviewDto
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path

interface GameApi {
    @GET("rawg/games/search")
    suspend fun searchGames(@Query("query") query: String): List<GamePreviewDto>

    @GET("rawg/games/popular")
    suspend fun getPopularGames(@Query("page") page: Int = 2): List<GamePreviewDto>

    @GET("rawg/games/{id}")
    suspend fun getGameDetails(@Path("id") gameId: Long): GameDetailDto
}
package com.example.playtracker.data.api

import com.example.playtracker.data.model.Game
import retrofit2.http.GET
import retrofit2.http.Query

interface GameApi {
    @GET("rawg/games/search")
    suspend fun searchGames(@Query("query") query: String): List<Game>

    @GET("rawg/games/popular")
    suspend fun getPopularGames(@Query("page") page: Int = 1): List<Game>
}
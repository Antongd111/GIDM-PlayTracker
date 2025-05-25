package com.example.playtracker.data.api

import com.example.playtracker.data.model.Game
import com.example.playtracker.data.model.GameDetail
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path

interface GameApi {
    @GET("rawg/games/search")
    suspend fun searchGames(@Query("query") query: String): List<Game>

    @GET("rawg/games/popular")
    suspend fun getPopularGames(@Query("page") page: Int = 1): List<Game>

    @GET("rawg/games/{id}")
    suspend fun getGameDetails(@Path("id") gameId: Int): GameDetail
}
package com.example.playtracker.data.remote.service

import com.example.playtracker.data.remote.dto.game.GamePreviewDto
import com.example.playtracker.data.remote.dto.user.FavouriteRequestDto
import com.example.playtracker.data.remote.dto.user.UpdateUserDto
import com.example.playtracker.data.remote.dto.user.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApi {

    // Buscar usuarios
    @GET("users/search/")
    suspend fun searchUsers(@Query("query") query: String): List<UserDto>

    // Obtener perfil del usuario actual
    @GET("users/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): UserDto

    // Obtener los juegos de los amigos del usuario
    @GET("users/{userId}/friends/games")
    suspend fun getFriendsGames(@Path("userId") userId: Int): List<GamePreviewDto>

    // Obtener perfil de un usuario por ID
    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Int): UserDto

    @PUT("users/{id}")
    suspend fun updateUserById(
        @Path("id") id: Int,
        @Body body: UpdateUserDto,
        @Header("Authorization") token: String
    ): UserDto

    @PATCH("users/{id}/favorite")
    suspend fun setFavoriteById(
        @Path("id") id: Int,
        @Body body: FavouriteRequestDto,
        @Header("Authorization") bearer: String
    ): UserDto
}

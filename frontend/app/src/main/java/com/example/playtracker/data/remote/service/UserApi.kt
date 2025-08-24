package com.example.playtracker.data.remote.service

import com.example.playtracker.data.remote.dto.user.UserDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApi {

    // Buscar usuarios
    @GET("users/search/")
    suspend fun searchUsers(@Query("query") query: String): List<UserDto>

    // Obtener perfil del usuario actual
    @GET("users/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): UserDto

    // Obtener perfil de un usuario por ID
    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Int): UserDto
}

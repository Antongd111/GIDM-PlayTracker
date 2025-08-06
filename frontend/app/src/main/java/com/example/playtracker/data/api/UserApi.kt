package com.example.playtracker.data.api

import com.example.playtracker.data.model.User
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApi {

    // Buscar usuarios
    @GET("users/search/")
    suspend fun searchUsers(@Query("query") query: String): List<User>

    // Obtener perfil del usuario actual
    @GET("users/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): User

    // Obtener perfil de un usuario por ID
    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Int): User
}

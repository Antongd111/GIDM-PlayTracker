package com.example.playtracker.data.api

import com.example.playtracker.data.model.User
import retrofit2.http.GET
import retrofit2.http.Query

interface UserApi {
    @GET("users/search/")
    suspend fun searchUsers(@Query("query") query: String): List<User>
}
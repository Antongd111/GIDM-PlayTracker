package com.example.playtracker.data.api

import com.example.playtracker.data.model.LoginRequest
import com.example.playtracker.data.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}
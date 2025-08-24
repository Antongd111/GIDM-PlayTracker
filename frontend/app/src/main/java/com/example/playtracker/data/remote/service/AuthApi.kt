package com.example.playtracker.data.remote.service

import com.example.playtracker.data.remote.dto.auth.LoginRequestDto
import com.example.playtracker.data.remote.dto.auth.LoginResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<LoginResponseDto>
}
package com.example.playtracker.data.remote.service

import com.example.playtracker.data.remote.dto.auth.LoginRequestDto
import com.example.playtracker.data.remote.dto.auth.LoginResponseDto
import com.example.playtracker.data.remote.dto.auth.RegisterRequestDto
import com.example.playtracker.data.remote.dto.auth.RegisterResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<LoginResponseDto>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<RegisterResponseDto>
}
package com.example.playtracker.data.remote.service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "http://192.168.18.191:8000/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    val gameApi: GameApi by lazy {
        retrofit.create(GameApi::class.java)
    }

    val userApi: UserApi by lazy {
        retrofit.create(UserApi::class.java)
    }

    val userGameApi: UserGameApi by lazy {
        retrofit.create(UserGameApi::class.java)
    }

    val friendsApi: FriendsApi by lazy {
        retrofit.create(FriendsApi::class.java)
    }

    val reviewsApi: ReviewsApi by lazy {
        retrofit.create(ReviewsApi::class.java)
    }
}

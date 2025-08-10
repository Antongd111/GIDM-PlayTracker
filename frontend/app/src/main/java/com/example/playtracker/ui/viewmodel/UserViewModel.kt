package com.example.playtracker.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.playtracker.data.api.RetrofitInstance
import com.example.playtracker.data.storage.TokenManager

class UserViewModel : ViewModel() {

    var userId by mutableStateOf<Int?>(null)
        private set

    fun setUserId(id: Int) {
        userId = id
    }

    suspend fun fetchAndSetUserId(context: Context) {
        try {
            val token = TokenManager.getToken(context)

            val user = RetrofitInstance.userApi.getCurrentUser("Bearer $token")
            userId = user.id

            Log.d("UserViewModel", "userId cargado: $userId")
        } catch (e: Exception) {
            Log.e("UserViewModel", "Error en getCurrentUser(): ${e.javaClass.simpleName} - ${e.message}", e)
        }
    }
}
package com.example.playtracker.data.repository

import com.example.playtracker.domain.model.Friend
import com.example.playtracker.domain.model.User

interface UserRepository {
    suspend fun me(bearer: String): User
    suspend fun getUser(id: Int): User
    suspend fun searchUsers(query: String): List<User>
    suspend fun getFriendsOf(userId: Int, bearer: String): List<Friend>
    suspend fun updateUserProfile(name: String, status: String?, bearer: String): User
}
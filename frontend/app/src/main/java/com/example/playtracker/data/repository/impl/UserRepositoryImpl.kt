package com.example.playtracker.data.repository.impl

import com.example.playtracker.data.remote.mapper.*
import com.example.playtracker.data.remote.service.UserApi
import com.example.playtracker.data.remote.service.FriendsApi
import com.example.playtracker.domain.model.User
import com.example.playtracker.domain.model.Friend
import com.example.playtracker.data.repository.UserRepository

class UserRepositoryImpl(
    private val users: UserApi,
    private val friends: FriendsApi
) : UserRepository {
    override suspend fun me(bearer: String): User =
        users.getCurrentUser(bearer).toDomain()

    override suspend fun getUser(id: Int): User =
        users.getUserById(id).toDomain()

    override suspend fun searchUsers(query: String): List<User> =
        users.searchUsers(query).map { it.toDomain() }

    override suspend fun getFriendsOf(userId: Int, bearer: String): List<Friend> =
        friends.listFriendsOf(userId, bearer).body().orEmpty().map { it.toDomain() }
}
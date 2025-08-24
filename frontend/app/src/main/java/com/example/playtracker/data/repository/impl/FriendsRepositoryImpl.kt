package com.example.playtracker.data.repository.impl

import com.example.playtracker.data.remote.mapper.*
import com.example.playtracker.data.remote.service.FriendsApi
import com.example.playtracker.data.repository.FriendsRepository
import com.example.playtracker.domain.model.FriendRequest
import com.example.playtracker.domain.model.User

class FriendsRepositoryImpl(
    private val api: FriendsApi
) : FriendsRepository {

    override suspend fun listFriends(bearer: String): Result<List<User>> = runCatching {
        api.listFriends(bearer).body().orEmpty().map { it.toUser() }
    }

    override suspend fun listOutgoing(bearer: String): Result<List<FriendRequest>> = runCatching {
        api.listOutgoing(bearer).body().orEmpty().map { it.toDomain() }
    }

    override suspend fun listIncoming(bearer: String): Result<List<FriendRequest>> = runCatching {
        api.listIncoming(bearer).body().orEmpty().map { it.toDomain() }
    }

    override suspend fun sendRequest(toUserId: Int, bearer: String) = runCatching {
        val res = api.sendFriendRequest(toUserId, bearer)
        if (!res.isSuccessful) error("HTTP ${res.code()}")
    }

    override suspend fun cancelRequest(toUserId: Int, bearer: String) = runCatching {
        val res = api.cancelFriendRequest(toUserId, bearer)
        if (!res.isSuccessful) error("HTTP ${res.code()}")
    }

    override suspend fun unfriend(otherUserId: Int, bearer: String) = runCatching {
        val res = api.unfriend(otherUserId, bearer)
        if (!res.isSuccessful) error("HTTP ${res.code()}")
    }

    override suspend fun accept(fromUserId: Int, bearer: String) = runCatching {
        val res = api.acceptFriendRequest(fromUserId, bearer)
        if (!res.isSuccessful) error("HTTP ${res.code()}")
    }

    override suspend fun decline(fromUserId: Int, bearer: String) = runCatching {
        val res = api.declineFriendRequest(fromUserId, bearer)
        if (!res.isSuccessful) error("HTTP ${res.code()}")
    }
}

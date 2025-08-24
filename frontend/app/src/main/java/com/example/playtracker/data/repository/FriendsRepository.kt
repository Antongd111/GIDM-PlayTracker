package com.example.playtracker.data.repository

import com.example.playtracker.domain.model.FriendRequest
import com.example.playtracker.domain.model.User

interface FriendsRepository {
    suspend fun listFriends(bearer: String): Result<List<User>>
    suspend fun listOutgoing(bearer: String): Result<List<FriendRequest>>
    suspend fun listIncoming(bearer: String): Result<List<FriendRequest>>
    suspend fun sendRequest(toUserId: Int, bearer: String): Result<Unit>
    suspend fun cancelRequest(toUserId: Int, bearer: String): Result<Unit>
    suspend fun unfriend(otherUserId: Int, bearer: String): Result<Unit>
    suspend fun accept(fromUserId: Int, bearer: String): Result<Unit>
    suspend fun decline(fromUserId: Int, bearer: String): Result<Unit>
}

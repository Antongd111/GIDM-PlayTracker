package com.example.playtracker.data.repository

import com.example.playtracker.data.api.FriendsApi
import retrofit2.HttpException

class FriendsRepository(private val api: FriendsApi) {

    suspend fun sendRequest(toUserId: Int, bearer: String): Result<Unit> = runCatching {
        val res = api.sendFriendRequest(toUserId, bearer)
        if (!(res.isSuccessful && res.body()?.ok == true)) throw HttpException(res)
    }

    suspend fun cancelRequest(toUserId: Int, bearer: String): Result<Unit> = runCatching {
        val res = api.cancelFriendRequest(toUserId, bearer)
        if (!(res.isSuccessful && res.body()?.ok == true)) throw HttpException(res)
    }

    suspend fun listFriends(bearer: String) = runCatching {
        val res = api.listFriends(bearer)
        if (!res.isSuccessful) throw HttpException(res)
        res.body().orEmpty()
    }

    suspend fun listOutgoing(bearer: String) = runCatching {
        val res = api.listOutgoing(bearer)
        if (!res.isSuccessful) throw HttpException(res)
        res.body().orEmpty()
    }

    suspend fun accept(fromUserId: Int, bearer: String): Result<Unit> = runCatching {
        val res = api.acceptFriendRequest(fromUserId, bearer)
        if (!(res.isSuccessful && res.body()?.ok == true)) throw HttpException(res)
    }

    suspend fun decline(fromUserId: Int, bearer: String): Result<Unit> = runCatching {
        val res = api.declineFriendRequest(fromUserId, bearer)
        if (!(res.isSuccessful && res.body()?.ok == true)) throw HttpException(res)
    }

    suspend fun unfriend(otherUserId: Int, bearer: String): Result<Unit> = runCatching {
        val res = api.unfriend(otherUserId, bearer)
        if (!(res.isSuccessful && res.body()?.ok == true)) throw HttpException(res)
    }

    suspend fun block(otherUserId: Int, bearer: String): Result<Unit> = runCatching {
        val res = api.blockUser(otherUserId, bearer)
        if (!(res.isSuccessful && res.body()?.ok == true)) throw HttpException(res)
    }
}
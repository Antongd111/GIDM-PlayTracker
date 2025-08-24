package com.example.playtracker.data.remote.service

import com.example.playtracker.data.remote.dto.friends.FriendDto
import com.example.playtracker.data.remote.dto.friends.IncomingReqDto
import com.example.playtracker.data.remote.dto.friends.OutgoingReqDto
import com.example.playtracker.data.remote.dto.friends.SimpleOkDto
import retrofit2.Response
import retrofit2.http.*


interface FriendsApi {

    @POST("friends/requests/{toUserId}")
    suspend fun sendFriendRequest(
        @Path("toUserId") toUserId: Int,
        @Header("Authorization") bearer: String
    ): Response<SimpleOkDto>

    @DELETE("friends/requests/{toUserId}")
    suspend fun cancelFriendRequest(
        @Path("toUserId") toUserId: Int,
        @Header("Authorization") bearer: String
    ): Response<SimpleOkDto>

    @POST("friends/{fromUserId}/accept")
    suspend fun acceptFriendRequest(
        @Path("fromUserId") fromUserId: Int,
        @Header("Authorization") bearer: String
    ): Response<SimpleOkDto>

    @POST("friends/{fromUserId}/decline")
    suspend fun declineFriendRequest(
        @Path("fromUserId") fromUserId: Int,
        @Header("Authorization") bearer: String
    ): Response<SimpleOkDto>

    @DELETE("friends/{otherUserId}")
    suspend fun unfriend(
        @Path("otherUserId") otherUserId: Int,
        @Header("Authorization") bearer: String
    ): Response<SimpleOkDto>

    @POST("friends/{otherUserId}/block")
    suspend fun blockUser(
        @Path("otherUserId") otherUserId: Int,
        @Header("Authorization") bearer: String
    ): Response<SimpleOkDto>

    @GET("friends/requests/incoming")
    suspend fun listIncoming(
        @Header("Authorization") bearer: String
    ): retrofit2.Response<List<IncomingReqDto>>

    @GET("friends")
    suspend fun listFriends(@Header("Authorization") bearer: String): Response<List<FriendDto>>

    @GET("friends/requests/outgoing")
    suspend fun listOutgoing(@Header("Authorization") bearer: String): Response<List<OutgoingReqDto>>

    @GET("friends/of/{userId}")
    suspend fun listFriendsOf(
        @Path("userId") userId: Int,
        @Header("Authorization") bearer: String
    ): retrofit2.Response<List<FriendDto>>
}
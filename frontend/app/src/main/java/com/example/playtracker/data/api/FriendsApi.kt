package com.example.playtracker.data.api

import retrofit2.Response
import retrofit2.http.*

data class SimpleOk(val ok: Boolean)

data class UserLite(
    val id: Int,
    val username: String?,
    val avatar_url: String?
)

data class FriendLite(
    val id: Int,
    val username: String?,
    val avatar_url: String?
)

data class OutgoingReq(
    val requester_id: Int,
    val other_user: UserLite,
    val status: String,
    val requested_at: String
)

data class IncomingReq(
    val requester_id: Int,
    val other_user: UserLite,
    val status: String,
    val requested_at: String
)

interface FriendsApi {

    @POST("friends/requests/{toUserId}")
    suspend fun sendFriendRequest(
        @Path("toUserId") toUserId: Int,
        @Header("Authorization") bearer: String
    ): Response<SimpleOk>

    @DELETE("friends/requests/{toUserId}")
    suspend fun cancelFriendRequest(
        @Path("toUserId") toUserId: Int,
        @Header("Authorization") bearer: String
    ): Response<SimpleOk>

    @POST("friends/{fromUserId}/accept")
    suspend fun acceptFriendRequest(
        @Path("fromUserId") fromUserId: Int,
        @Header("Authorization") bearer: String
    ): Response<SimpleOk>

    @POST("friends/{fromUserId}/decline")
    suspend fun declineFriendRequest(
        @Path("fromUserId") fromUserId: Int,
        @Header("Authorization") bearer: String
    ): Response<SimpleOk>

    @DELETE("friends/{otherUserId}")
    suspend fun unfriend(
        @Path("otherUserId") otherUserId: Int,
        @Header("Authorization") bearer: String
    ): Response<SimpleOk>

    @POST("friends/{otherUserId}/block")
    suspend fun blockUser(
        @Path("otherUserId") otherUserId: Int,
        @Header("Authorization") bearer: String
    ): Response<SimpleOk>

    @GET("friends/requests/incoming")
    suspend fun listIncoming(
        @Header("Authorization") bearer: String
    ): retrofit2.Response<List<IncomingReq>>

    @GET("friends")
    suspend fun listFriends(@Header("Authorization") bearer: String): Response<List<FriendLite>>

    @GET("friends/requests/outgoing")
    suspend fun listOutgoing(@Header("Authorization") bearer: String): Response<List<OutgoingReq>>

    @GET("friends/of/{userId}")
    suspend fun listFriendsOf(
        @Path("userId") userId: Int,
        @Header("Authorization") bearer: String
    ): retrofit2.Response<List<FriendLite>>
}
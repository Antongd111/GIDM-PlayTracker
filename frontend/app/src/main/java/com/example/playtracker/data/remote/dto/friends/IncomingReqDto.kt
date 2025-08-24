package com.example.playtracker.data.remote.dto.friends

import com.example.playtracker.data.remote.dto.user.UserLiteDto

data class IncomingReqDto(
    val requester_id: Int,
    val other_user: UserLiteDto,
    val status: String,
    val requested_at: String
)
package com.ghostbug.heavyliftsapp.domain.model

import kotlin.time.Instant

data class LeaderboardMemberEntity(
   val  id:String?=null ,
    val leaderboardId:String,
    val userId:String ,
    val status:String,
    val joinedAt: Instant?=null
)

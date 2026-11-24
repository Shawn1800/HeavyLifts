package com.ghostbug.heavyliftsapp.domain.model

import kotlin.time.Instant



data class LeaderboardEntity(
    val id: String?=null,
    val ownerId: String,
    val name: String,
    val createdAt: Instant?=null
)

package com.ghostbug.heavyliftsapp.domain.model



data class OneRepMaxEntity(
    val id: Long=0L,
    val exerciseId: Long,
    val curr1RM: Float,
    val prev1RM: Float,
    val changePercent: Float,
    val date: Long,
    val userId: String
)

package com.ghostbug.heavyliftsapp.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(val icon: ImageVector)

val TOP_LEVEL_DESTINATIONS = mapOf(
    Route.HomeScreen    to BottomNavItem(icon = Icons.Filled.FitnessCenter),
    Route.ProgressScreen to BottomNavItem(icon = Icons.Filled.BarChart),
//    Route.LeaderBoards  to BottomNavItem(icon = Icons.Outlined.EmojiEvents),
    Route.ProfileScreen to BottomNavItem(icon = Icons.Outlined.PersonOutline)
)

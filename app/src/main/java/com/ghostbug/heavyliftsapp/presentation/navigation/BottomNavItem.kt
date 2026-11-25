package com.ghostbug.heavyliftsapp.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.ui.graphics.vector.ImageVector
import com.ghostbug.heavyLifts.R

data class BottomNavItem(
    val icon: ImageVector,
    @param:StringRes val labelRes: Int,
    val iconRotation: Float = 0f
)

val TOP_LEVEL_DESTINATIONS = mapOf(
    Route.HomeScreen to BottomNavItem(Icons.Filled.Home, R.string.home),
    Route.ProgressScreen to BottomNavItem(Icons.Filled.BarChart, R.string.nav_progress),
    Route.ProfileScreen to BottomNavItem(Icons.Filled.Person, R.string.nav_you)
)

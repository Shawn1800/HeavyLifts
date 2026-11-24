package com.ghostbug.heavyliftsapp.presentation.screens.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ghostbug.heavyliftsapp.presentation.ui.theme.HeavyLiftsColors
import com.ghostbug.heavyliftsapp.presentation.ui.theme.HeavyLiftsType

@Composable
fun LeaderboardScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HeavyLiftsColors.Bg),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "COMING SOON...",
            color = HeavyLiftsColors.Fg1,
            fontFamily = HeavyLiftsType.Display,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

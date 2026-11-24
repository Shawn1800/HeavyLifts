package com.ghostbug.heavyliftsapp.presentation.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.ghostbug.heavyliftsapp.presentation.ui.theme.HeavyLiftsColors
import com.ghostbug.heavyliftsapp.presentation.ui.theme.HeavyLiftsType

@Composable
fun BottomNavigationBar(
    selectedKey: NavKey,
    onSelectKey: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(HeavyLiftsColors.NavBackground)
            .navigationBarsPadding()
            .padding(start = 8.dp, end = 8.dp, top = 6.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TOP_LEVEL_DESTINATIONS.forEach { (route, data) ->
            NavTabItem(
                modifier = Modifier.weight(1f),
                label = labelFor(route),
                icon = data.icon,
                iconRotation = if (route == Route.HomeScreen) -45f else 0f,
                isActive = route == selectedKey,
                onClick = { onSelectKey(route) }
            )
        }
    }
}

@Composable
private fun NavTabItem(
    modifier: Modifier = Modifier,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    iconRotation: Float,
    onClick: () -> Unit
) {
    val foreground by animateColorAsState(
        targetValue = if (isActive) HeavyLiftsColors.NavyStrong else HeavyLiftsColors.TextSecondary,
        animationSpec = tween(durationMillis = 180),
        label = "navigationForeground"
    )
    val pillColor by animateColorAsState(
        targetValue = if (isActive) HeavyLiftsColors.AccentSoft else HeavyLiftsColors.NavBackground,
        animationSpec = tween(durationMillis = 180),
        label = "navigationSelection"
    )

    Column(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick, role = Role.Tab)
            .semantics { selected = isActive },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 64.dp, height = 32.dp)
                .clip(CircleShape)
                .background(pillColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = foreground,
                modifier = Modifier.size(20.dp).rotate(iconRotation)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = foreground,
            fontFamily = HeavyLiftsType.Body,
            fontSize = 12.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

private fun labelFor(route: NavKey): String = when (route) {
    is Route.HomeScreen -> "Train"
    is Route.ProgressScreen -> "Progress"
    is Route.ProfileScreen -> "You"
    else -> ""
}

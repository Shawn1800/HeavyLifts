package com.ghostbug.heavyliftsapp.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.ghostbug.heavyLifts.R
import com.ghostbug.heavyliftsapp.presentation.ui.theme.HeavyLiftsColors

@Composable
fun BottomNavigationBar(
    selectedKey: NavKey,
    onSelectKey: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
    onAddExercise: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = HeavyLiftsColors.NavBackground,
                shadowElevation = 12.dp,
                modifier = Modifier.weight(1f, fill = false),
            ) {
                Row(
                    modifier = Modifier
                        .selectableGroup()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TOP_LEVEL_DESTINATIONS.forEach { (route, item) ->
                        val selected = route == selectedKey
                        NavTabItem(
                            item = item,
                            selected = selected,
                            onClick = { onSelectKey(route) },
                            modifier = if (selected) Modifier.weight(1f, fill = false) else Modifier
                        )
                    }
                }
            }
            if (selectedKey == Route.HomeScreen && onAddExercise != null) {
                FloatingActionButton(
                    onClick = onAddExercise,
                    shape = MaterialTheme.shapes.medium,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_exercise),
                    )
                }
            }
        }
    }
}

@Composable
private fun NavTabItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(item.labelRes)
    val interactions = remember { MutableInteractionSource() }
    val pressed by interactions.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "tabPress"
    )
    val iconScale = remember { Animatable(1f) }
    LaunchedEffect(selected) {
        if (selected) {
            iconScale.snapTo(0.65f)
            iconScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        } else {
            iconScale.snapTo(1f)
        }
    }

    val colors = MaterialTheme.colorScheme
    val foreground = if (selected) colors.onPrimary else colors.onSurfaceVariant
    Row(
        modifier = modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clip(CircleShape)
            .background(if (selected) colors.primary else Color.Transparent)
            .selectable(
                selected = selected,
                interactionSource = interactions,
                indication = LocalIndication.current,
                role = Role.Tab,
                onClick = onClick
            )
            .semantics { contentDescription = label }
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = foreground,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer {
                    rotationZ = item.iconRotation
                    scaleX = iconScale.value
                    scaleY = iconScale.value
                }
        )
        AnimatedVisibility(
            visible = selected,
            enter = expandHorizontally(expandFrom = Alignment.Start) + fadeIn(),
            exit = shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut()
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(start = 8.dp).clearAndSetSemantics {},
                style = MaterialTheme.typography.labelLarge,
                color = colors.onPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

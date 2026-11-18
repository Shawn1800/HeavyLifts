package com.ghostbug.heavyliftsapp.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val SetBadgeShape = GenericShape { size, _ ->
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val outerRadius = minOf(size.width, size.height) / 2f
    val innerRadius = outerRadius * 0.82f
    val points = 12

    for (point in 0 until points * 2) {
        val angle = point * PI / points - PI / 2
        val radius = if (point % 2 == 0) outerRadius else innerRadius
        val x = centerX + cos(angle).toFloat() * radius
        val y = centerY + sin(angle).toFloat() * radius

        if (point == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

@Composable
fun SetNumberBadge(
    number: Int,
    modifier: Modifier = Modifier.size(48.dp),
    containerColor: Color,
    contentColor: Color = Color.White
) {
    Box(
        modifier = modifier.background(containerColor, SetBadgeShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            color = contentColor,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

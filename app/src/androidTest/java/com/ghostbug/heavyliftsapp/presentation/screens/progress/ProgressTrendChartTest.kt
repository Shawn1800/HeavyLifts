package com.ghostbug.heavyliftsapp.presentation.screens.progress

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ghostbug.heavyliftsapp.domain.model.ProgressChartPoint
import com.ghostbug.heavyliftsapp.presentation.ui.theme.Demo103Theme
import com.ghostbug.heavyliftsapp.presentation.ui.theme.HeavyLiftsColors
import java.time.LocalDate
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProgressTrendChartTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun singleSessionDrawsMarkerWithinChart() {
        composeRule.setContent {
            Demo103Theme {
                Box(Modifier.width(240.dp).testTag("chart")) {
                    ProgressTrendChart(
                        points = listOf(ProgressChartPoint(LocalDate.of(2026, 11, 24), 100f)),
                        metricLabel = "Top weight",
                    )
                }
            }
        }

        val pixels = composeRule.onNodeWithTag("chart").captureToImage().toPixelMap()
        val hasMarker = (0 until pixels.height).any { y ->
            (0 until pixels.width).any { x -> pixels[x, y] == HeavyLiftsColors.AccentStrong }
        }
        assertTrue("The latest workout marker should be visible", hasMarker)
    }
}

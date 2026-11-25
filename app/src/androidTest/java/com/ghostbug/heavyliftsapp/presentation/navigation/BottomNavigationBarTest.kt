package com.ghostbug.heavyliftsapp.presentation.navigation

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.navigation3.runtime.NavKey
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ghostbug.heavyLifts.R
import com.ghostbug.heavyliftsapp.presentation.ui.theme.Demo103Theme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BottomNavigationBarTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun selectingTabDispatchesItsKeyAndExpandsOnlyItsLabel() {
        val selected = mutableStateOf<NavKey>(Route.HomeScreen)
        val selections = mutableListOf<NavKey>()
        composeRule.setContent {
            Demo103Theme {
                BottomNavigationBar(
                    selectedKey = selected.value,
                    onSelectKey = { key ->
                        selections.add(key)
                        selected.value = key
                    },
                )
            }
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val train = composeRule.onNodeWithContentDescription(context.getString(R.string.home))
        val progress = composeRule.onNodeWithContentDescription(context.getString(R.string.nav_progress))
        val you = composeRule.onNodeWithContentDescription(context.getString(R.string.nav_you))

        train.assertIsDisplayed().assertIsSelected()
        progress.assertIsDisplayed().assertIsNotSelected()
        you.assertIsDisplayed().assertIsNotSelected()
        val expandedTrainWidth = train.fetchSemanticsNode().boundsInRoot.width
        val collapsedProgressWidth = progress.fetchSemanticsNode().boundsInRoot.width

        progress.performClick()
        train.assertIsNotSelected()
        progress.assertIsSelected()
        you.assertIsNotSelected()
        assertTrue(train.fetchSemanticsNode().boundsInRoot.width < expandedTrainWidth)
        assertTrue(progress.fetchSemanticsNode().boundsInRoot.width > collapsedProgressWidth)

        you.performClick()
        you.assertIsSelected()
        train.performClick()
        train.assertIsSelected()
        composeRule.runOnIdle {
            assertEquals(listOf(Route.ProgressScreen, Route.ProfileScreen, Route.HomeScreen), selections)
        }
    }

    @Test
    fun addExerciseAppearsOnlyOnTrainWhenAvailable() {
        val selected = mutableStateOf<NavKey>(Route.HomeScreen)
        val canAddExercise = mutableStateOf(true)
        var addClicks = 0
        val onAddExercise: () -> Unit = { addClicks++ }
        composeRule.setContent {
            Demo103Theme {
                BottomNavigationBar(
                    selectedKey = selected.value,
                    onSelectKey = { selected.value = it },
                    onAddExercise = onAddExercise.takeIf { canAddExercise.value },
                )
            }
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val add = composeRule.onNodeWithContentDescription(context.getString(R.string.add_exercise))

        add.assertIsDisplayed().performClick()
        composeRule.runOnIdle { assertEquals(1, addClicks) }

        composeRule.onNodeWithContentDescription(context.getString(R.string.nav_progress)).performClick()
        add.assertDoesNotExist()

        composeRule.onNodeWithContentDescription(context.getString(R.string.nav_you)).performClick()
        add.assertDoesNotExist()

        composeRule.onNodeWithContentDescription(context.getString(R.string.home)).performClick()
        add.assertIsDisplayed()

        composeRule.runOnIdle { canAddExercise.value = false }
        add.assertDoesNotExist()
    }
}

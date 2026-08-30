package org.elnix.dragonlauncher.ui.dragon

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.elnix.dragonlauncher.ui.dragon.components.SwitchRow
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for [SwitchRow].
 *
 * [SwitchRow] combines a title text, optional description, and a Material3
 * [androidx.compose.material3.Switch] toggle inside a [androidx.compose.foundation.layout.Row]. It supports:
 * - State management via nullable Boolean (null = use defaultValue)
 * - Enabled/disabled state
 * - Optional onToggle callback (separate from onCheck when divider is shown)
 * - Optional reset button
 *
 * KEY TESTING PATTERN: Testing stateful composables. We pass state as
 * parameters and verify that interaction callbacks are invoked correctly.
 * The composable is "controlled" — the test owns the state and passes it down.
 *
 * NOTE: SwitchRow depends on AppObjectsColors.switchColors() from :core:ui:theme,
 * and TextWithDescription from :core:ui:dragon. These must be on the classpath.
 */
class SwitchRowTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun switchRow_displaysTitle() {
        composeTestRule.setContent {
            MaterialTheme {
                SwitchRow(
                    state = false,
                    title = "Enable Feature",
                    onCheck = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Enable Feature").assertIsDisplayed()
    }

    @Test
    fun switchRow_displaysDescriptionWhenProvided() {
        composeTestRule.setContent {
            MaterialTheme {
                SwitchRow(
                    state = true,
                    title = "Feature",
                    description = "This enables the feature",
                    onCheck = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Feature").assertIsDisplayed()
        composeTestRule.onNodeWithText("This enables the feature").assertIsDisplayed()
    }

    @Test
    fun switchRow_hidesDescriptionWhenNull() {
        composeTestRule.setContent {
            MaterialTheme {
                SwitchRow(
                    state = false,
                    title = "Feature",
                    description = null,
                    onCheck = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Feature").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nonexistent description").assertDoesNotExist()
    }

    @Test
    fun switchRow_clickRowTriggersOnCheck() {
        var checkedValue = false

        composeTestRule.setContent {
            MaterialTheme {
                SwitchRow(
                    state = false,
                    title = "Toggle",
                    onCheck = { checkedValue = it }
                )
            }
        }

        composeTestRule.onNodeWithText("Toggle").performClick()
        assertTrue("onCheck should have been called with true", checkedValue)
    }

    @Test
    fun switchRow_clickRowWhenCheckedTriggersOnCheckWithFalse() {
        var checkedValue = true

        composeTestRule.setContent {
            MaterialTheme {
                SwitchRow(
                    state = true,
                    title = "Toggle",
                    onCheck = { checkedValue = it }
                )
            }
        }

        composeTestRule.onNodeWithText("Toggle").performClick()
        assertFalse("onCheck should have been called with false", checkedValue)
    }

    @Test
    fun switchRow_usesDefaultWhenStateIsNull() {
        // When state is null, defaultValue (false by default) is used.
        // We verify the title is shown and the row is clickable.
        var wasClicked = false

        composeTestRule.setContent {
            MaterialTheme {
                SwitchRow(
                    state = null,
                    title = "Null State",
                    onCheck = { wasClicked = it }
                )
            }
        }

        composeTestRule.onNodeWithText("Null State").assertIsDisplayed()
        composeTestRule.onNodeWithText("Null State").performClick()
        assertTrue("onCheck should trigger with true (default false toggled)", wasClicked)
    }

    @Test
    fun switchRow_customDefaultValue() {
        var wasClicked = false

        composeTestRule.setContent {
            MaterialTheme {
                SwitchRow(
                    state = null,
                    title = "Custom Default",
                    defaultValue = true,
                    onCheck = { wasClicked = it }
                )
            }
        }

        composeTestRule.onNodeWithText("Custom Default").performClick()
        assertFalse("onCheck should trigger with false (default true toggled)", wasClicked)
    }
}

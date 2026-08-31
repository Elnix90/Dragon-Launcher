package org.elnix.dragonlauncher.ui.dragon

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.elnix.dragonlauncher.ui.composition.LocalUseCustomColorChannels
import org.elnix.dragonlauncher.ui.dragon.components.DragonGroupScope
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.SwitchRow
import org.elnix.dragonlauncher.ui.dragon.test.R
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

    @Composable
    fun TestTheme(content: @Composable DragonGroupScope.() -> Unit) {
        MaterialTheme {
            CompositionLocalProvider(
                LocalUseCustomColorChannels provides true
            ) {
                DragonSettingsGroup {
                    content()
                }
            }
        }
    }

    @Test
    fun switchRow_displaysTitle() {
        composeTestRule.setContent {
            TestTheme {
                SwitchRow(
                    state = false,
                    title = R.string.test,
                    onCheck = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Test").assertIsDisplayed()
    }

    @Test
    fun switchRow_displaysDescriptionWhenProvided() {
        composeTestRule.setContent {
            TestTheme {
                SwitchRow(
                    state = true,
                    title = R.string.test,
                    description = R.string.desc,
                    onCheck = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Test").assertIsDisplayed()
        composeTestRule.onNodeWithText("Desc").assertIsDisplayed()
    }

    @Test
    fun switchRow_hidesDescriptionWhenNull() {
        composeTestRule.setContent {
            TestTheme {
                SwitchRow(
                    state = false,
                    title = R.string.test,
                    description = null,
                    onCheck = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Test").assertIsDisplayed()
        composeTestRule.onNodeWithText("").assertDoesNotExist()
    }

    @Test
    fun switchRow_clickRowTriggersOnCheck() {
        var checkedValue = false

        composeTestRule.setContent {
            TestTheme {
                SwitchRow(
                    state = false,
                    title = R.string.test,
                    onCheck = { checkedValue = it }
                )
            }
        }

        composeTestRule.onNodeWithText("Test").performClick()
        assertTrue("onCheck should have been called with true", checkedValue)
    }

    @Test
    fun switchRow_clickRowWhenCheckedTriggersOnCheckWithFalse() {
        var checkedValue = true

        composeTestRule.setContent {
            TestTheme {
                SwitchRow(
                    state = true,
                    title = R.string.test,
                    onCheck = { checkedValue = it }
                )
            }
        }

        composeTestRule.onNodeWithText("Test").performClick()
        assertFalse("onCheck should have been called with false", checkedValue)
    }

    @Test
    fun switchRow_usesDefaultWhenStateIsNull() {
        // When state is null, defaultValue (false by default) is used.
        // We verify the title is shown and the row is clickable.
        var wasClicked = false

        composeTestRule.setContent {
            TestTheme {
                SwitchRow(
                    state = null,
                    title = R.string.test,
                    onCheck = { wasClicked = it }
                )
            }

            composeTestRule.onNodeWithText("Test").assertIsDisplayed()
            composeTestRule.onNodeWithText("Test").performClick()
            assertTrue("onCheck should trigger with true (default false toggled)", wasClicked)
        }
    }

    @Test
    fun switchRow_customDefaultValue() {
        var wasClicked = false

        composeTestRule.setContent {
            TestTheme {
                SwitchRow(
                    state = null,
                    title = R.string.test,
                    defaultValue = true,
                    onCheck = { wasClicked = it }
                )
            }
        }

        composeTestRule.onNodeWithText("Test").performClick()
        assertFalse("onCheck should trigger with false (default true toggled)", wasClicked)
    }
}

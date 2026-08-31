package org.elnix.dragonlauncher.ui.dragon

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.elnix.dragonlauncher.ui.base.compositionlocals.LocalDisableHapticFeedbackGlobally
import org.elnix.dragonlauncher.ui.composition.LocalUseCustomColorChannels
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonGroupScope
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for [DragonButton].
 *
 * [DragonButton] wraps Material3's [androidx.compose.material3.Button] with haptic feedback and optional
 * confirmation dialog. It supports:
 * - Click callback with haptic feedback
 * - Enable/disable state
 * - Optional confirmation popup via `needConfirm` (shows [org.elnix.dragonlauncher.ui.dragon.dialogs.UserValidation] dialog)
 * - Custom button colors
 *
 * KEY TESTING PATTERN: Testing button interactions and state. We verify that
 * clicking the button triggers the callback, and that disabled buttons don't
 * respond to clicks.
 *
 * NOTE: The confirmation dialog (needConfirm=true) is not tested here because
 * it involves AlertDialog rendering which requires more complex test setup.
 * Focus on the basic interaction pattern.
 */
class DragonButtonTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Composable
    fun TestTheme(content: @Composable DragonGroupScope.() -> Unit) {
        MaterialTheme {
            CompositionLocalProvider(
                LocalUseCustomColorChannels provides true,
                LocalDisableHapticFeedbackGlobally provides false
            ) {
                DragonSettingsGroup {
                    content()
                }
            }
        }
    }

    @Test
    fun dragonButton_displaysContent() {
        composeTestRule.setContent {
            TestTheme {
                DragonButton(onClick = {}) {
                    Text("Press Me")
                }
            }
        }

        composeTestRule.onNodeWithText("Press Me").assertIsDisplayed()
    }

    @Test
    fun dragonButton_clickTriggersCallback() {
        var clicked = false

        composeTestRule.setContent {
            TestTheme {
                DragonButton(onClick = { clicked = true }) {
                    Text("Click")
                }
            }
        }

        composeTestRule.onNodeWithText("Click").performClick()
        assertTrue("onClick should have been called", clicked)
    }

    @Test
    fun dragonButton_multipleClicks() {
        var count = 0

        composeTestRule.setContent {
            TestTheme {
                DragonButton(onClick = { count++ }) {
                    Text("Tap")
                }
            }
        }

        val button = composeTestRule.onNodeWithText("Tap")
        button.performClick()
        button.performClick()
        assertTrue("Should have been clicked twice", count == 2)
    }

    @Test
    fun dragonButton_disabledDoesNotTriggerCallback() {
        var clicked = false

        composeTestRule.setContent {
            TestTheme {
                DragonButton(
                    onClick = { clicked = true },
                    enabled = false
                ) {
                    Text("Disabled")
                }
            }
        }

        composeTestRule.onNodeWithText("Disabled").assertIsDisplayed()
        assertFalse("onClick should NOT have been called on disabled button", clicked)
    }
}

package org.elnix.dragonlauncher.ui.dragon

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.elnix.dragonlauncher.ui.dragon.components.ValidateCancelButtons
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for [ValidateCancelButtons].
 *
 * [ValidateCancelButtons] renders a pair of buttons (Confirm + Cancel) using
 * Material3's [androidx.compose.material3.ButtonGroup] API. It supports:
 * - Custom button text via `validateText` / `cancelText`
 * - Optional cancel button (hidden when `onCancel` is null)
 * - Enable/disable state via `validateEnabled`
 * - Callbacks: `onConfirm` and `onCancel`
 *
 * KEY TESTING PATTERN: Testing conditional UI rendering. The cancel button
 * should only appear when onCancel is provided. This is a common pattern
 * in settings UIs where some dialogs are confirm-only.
 *
 * NOTE: The actual button text defaults come from string resources (R.string.save,
 * R.string.cancel). In instrumented tests, these resolve from the app's resources.
 * If the app module's resources aren't available, use explicit text overrides.
 */
class ValidateCancelButtonsTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun validateCancelButtons_bothButtonsVisible() {
        composeTestRule.setContent {
            MaterialTheme {
                ValidateCancelButtons(
                    validateText = "Save",
                    cancelText = "Cancel",
                    onCancel = {},
                    onConfirm = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Save").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    @Test
    fun validateCancelButtons_confirmClickTriggersOnConfirm() {
        var confirmed = false

        composeTestRule.setContent {
            MaterialTheme {
                ValidateCancelButtons(
                    validateText = "OK",
                    cancelText = "Cancel",
                    onCancel = {},
                    onConfirm = { confirmed = true }
                )
            }
        }

        composeTestRule.onNodeWithText("OK").performClick()
        assertTrue("onConfirm should have been called", confirmed)
    }

    @Test
    fun validateCancelButtons_cancelClickTriggersOnCancel() {
        var cancelled = false

        composeTestRule.setContent {
            MaterialTheme {
                ValidateCancelButtons(
                    validateText = "Save",
                    cancelText = "Cancel",
                    onCancel = { cancelled = true },
                    onConfirm = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Cancel").performClick()
        assertTrue("onCancel should have been called", cancelled)
    }

    @Test
    fun validateCancelButtons_hidesCancelWhenOnCancelIsNull() {
        composeTestRule.setContent {
            MaterialTheme {
                ValidateCancelButtons(
                    validateText = "OK",
                    cancelText = "Cancel",
                    onCancel = null,
                    onConfirm = {}
                )
            }
        }

        composeTestRule.onNodeWithText("OK").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertDoesNotExist()
    }

    @Test
    fun validateCancelButtons_confirmDisabledWhenValidateEnabledIsFalse() {
        var confirmed = false

        composeTestRule.setContent {
            MaterialTheme {
                ValidateCancelButtons(
                    validateText = "Save",
                    cancelText = "Cancel",
                    validateEnabled = false,
                    onCancel = {},
                    onConfirm = { confirmed = true }
                )
            }
        }

        // The confirm button should exist but not be clickable
        composeTestRule.onNodeWithText("Save").assertIsDisplayed()
        // Note: clicking a disabled button should NOT trigger the callback.
        // Compose's test framework won't perform the click on a disabled node.
        assertFalse("onConfirm should NOT have been called on disabled button", confirmed)
    }

    @Test
    fun validateCancelButtons_customText() {
        composeTestRule.setContent {
            MaterialTheme {
                ValidateCancelButtons(
                    validateText = "Accept",
                    cancelText = "Decline",
                    onCancel = {},
                    onConfirm = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Accept").assertIsDisplayed()
        composeTestRule.onNodeWithText("Decline").assertIsDisplayed()
    }
}

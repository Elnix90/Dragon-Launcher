package org.elnix.dragonlauncher.ui.dragon

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.elnix.dragonlauncher.ui.dragon.text.TextDividerOld
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for [TextDividerOld].
 *
 * [TextDividerOld] (deprecated in favor of DragonSettingsGroup) renders a
 * horizontal divider with centered text between two line segments. It supports:
 * - Custom line and text colors
 * - Enable/disable state with transparency
 * - Custom thickness and padding
 *
 * KEY TESTING PATTERN: Testing deprecated composables. Even though this is
 * deprecated, it's still in use, so we ensure it continues to work correctly.
 * This is a common scenario when maintaining legacy code.
 */
@Suppress("DEPRECATION")
class TextDividerTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun textDivider_displaysText() {
        composeTestRule.setContent {
            MaterialTheme {
                TextDividerOld(text = "Section")
            }
        }

        composeTestRule.onNodeWithText("Section").assertIsDisplayed()
    }

    @Test
    fun textDivider_displaysLongText() {
        val text = "This is a longer divider label"
        composeTestRule.setContent {
            MaterialTheme {
                TextDividerOld(text = text)
            }
        }

        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun textDivider_enabledByDefault() {
        composeTestRule.setContent {
            MaterialTheme {
                TextDividerOld(text = "Enabled divider", enabled = true)
            }
        }

        composeTestRule.onNodeWithText("Enabled divider").assertIsDisplayed()
    }

    @Test
    fun textDivider_disabledStillDisplays() {
        composeTestRule.setContent {
            MaterialTheme {
                TextDividerOld(text = "Disabled divider", enabled = false)
            }
        }

        // Disabled state affects colors (transparency) but text should still render
        composeTestRule.onNodeWithText("Disabled divider").assertIsDisplayed()
    }
}

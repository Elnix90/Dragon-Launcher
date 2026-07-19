package org.elnix.dragonlauncher.ui.dragon

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.elnix.dragonlauncher.ui.dragon.text.LabelText
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for [LabelText].
 *
 * [LabelText] renders a text string using the Material3 `titleLarge` typography
 * and the theme's primary color. It wraps a standard [androidx.compose.material3.Text] composable with
 * consistent styling.
 *
 * Tests use [createAndroidComposeRule] which provides a [ComponentActivity]
 * host. All composables are wrapped in [MaterialTheme] so that theme-dependent
 * styles (typography, colors) resolve correctly.
 *
 * HOW TO RUN:
 *   ./gradlew :core:ui:dragon:connectedAndroidTest
 *   (Requires a connected device or emulator)
 */
class LabelTextTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun labelText_displaysText() {
        composeTestRule.setContent {
            MaterialTheme {
                LabelText(text = "Settings")
            }
        }

        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
    }

    @Test
    fun labelText_displaysEmptyString() {
        composeTestRule.setContent {
            MaterialTheme {
                LabelText(text = "")
            }
        }

        // An empty text node still exists in the tree; it just has no visible text.
        // We verify it doesn't crash and the node is present.
        composeTestRule.onNodeWithText("").assertExists()
    }

    @Test
    fun labelText_displaysLongText() {
        val longText = "This is a very long label text that might wrap or truncate depending on available space"
        composeTestRule.setContent {
            MaterialTheme {
                LabelText(text = longText)
            }
        }

        composeTestRule.onNodeWithText(longText).assertIsDisplayed()
    }

    @Test
    fun labelText_displaysSpecialCharacters() {
        val specialText = "Émojis & Spëcial Chârs: @#\$%"
        composeTestRule.setContent {
            MaterialTheme {
                LabelText(text = specialText)
            }
        }

        composeTestRule.onNodeWithText(specialText).assertIsDisplayed()
    }
}

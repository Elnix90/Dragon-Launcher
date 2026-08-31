package org.elnix.dragonlauncher.ui.dragon

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.sp
import org.elnix.dragonlauncher.ui.dragon.text.AutoResizeableText
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for [AutoResizeableText].
 *
 * [AutoResizeableText] wraps the standard Material3 [androidx.compose.material3.Text] composable with
 * [androidx.compose.foundation.text.TextAutoSize.StepBased] enabled, automatically shrinking font size when
 * text overflows. Default maxLines is 1 and minFontSize is 6.sp.
 *
 * KEY TESTING PATTERN: Verifying that the auto-size text composable renders
 * correctly with various text lengths. Auto-resizing is visual behavior that
 * doesn't affect the text content in the semantic tree.
 */
class AutoResizeableTextTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun autoResizeableText_displaysShortText() {
        composeTestRule.setContent {
            MaterialTheme {
                AutoResizeableText(text = "Hi")
            }
        }

        composeTestRule.onNodeWithText("Hi").assertIsDisplayed()
    }

    @Test
    fun autoResizeableText_displaysLongText() {
        val longText = "This is a very long text that should auto-resize to fit within the available space"
        composeTestRule.setContent {
            MaterialTheme {
                AutoResizeableText(text = longText)
            }
        }

        composeTestRule.onNodeWithText(longText).assertIsDisplayed()
    }

    @Test
    fun autoResizeableText_displaysEmptyText() {
        composeTestRule.setContent {
            MaterialTheme {
                AutoResizeableText(text = "")
            }
        }

        composeTestRule.onNodeWithText("").assertExists()
    }

    @Test
    fun autoResizeableText_displaysSpecialCharacters() {
        val text = "α β γ & < > \" '"
        composeTestRule.setContent {
            MaterialTheme {
                AutoResizeableText(text = text)
            }
        }

        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun autoResizeableText_withExplicitFontSize() {
        composeTestRule.setContent {
            MaterialTheme {
                AutoResizeableText(
                    text = "Custom size",
                    fontSize = 24.sp
                )
            }
        }

        composeTestRule.onNodeWithText("Custom size").assertIsDisplayed()
    }
}
